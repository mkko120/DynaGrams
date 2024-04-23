package studio.mkko120.dynagrams;

import co.aikar.commands.PaperCommandManager;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import studio.mkko120.dynagrams.commands.HologramsCommand;
import studio.mkko120.dynagrams.db.DB;
import studio.mkko120.dynagrams.holograms.HologramManager;
import studio.mkko120.dynagrams.util.Config;

import java.util.stream.Collectors;

@Getter
public final class DynaGrams extends JavaPlugin {

    @Getter
    private static DynaGrams instance;

    private PaperCommandManager commandManager;

    private YamlConfiguration config;

    private YamlConfiguration holograms;

    private DB database;

    private HologramManager hologramManager;


    @Override
    public void onLoad() {
        instance = this;
        // Load configurations
        config = Config.load("config.yml");
        holograms = Config.load("holograms.yml");
        if (config == null || holograms == null) {
            getLogger().severe("Failed to load the configuration file");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // connect to database
        database = new DB(
            config.getString("database.host", "localhost"),
            config.getInt("database.port", 3306),
            config.getString("database.database", "dynagrams"),
            config.getString("database.username", "dynagrams"),
            config.getString("database.password", "dynagrams")
        );
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        hologramManager = new HologramManager(database);
        commandManager = new PaperCommandManager(this);
        commandManager.enableUnstableAPI("brigadier");
        commandManager.enableUnstableAPI("help");

        // Register commands
        commandManager.registerCommand(new HologramsCommand(this, getLogger()));

        // Add command completions
        commandManager.getCommandCompletions()
                .registerAsyncCompletion(
                        "hologram",
                        c -> hologramManager.getHolograms()
                        .values()
                        .stream()
                        .map(Hologram::getName)
                        .collect(Collectors.toList())
                );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Config.save(config, "config.yml");
        Config.save(holograms, "holograms.yml");
        database.close();
    }
}
