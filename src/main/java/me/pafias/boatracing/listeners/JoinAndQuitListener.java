package me.pafias.boatracing.listeners;

import me.lucko.helper.Schedulers;
import me.pafias.boatracing.BoatRacing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndQuitListener implements Listener {

    private final BoatRacing instance;

    public JoinAndQuitListener(BoatRacing plugin) {
        this.instance = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Schedulers.sync().run(() -> {
            instance.getSM().getUserManager().addUser(event.getPlayer());
        }).thenRunSync(() -> {

        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        instance.getSM().getUserManager().removeUser(event.getPlayer());
    }

}
