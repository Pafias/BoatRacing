package me.pafias.boatracing.game.exceptions;

import org.bukkit.ChatColor;

public class WorldNotFoundException extends Throwable{

    String world;

    public WorldNotFoundException(String world) {
        this.world = world;
    }

    public WorldNotFoundException() {

    }

    @Override
    public String getMessage() {
        return ChatColor.RED + "World " + (world != null ? "\"" + world + "\"" : "") + " not found!";
    }

}
