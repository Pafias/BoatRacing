package me.pafias.boatracing.game;

import me.pafias.boatracing.BoatRacing;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GameConfig {

    static File file;
    static FileConfiguration config;

    public static void reloadConfig() {
        file = new File(BoatRacing.getInstance().getDataFolder(), "game.yml");
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration getConfig() {
        if (config == null)
            reloadConfig();
        return config;
    }

    public static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
