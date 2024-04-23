package studio.mkko120.dynagrams.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Builder class for creating holograms
 * @since 1.0.0
 * @author mkko120
 */
public class HologramBuilder {

    private String name;
    private Location location;
    private final ArrayList<String> lines = new ArrayList<>();

    private HologramBuilder() {}

    /**
     * Creates a new hologram builder
     * @return the hologram builder instance
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull HologramBuilder create() {
        return new HologramBuilder();
    }

    /**
     * Sets the name of the hologram
     * @param name the name of the hologram
     * @return the hologram builder instance
     */
    public HologramBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the location of the hologram
     * @param location the location of the hologram
     * @return the hologram builder instance
     */
    public HologramBuilder location(Location location) {
        this.location = location;
        return this;
    }

    /**
     * Adds a line to the hologram
     * @param line the line to add
     * @return the hologram builder instance
     */
    public HologramBuilder addline(String line) {
        lines.add(line);
        return this;
    }

    /**
     * Adds all lines to the hologram
     * @param lines the lines to add
     * @return the hologram builder instance
     */
    public HologramBuilder addAll(ArrayList<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    /**
     * Builds the hologram
     * @return the hologram instance
     */
    public Hologram build() {
        Hologram holo = DHAPI.getHologram(name);
        if (holo != null) {
            return holo;
        }
        holo = DHAPI.createHologram(name, location);
        DHAPI.setHologramLines(holo, lines);
        return holo;
    }


}
