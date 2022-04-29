package me.pafias.boatracing.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.game.Game;
import me.pafias.boatracing.game.GameState;
import net.minecraft.server.v1_16_R3.PacketPlayInSteerVehicle;
import org.bukkit.event.Listener;

public class SteerListener implements Listener {

    private final BoatRacing instance;

    float forward, side;
    boolean space, shift;

    public SteerListener(BoatRacing plugin) {
        instance = plugin;
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(instance, PacketType.Play.Client.STEER_VEHICLE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Game game = instance.getSM().getGameManager().getGame();
                if (game == null) return;
                PacketPlayInSteerVehicle packet = (PacketPlayInSteerVehicle) event.getPacket().getHandle();
                forward = packet.c();
                side = packet.b();
                space = packet.d();
                shift = packet.e();
                if (!game.getGameState().equals(GameState.INGAME) && !game.getGameState().equals(GameState.POSTGAME)) {
                    event.setCancelled(true);
                    return;
                }
                if (goingBackwards()) {
                    event.setCancelled(true);
                    // event.getPlayer().sendMessage(ChatColor.RED + "You cannot go backwards!");
                    return;
                }
                if (shift()) {
                    event.setCancelled(true);
                    // event.getPlayer().sendMessage(ChatColor.RED + "You cannot leave your kart!");
                    return;
                }
            }
        });
    }

    private boolean goingForward() {
        return forward > 0;
    }

    private boolean goingBackwards() {
        return forward < 0;
    }

    private boolean goingRight() {
        return side < 0;
    }

    private boolean goingLeft() {
        return side > 0;
    }

    private boolean jumping() {
        return space;
    }

    private boolean shift() {
        return shift;
    }

}
