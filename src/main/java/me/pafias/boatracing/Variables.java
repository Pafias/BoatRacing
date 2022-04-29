package me.pafias.boatracing;

import me.pafias.boatracing.game.GameConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Variables {

    private BoatRacing instance;

    public Variables(BoatRacing plugin) {
        instance = plugin;
        reloadConfigs();
    }

    // config.yml
    public Location lobby = new Location(BoatRacing.getInstance().getServer().getWorld("world"), 0, 0, 0);

    // game.yml
    public World world = BoatRacing.getInstance().getServer().getWorld("game");
    public Location waitinglobby = new Location(world, 0, 3, 0);
    public int minPlayers = 2;
    public int maxPlayers = 12;
    public List<Location> flaglocations = new ArrayList<>();

    public void reloadConfigs() {
        instance.reloadConfig();
        reloadConfigYML();
        GameConfig.reloadConfig();
        reloadGameYML();
    }

    private void reloadConfigYML() {
        FileConfiguration config = instance.getConfig();
        lobby = new Location(
                instance.getServer().getWorld(config.getString("lobby.world")),
                config.getDouble("lobby.x"),
                config.getDouble("lobby.y"),
                config.getDouble("lobby.z"),
                (float) config.getDouble("lobby.yaw"),
                (float) config.getDouble("lobby.pitch")
        );
    }

    private void reloadGameYML() {
        FileConfiguration config = GameConfig.getConfig();
        this.world = instance.getServer().getWorld(config.getString("worldname"));
        this.waitinglobby = new Location(
                instance.getServer().getWorld(config.getString("waitinglobby.world")),
                config.getDouble("waitinglobby.x"),
                config.getDouble("waitinglobby.y"),
                config.getDouble("waitinglobby.z"),
                (float) config.getDouble("waitinglobby.yaw"),
                (float) config.getDouble("waitinglobby.pitch")
        );
        this.minPlayers = config.getInt("minPlayers");
        this.maxPlayers = config.getInt("maxPlayers");
        for(String s : config.getStringList("flaglocations")){
            double x = Double.parseDouble(s.split(",")[0]);
            double y = Double.parseDouble(s.split(",")[1]);
            double z = Double.parseDouble(s.split(",")[2]);
            flaglocations.add(new Location(world, x, y, z));
        }
    }

}
