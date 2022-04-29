package me.pafias.boatracing.listeners;

import me.pafias.boatracing.BoatRacing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class InteractListener implements Listener {

    private final BoatRacing instance;

    public InteractListener(BoatRacing plugin) {
        instance = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (instance.getSM().getGameManager().isInGame(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (instance.getSM().getGameManager().isInGame(event.getPlayer()))
            event.setCancelled(true);
    }

}
