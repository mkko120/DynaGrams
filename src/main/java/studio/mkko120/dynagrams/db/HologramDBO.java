package studio.mkko120.dynagrams.db;

import com.google.gson.Gson;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import lombok.Data;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.stream.Collectors;


/**
 * Database object class for converting hologram object to and from json
 * @since 1.0.0
 * @author mkko120
 */
@Data
public class HologramDBO {

    /**
     * Converts json string to hologram database object
     * @param json the json string to convert from
     * @return the converted database object
     */
    public static HologramDBO fromString(String json) {
        return new Gson().fromJson(json, HologramDBO.class);
    }

    /**
     * Converts hologram object to database object
     * @param hologram the hologram object to convert
     * @return the converted database object
     */
    @Contract("_ -> new")
    public static @NotNull HologramDBO fromHologram(Hologram hologram) {
        return new HologramDBO(
                hologram.getName(),
                new ArrayList<>(
                        hologram.getPage(0)
                                .getLines()
                                .stream()
                                .map(HologramLine::getText)
                                .collect(
                                    Collectors.toList()
                                )
                )
        );
    }

    // The name of the hologram
    private final String name;
    // The hologram's content
    private final ArrayList<String> content;


    /**
     * Converts the database object to json
     * @return the json string
     */
    public String toJson() {
        return new Gson().toJson(this);
    }
}
