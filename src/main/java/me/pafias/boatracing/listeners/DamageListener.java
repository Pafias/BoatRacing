package me.pafias.boatracing.listeners;

import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.game.Game;
import me.pafias.boatracing.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class DamageListener implements Listener {

    private final BoatRacing instance;

    public DamageListener(BoatRacing plugin) {
        instance = plugin;
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (event.getVehicle().getPassenger() instanceof Player && instance.getSM().getGameManager().isInGame((Player) event.getVehicle().getPassenger()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && instance.getSM().getGameManager().isInGame((Player) event.getEntity())) {
            Game game = instance.getSM().getGameManager().getGame();
            if (!game.getGameState().equals(GameState.INGAME))
                event.setCancelled(true);
            else
                event.setDamage(0);
        }
    }

}
