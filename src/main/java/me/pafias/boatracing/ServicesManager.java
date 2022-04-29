package me.pafias.boatracing;

import me.pafias.boatracing.game.GameManager;

public class ServicesManager {

    private final BoatRacing instance;

    public BoatRacing getInstance() {
        return instance;
    }

    public ServicesManager(BoatRacing plugin) {
        instance = plugin;
        variables = new Variables(plugin);
        userManager = new UserManager(plugin);
        gameManager = new GameManager(plugin);
    }

    private Variables variables;

    public Variables getVariables() {
        return variables;
    }

    private UserManager userManager;

    public UserManager getUserManager() {
        return userManager;
    }

    private GameManager gameManager;

    public GameManager getGameManager() {
        return gameManager;
    }
}
