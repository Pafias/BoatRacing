package me.pafias.boatracing.listeners;

import me.lucko.helper.Schedulers;
import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.User;
import me.pafias.boatracing.game.Checkpoint;
import me.pafias.boatracing.game.Game;
import me.pafias.boatracing.game.GameState;
import me.pafias.boatracing.utils.CC;
import me.pafias.boatracing.utils.RandomUtils;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MoveListener implements Listener {

    private final BoatRacing instance;

    public MoveListener(BoatRacing plugin) {
        instance = plugin;
        /*
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(instance, PacketType.Play.Client.VEHICLE_MOVE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketPlayInVehicleMove packet = (PacketPlayInVehicleMove) event.getPacket().getHandle();
                if (instance.getSM().getGameManager().isInGame(event.getPlayer())) {
                    Game game = instance.getSM().getGameManager().getGame();
                    if (game == null) return;
                    List<Location> list = instance.getSM().getVariables().flaglocations;
                    boolean b = false;
                    for (Location l : list)
                        if ((int) l.getX() == (int) packet.getX() && (int) l.getZ() == (int) packet.getZ())
                            b = true;
                    if (b) {
                        User user = instance.getSM().getUserManager().getUser(event.getPlayer());
                        user.setLap(user.getLap() + 1);
                        if (user.getLap() >= game.getTotalLaps()) {
                            user.setFinishTime(game.getElapsedTime());
                        }
                    }
                }
            }
        });
         */
    }

    @EventHandler
    public void onPMove(PlayerMoveEvent event) {
        User user = instance.getSM().getUserManager().getUser(event.getPlayer());
        if (user == null) return;
        Game game = instance.getSM().getGameManager().getGame();
        if (game == null) return;
        if (!game.getPlayers().contains(user)) return;
        if (!game.getGameState().equals(GameState.INGAME) && !game.getGameState().equals(GameState.POSTGAME))
            if (event.getTo().getBlockX() != event.getFrom().getBlockX() || event.getTo().getBlockZ() != event.getFrom().getBlockZ())
                event.setTo(event.getFrom());
    }

    public static List<UUID> cooldown = new ArrayList<>();

    @EventHandler
    public void onMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Boat && !event.getVehicle().getPassengers().isEmpty() && event.getVehicle().getPassengers().get(0) instanceof Player && instance.getSM().getGameManager().isInGame((Player) event.getVehicle().getPassengers().get(0))) {
            Game game = instance.getSM().getGameManager().getGame();
            if (!game.getGameState().equals(GameState.INGAME)) {
                event.getVehicle().teleport(event.getFrom());
                return;
            }
            if (RandomUtils.isInBetween(event.getVehicle().getLocation(), game.minX, game.maxX, game.minZ, game.maxZ)) {
                User user = instance.getSM().getUserManager().getUser((Player) event.getVehicle().getPassengers().get(0));
                if (cooldown.contains(user.getUUID())) return;
                if (user.getCheckpoints().size() >= game.getCheckpoints().size()) {
                    if (user.getLap() >= game.getTotalLaps()) {
                        user.setFinishTime(game.getElapsedTime());
                        game.handleFinish(user);
                        return;
                    }
                    user.setLap(user.getLap() + 1);
                    user.getPlayer().sendTitle(CC.translate("&6&lLAP &b&l" + user.getLap()), "");
                    user.getCheckpoints().clear();
                    Schedulers.sync().runLater(() -> cooldown.remove(user.getUUID()), 5, TimeUnit.SECONDS);
                }
            }
            Checkpoint checkpoint = goingThroughCheckpoint(event.getVehicle(), game);
            if (checkpoint != null) {
                User user = instance.getSM().getUserManager().getUser((Player) event.getVehicle().getPassengers().get(0));
                user.addCheckpoint(checkpoint);
            }
        }
    }

    private Checkpoint goingThroughCheckpoint(Vehicle boat, Game game) {
        for (Checkpoint c : game.getCheckpoints())
            if (RandomUtils.isInBetween(boat.getLocation(), c.minX(), c.maxX(), c.minZ(), c.maxZ()))
                return c;
        return null;
    }

}
