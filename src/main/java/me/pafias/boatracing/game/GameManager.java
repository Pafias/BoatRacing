package me.pafias.boatracing.game;

import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.User;
import me.pafias.boatracing.game.exceptions.GameFullException;
import me.pafias.boatracing.game.exceptions.GameNotFoundException;
import me.pafias.boatracing.game.exceptions.WorldNotFoundException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class GameManager {

    private final BoatRacing instance;
    private final Set<Game> games = new HashSet<>();

    public GameManager(BoatRacing plugin) {
        this.instance = plugin;
    }

    public Set<Game> getGames() {
        return games;
    }

    public Game createGame() throws WorldNotFoundException {
        if (!games.isEmpty())
            return null;
        World world = instance.getSM().getVariables().world;
        if (world == null)
            throw new WorldNotFoundException();
        Game game = new Game(world);
        games.add(game);
        instance.getServer().getLogger().log(Level.INFO, ChatColor.GREEN + "Game created!");
        return game;
    }

    public void deleteGame(Game game) {
        games.remove(game);
    }

    public Game getGame(User user) {
        return games.stream().filter(oorlog -> oorlog.getEveryone().contains(user)).findAny().orElse(null);
    }

    public Game getGame() {
        return games.stream().findAny().orElse(null);
    }

    public void addPlayer(User user) throws GameNotFoundException, GameFullException {
        Game game = getGame();
        if (game == null) throw new GameNotFoundException();
        if (game.getPlayers().size() >= game.maxPlayers)
            throw new GameFullException();
        getGame().addPlayer(user);
    }

    public boolean isInGame(User user) {
        return getGame(user) != null;
    }

    public boolean isInSameGame(User user1, User user2) {
        return isInGame(user1) && isInGame(user2) && getGame(user1) == getGame(user2);
    }

    public void removePlayer(User user) {
        getGame().removePlayer(user);
        user.getPlayer().teleport(instance.getSM().getVariables().lobby);
    }

    public boolean isInGame(Player player) {
        if (getGame() == null) return false;
        return getGame().getEveryone().contains(instance.getSM().getUserManager().getUser(player));
    }

}
