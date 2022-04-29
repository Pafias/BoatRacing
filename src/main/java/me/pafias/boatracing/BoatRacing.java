package me.pafias.boatracing;

import me.lucko.helper.Schedulers;
import me.pafias.boatracing.commands.BoatRacingCommand;
import me.pafias.boatracing.game.exceptions.WorldNotFoundException;
import me.pafias.boatracing.listeners.*;
import me.pafias.boatracing.utils.CC;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public final class BoatRacing extends JavaPlugin {

    private static BoatRacing instance;
    private ServicesManager servicesManager;

    public static BoatRacing getInstance() {
        return instance;
    }

    public ServicesManager getSM() {
        return servicesManager;
    }

    public static boolean auto;

    @Override
    public void onEnable() {
        instance = this;
        servicesManager = new ServicesManager(instance);
        getServer().getOnlinePlayers().forEach(p -> servicesManager.getUserManager().addUser(p));
        registerCommands();
        registerListeners();
        Schedulers.sync().runRepeating(() -> {
            if (auto && servicesManager.getGameManager().getGame() == null) {
                try {
                    servicesManager.getGameManager().createGame();
                    getServer().getConsoleSender().sendMessage(CC.translate("&aGame created!"));
                } catch (WorldNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5, TimeUnit.SECONDS, 5, TimeUnit.SECONDS);
    }

    private void registerCommands() {
        getCommand("boatracing").setExecutor(new BoatRacingCommand(instance));
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JoinAndQuitListener(instance), instance);
        pm.registerEvents(new SteerListener(instance), instance);
        pm.registerEvents(new DamageListener(instance), instance);
        pm.registerEvents(new InteractListener(instance), instance);
        pm.registerEvents(new MoveListener(instance), instance);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(instance);
        instance = null;
    }

}
