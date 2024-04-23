package studio.mkko120.dynagrams.holograms;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import studio.mkko120.dynagrams.DynaGrams;
import studio.mkko120.dynagrams.db.DB;
import studio.mkko120.dynagrams.db.HologramDBO;
import studio.mkko120.dynagrams.util.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manager for holograms
 * @since 1.0.0
 * @author mkko120
 */
public class HologramManager {

    @Getter
    private HashMap<Integer, Hologram> holograms;
    private Logger logger = Bukkit.getLogger();

    private final DB database;

    public HologramManager(DB database) {
        this.database = database;
        this.holograms = new HashMap<>();
        createDatabaseStructure();
        loadHolograms();
    }

    private void createDatabaseStructure() {
        @Language("MySQL")
        String holograms = "CREATE TABLE IF NOT EXISTS holograms (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "json JSON not null," +
                "INDEX ((CAST(json ->> $.name AS CHAR(255)))));";
        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return;
            }
            con.createStatement().execute(holograms);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        this.holograms.values().forEach(Hologram::delete);
        this.holograms = new HashMap<>();
        loadHolograms();
    }

    private void loadHolograms() {
        YamlConfiguration holograms = DynaGrams.getInstance().getHolograms();
        if (holograms == null) {
            return;
        }

        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return;
            }
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM holograms WHERE id = ?")) {
                ConfigurationSection hologramsConfiguration = holograms.getConfigurationSection("holograms");
                if (hologramsConfiguration == null) {
                    holograms.createSection("holograms");
                    return;
                }
                hologramsConfiguration.getKeys(false).forEach(name -> {
                    ConfigurationSection hologramSection = holograms.getConfigurationSection("holograms." + name);
                    if (hologramSection == null) {
                        return;
                    }
                    int id = hologramSection.getInt("id");
                    try {
                        stmt.setInt(1, id);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String worldName = hologramSection.getString("world", "world");
                                double x = hologramSection.getDouble("x");
                                double y = hologramSection.getDouble("y");
                                double z = hologramSection.getDouble("z");
                                Location location = new Location(
                                        Bukkit.getWorld(worldName),
                                        x,
                                        y,
                                        z
                                );
                                HologramDBO dbo = HologramDBO.fromString(rs.getString("json"));
                                Hologram hologram = HologramBuilder.create()
                                        .name(rs.getString("name"))
                                        .location(location)
                                        .addAll(dbo.getContent())
                                        .build();
                                this.holograms.put(id, hologram);
                            } else {
                                Bukkit.getLogger().severe("Hologram '" + name + "' (ID: " + id + ") was not found in the database but existed in configuration. Deleting...");
                                hologramsConfiguration.set(name, null);
                            }
                        } catch (SQLTimeoutException e) {
                            e.printStackTrace();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                Bukkit.getLogger().info("Loaded " + this.holograms.size() + " holograms from configuration.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int createHologram(String name) {
        if (holograms.values().stream().anyMatch(hologram -> hologram.getName().equals(name))) {
            return 0;
        }
        HologramDBO dbo = new HologramDBO(name, new ArrayList<>(0));
        return insertHologram(dbo);
    }

    public boolean placeHologram(Player player, String name) {
        Map.Entry<Integer, HologramDBO> entry = getHologramByName(name);
        if (entry == null) {
            return false;
        }
        Hologram hologram = HologramBuilder.create()
                .name(name)
                .location(player.getLocation())
                .addAll(entry.getValue().getContent())
                .build();
        this.holograms.put(entry.getKey(), hologram);
        saveLocal(entry.getKey(), name, player.getLocation());
        return true;
    }

    public boolean unplaceHologram(String name) {
        int id = holograms.entrySet().stream()
                .filter(entry -> entry.getValue()
                        .getName()
                        .equals(name)
                )
                .mapToInt(HashMap.Entry::getKey)
                .findFirst()
                .orElse(-1);
        if (id == -1) {
            return false;
        }
        deleteLocal(id);
        return true;
    }

    public boolean deleteHologram(String name) {
        int id = holograms.entrySet().stream()
                .filter(entry -> entry.getValue()
                        .getName()
                        .equals(name)
                )
                .mapToInt(HashMap.Entry::getKey)
                .findFirst()
                .orElse(-1);
        if (id == -1) {
            return false;
        }
        deleteLocal(id);
        return deleteHologram(id);
    }

    public HashMap<Integer, HologramDBO> getAllHolograms() {
        HashMap<Integer, HologramDBO> holograms = new HashMap<>();
        @Language("MySQL")
        String select = "SELECT * FROM holograms;";
        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return null;
            }
            try (PreparedStatement stmt = con.prepareStatement(select)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        HologramDBO dbo = HologramDBO.fromString(rs.getString("json"));
                        holograms.put(rs.getInt("id"), dbo);
                    }
                } catch (SQLTimeoutException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return holograms;
    }

    private int insertHologram(@NotNull HologramDBO dbo) {
        @Language("MySQL")
        String insert = "INSERT INTO holograms (json) VALUES (?);";
        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return -1;
            }
            try (PreparedStatement stmt = con.prepareStatement(insert)) {
                stmt.setString(1, dbo.toString());
                stmt.execute();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private @Nullable Map.@Unmodifiable Entry<Integer,HologramDBO> getHologramByName(String name) {
        @Language("MySQL")
        String select = "SELECT * FROM holograms WHERE json ->> $.name = ?;";
        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return null;
            }
            try (PreparedStatement stmt = con.prepareStatement(select)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Map.entry(
                                rs.getInt("id"),
                                HologramDBO.fromString(rs.getString("json"))
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean deleteHologram(int id) {
        @Language("MySQL")
        String delete = "DELETE FROM holograms WHERE id = ?;";
        try (Connection con = this.database.getConnection()) {
            if (con == null) {
                logger.severe("Failed to connect to the database.");
                return false;
            }
            try (PreparedStatement stmt = con.prepareStatement(delete)) {
                stmt.setInt(1, id);
                return stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveLocal(int id, String name, Location location) {
        YamlConfiguration holograms = DynaGrams.getInstance().getHolograms();
        if (holograms == null) {
            return;
        }
        ConfigurationSection hologramSection = holograms.createSection("holograms." + name);
        hologramSection.set("id", id);
        hologramSection.set("world", location.getWorld().getName());
        hologramSection.set("x", location.getX());
        hologramSection.set("y", location.getY());
        hologramSection.set("z", location.getZ());
        Config.save(holograms, "holograms.yml");

    }
    private void deleteLocal(int id) {
        Hologram hologram = this.holograms.remove(id);
        DynaGrams.getInstance().getHolograms().set("holograms." + hologram.getName(), null);
        hologram.delete();
    }
}
