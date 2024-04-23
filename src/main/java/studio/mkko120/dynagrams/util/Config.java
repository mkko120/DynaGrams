package studio.mkko120.dynagrams.util;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import studio.mkko120.dynagrams.DynaGrams;

import java.io.File;
import java.io.IOException;

public class Config {


    /**
     * Loads specified config from resources or plugin folder
     * @param configName Name of a config
     * @return Returns loaded config file or error if it doesn't exist
     */
    public static YamlConfiguration load(String configName){
        YamlConfiguration config;
        File file = new File(DynaGrams.getInstance().getDataFolder() + File.separator + configName);
        if (!file.exists())
            try {
                DynaGrams.getInstance().saveResource(configName, false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return null;
            }
        config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config;
    }

    /**
     * Saves given config file
     * @param c Yaml file
     * @param file File name
     */
    public static void save(YamlConfiguration c, String file) {
        try {
            c.save(new File(DynaGrams.getInstance().getDataFolder(), file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}


