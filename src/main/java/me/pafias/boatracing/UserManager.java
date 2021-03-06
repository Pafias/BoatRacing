package me.pafias.boatracing;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager {

    private BoatRacing instance;
    private Set<User> users = new HashSet<>();

    public UserManager(BoatRacing plugin) {
        instance = plugin;
    }

    public Set<User> getUsers(){
        return users;
    }

    public User getUser(UUID uuid) {
        return users.stream().filter(u -> u.getUUID().equals(uuid)).findAny().orElse(null);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public User getUser(String name) {
        return users.stream().filter(u -> u.getName().equals(name)).findAny().orElse(null);
    }

    public void addUser(Player player) {
        users.add(new User(player));
    }

    public void removeUser(Player player) {
        if (instance.getSM().getGameManager().getGame() != null)
            instance.getSM().getGameManager().getGame().removePlayer(getUser(player));
        users.remove(getUser(player));
    }

}
