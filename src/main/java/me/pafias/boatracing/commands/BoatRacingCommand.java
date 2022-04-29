package me.pafias.boatracing.commands;

import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.User;
import me.pafias.boatracing.game.Game;
import me.pafias.boatracing.game.exceptions.GameFullException;
import me.pafias.boatracing.game.exceptions.GameNotFoundException;
import me.pafias.boatracing.game.exceptions.WorldNotFoundException;
import me.pafias.boatracing.utils.CC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class BoatRacingCommand implements CommandExecutor {

    private final BoatRacing instance;

    public BoatRacingCommand(BoatRacing plugin) {
        this.instance = plugin;
    }

    private boolean help(CommandSender sender, String label) {
        sender.sendMessage(CC.translate("&f--------------- &6BoatRacing &f---------------"));
        sender.sendMessage(CC.translate("&7/" + label + " create"));
        sender.sendMessage(CC.translate("&7/" + label + " auto"));
        sender.sendMessage(CC.translate("&7/" + label + " join"));
        sender.sendMessage(CC.translate("&7/" + label + " addplayer <player/*>"));
        sender.sendMessage(CC.translate("&7/" + label + " removeplayer <player/*>"));
        sender.sendMessage(CC.translate("&7/" + label + " addteam <team>"));
        sender.sendMessage(CC.translate("&7/" + label + " removeteam <team>"));
        sender.sendMessage(CC.translate("&7/" + label + " forcestart"));
        sender.sendMessage(CC.translate("&7/" + label + " leave"));
        sender.sendMessage(CC.translate("&7/" + label + " stop"));
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return help(sender, label);
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("forcestart") && sender.isOp()) {
                Game game = instance.getSM().getGameManager().getGame();
                if (game == null) {
                    sender.sendMessage(ChatColor.RED + "There is no game started.");
                    return true;
                }
                game.start(true);
                sender.sendMessage(ChatColor.GREEN + "You forcestarted the game.");
            } else if (args[0].equalsIgnoreCase("stop") && sender.isOp()) {
                Game game = instance.getSM().getGameManager().getGame();
                if (game == null) {
                    sender.sendMessage(CC.translate("&cThere is currently no game going on."));
                    return true;
                }
                game.stop();
                sender.sendMessage(ChatColor.GOLD + "Game stopped.");
            } else if (args[0].equalsIgnoreCase("leave")) {
                User user = instance.getSM().getUserManager().getUser((Player) sender);
                Game game = instance.getSM().getGameManager().getGame();
                game.removePlayer(user);
                sender.sendMessage(ChatColor.GREEN + "You are out the game.");
            } else if (args[0].equalsIgnoreCase("create") && sender.isOp()) {
                try {
                    instance.getSM().getGameManager().createGame();
                    sender.sendMessage(ChatColor.GREEN + "Game created!");
                } catch (WorldNotFoundException e) {
                    sender.sendMessage(ChatColor.RED + "Game world not found!");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("join")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(CC.translate("&cOnly players!"));
                    return true;
                }
                User user = instance.getSM().getUserManager().getUser((Player) sender);
                try {
                    instance.getSM().getGameManager().addPlayer(user);
                    user.getPlayer().sendMessage(CC.translate("&aYou joined the game!"));
                } catch (GameNotFoundException e) {
                    user.getPlayer().sendMessage(CC.translate("&cThere is currently no game available. Please wait a few seconds and try again."));
                } catch (GameFullException e) {
                    user.getPlayer().sendMessage(CC.translate("&cThe game is already full! ;("));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("auto") && sender.isOp()) {
                BoatRacing.auto = !BoatRacing.auto;
                sender.sendMessage(CC.translate("&6Automatic game creation: " + (BoatRacing.auto ? "&aON" : "&cOFF")));
            }
            return true;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("addplayer")) {
                if (args[1].equalsIgnoreCase("*")) {
                    for (User u : instance.getSM().getUserManager().getUsers()) {
                        try {
                            instance.getSM().getGameManager().addPlayer(u);
                        } catch (GameNotFoundException e) {
                            sender.sendMessage(e.getMessage());
                            return true;
                        } catch (GameFullException e) {
                            sender.sendMessage(CC.translate("&cMax players for this game reached. Some players may not be added."));
                            break;
                        }
                        u.getPlayer().sendMessage(ChatColor.GREEN + "You are in the game!");
                    }
                    sender.sendMessage(ChatColor.GREEN + "Players added");
                    return true;
                }
                User user = instance.getSM().getUserManager().getUser(args[1]);
                try {
                    instance.getSM().getGameManager().addPlayer(user);
                } catch (GameNotFoundException | GameFullException e) {
                    sender.sendMessage(e.getMessage());
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Player added");
                user.getPlayer().sendMessage(ChatColor.GREEN + "You are in the game!");
            } else if (args[0].equalsIgnoreCase("removeplayer")) {
                if (args[1].equalsIgnoreCase("*")) {
                    instance.getSM().getUserManager().getUsers().forEach(user -> {
                        instance.getSM().getGameManager().removePlayer(user);
                        user.getPlayer().sendMessage(ChatColor.RED + "You are no longer in the game!");
                    });
                    sender.sendMessage(ChatColor.GREEN + "Players removed");
                    return true;
                }
                User user = instance.getSM().getUserManager().getUser(args[1]);
                instance.getSM().getGameManager().removePlayer(user);
                sender.sendMessage(ChatColor.GREEN + "Player removed");
                user.getPlayer().sendMessage(ChatColor.RED + "You are no longer in the game!");
            } else if (args[0].equalsIgnoreCase("addteam")) {
                Team team = instance.getServer().getScoreboardManager().getMainScoreboard().getTeam(args[1]);
                if (team == null) {
                    sender.sendMessage(ChatColor.RED + "Team not found!");
                    return true;
                }
                team.getPlayers().forEach(p -> {
                    if (p != null && p.isOnline()) {
                        try {
                            instance.getSM().getGameManager().addPlayer(instance.getSM().getUserManager().getUser(p.getPlayer()));
                        } catch (GameNotFoundException | GameFullException e) {
                            sender.sendMessage(e.getMessage());
                            return;
                        }
                    }
                });
                team.getEntries().forEach(e -> {
                    User user = instance.getSM().getUserManager().getUser(e);
                    if (user != null) {
                        try {
                            instance.getSM().getGameManager().addPlayer(user);
                        } catch (GameNotFoundException | GameFullException ex) {
                            sender.sendMessage(ex.getMessage());
                            return;
                        }
                    }
                });
                sender.sendMessage(ChatColor.GREEN + "Team added.");
            } else if (args[0].equalsIgnoreCase("removeteam")) {
                Team team = instance.getServer().getScoreboardManager().getMainScoreboard().getTeam(args[1]);
                if (team == null) {
                    sender.sendMessage(ChatColor.RED + "Team not found!");
                    return true;
                }
                team.getPlayers().forEach(p -> {
                    if (p != null && p.isOnline())
                        instance.getSM().getGameManager().removePlayer(instance.getSM().getUserManager().getUser(p.getPlayer()));
                });
                team.getEntries().forEach(e -> {
                    User user = instance.getSM().getUserManager().getUser(e);
                    if (user != null)
                        instance.getSM().getGameManager().removePlayer(user);
                });
                sender.sendMessage(ChatColor.GREEN + "Team removed.");
            }
            return true;
        } else return help(sender, label);
    }

}
