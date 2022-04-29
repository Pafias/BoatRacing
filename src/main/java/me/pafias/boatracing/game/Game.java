package me.pafias.boatracing.game;

import me.lucko.helper.Schedulers;
import me.lucko.helper.random.RandomSelector;
import me.lucko.helper.scheduler.Task;
import me.pafias.boatracing.BoatRacing;
import me.pafias.boatracing.User;
import me.pafias.boatracing.listeners.MoveListener;
import me.pafias.boatracing.utils.CC;
import me.pafias.boatracing.utils.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Game {

    public boolean started;

    private World world;
    public int minPlayers;
    public int maxPlayers;
    private int totalLaps;
    private int gameY;
    private Set<Location> spawnpoints;

    public double minX;
    public double maxX;
    public double minZ;
    public double maxZ;
    private Set<Checkpoint> checkpoints;

    private Set<User> everyone;
    private GameState state;
    private List<Boat> boats;
    private Long starttime;

    private Scoreboard gameScoreboard;
    private Objective gameObjective;
    private Team team;

    private Map<String, Task> tasks;
    private int countdown;

    public Game(World world) {
        this.world = world;
        this.everyone = new HashSet<>();
        this.minPlayers = GameConfig.getConfig().getInt("min-players");
        this.maxPlayers = GameConfig.getConfig().getInt("max-players");
        this.totalLaps = GameConfig.getConfig().getInt("total-laps");
        this.gameY = GameConfig.getConfig().getInt("game-y");
        checkpoints = loadCheckpoints();
        spawnpoints = new HashSet<>();
        for (String s : GameConfig.getConfig().getStringList("spawnpoints")) {
            double x = Double.parseDouble(s.split(",")[0]);
            double y = Double.parseDouble(s.split(",")[1]);
            double z = Double.parseDouble(s.split(",")[2]);
            float yaw = Float.parseFloat(s.split(",")[3]);
            float pitch = Float.parseFloat(s.split(",")[4]);
            spawnpoints.add(new Location(world, x, y, z, yaw, pitch));
        }
        minX = GameConfig.getConfig().getDouble("min-x");
        maxX = GameConfig.getConfig().getDouble("max-x");
        minZ = GameConfig.getConfig().getDouble("min-z");
        maxZ = GameConfig.getConfig().getDouble("max-z");
        this.state = GameState.LOBBY;
        tasks = new HashMap<>();
        boats = new ArrayList<>();
        gameScoreboard = BoatRacing.getInstance().getServer().getScoreboardManager().getNewScoreboard();
        gameObjective = gameScoreboard.registerNewObjective("BoatRacing", "dummy");
        gameObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        gameObjective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lBoatRacing"));
        team = gameScoreboard.registerNewTeam("Racers");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        MoveListener.cooldown = new ArrayList<>();
    }

    public void start(boolean force) {
        if (!started) {
            if (everyone.size() >= minPlayers || force) {
                started = true;
                setGameState(GameState.PREGAME);
                countdown = 10;
                tasks.put("start", Schedulers.sync().runRepeating(() -> {
                    if (countdown == 0) {
                        tasks.get("start").stop();
                        everyone.forEach(p -> {
                            p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F);
                            p.getPlayer().setExp(0);
                            p.getPlayer().setLevel(0);
                            p.getPlayer().setGameMode(GameMode.SURVIVAL);
                            p.getPlayer().setInvulnerable(false);
                            p.getPlayer().getActivePotionEffects().forEach(pe -> p.getPlayer().removePotionEffect(pe.getType()));
                        });
                        // handleTeleport();
                        handleBoats();
                        handleGameScoreboard();
                        handleGameTimer();
                        handleDerail();
                        setGameState(GameState.INGAME);
                        broadcast(ChatColor.GREEN + "Game started!");
                        return;
                    }
                    getPlayers().forEach(p -> {
                        p.getPlayer().setLevel(countdown);
                        p.getPlayer().setExp(countdown / (float) 10);
                    });
                    if (countdown == 10 || countdown == 5 || countdown == 4 || countdown == 3 || countdown == 2 || countdown == 1) {
                        getPlayers().forEach(p -> p.getPlayer().playSound(p.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F));
                        broadcast(ChatColor.RED + "The game starts in " + countdown + " seconds!");
                    }
                    countdown--;
                }, 1, 20));
            }
        }
    }

    private void handleTeleport(User user) {
        Location spawnpoint = RandomSelector.uniform(spawnpoints.stream().filter(l -> l.getNearbyPlayers(0.05).isEmpty()).collect(Collectors.toSet())).pick();
        user.getPlayer().teleport(spawnpoint);
        /*
        List<Location> left = new ArrayList<>(spawnpoints);
        getPlayers().forEach(u -> {
            if (u != null) {
                Schedulers.sync().runLater(() -> {
                    Location location = RandomSelector.uniform(left).pick();
                    Boat boat = (Boat) world.spawnEntity(location, EntityType.BOAT);
                    boat.setWoodType(TreeSpecies.GENERIC);
                    boat.setCustomName(u.getName() + (u.getName().endsWith("s") ? "'" : "'s") + " boat");
                    u.getPlayer().teleport(location);
                    boat.setPassenger(u.getPlayer());
                    boats.add(boat);
                    left.remove(location);
                }, 10);
            }
        });
         */
    }

    private void handleBoats() {
        getPlayers().forEach(user -> {
            Boat boat = (Boat) world.spawnEntity(user.getPlayer().getLocation(), EntityType.BOAT);
            boat.setWoodType(TreeSpecies.GENERIC);
            boat.setCustomName(user.getName() + (user.getName().endsWith("s") ? "'" : "'s") + " boat");
            boat.setPassenger(user.getPlayer());
            boats.add(boat);
        });
    }

    private void handleGameScoreboard() {
        everyone.forEach(u -> {
            Scoreboard sb = BoatRacing.getInstance().getServer().getScoreboardManager().getNewScoreboard();
            Objective o = sb.registerNewObjective("BoatRacing", "dummy");
            o.setDisplaySlot(DisplaySlot.SIDEBAR);
            o.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&e&lBoatRacing"));
            Team timeTeam = sb.registerNewTeam("SB_time");
            timeTeam.addEntry(ChatColor.WHITE.toString() + "");
            Team lapTeam = sb.registerNewTeam("SB_lap");
            lapTeam.addEntry(ChatColor.BLUE.toString() + "");
            Team placeTeam = sb.registerNewTeam("SB_place");
            placeTeam.addEntry(ChatColor.RED.toString() + "");
            u.setScoreboard(sb);
            u.setObjective(o);
        });
        tasks.put("scoreboard", Schedulers.async().runRepeating(() -> {
            everyone.forEach(u -> {
                Team timeTeam = u.getScoreboard().getTeam("SB_time");
                timeTeam.setPrefix(ChatColor.YELLOW + (u.isFinished() ? "Finish" : "Elapsed") + " time: ");
                timeTeam.setSuffix(ChatColor.GOLD + (u.isFinished() ? u.getFinishTimeFormatted() : RandomUtils.formatTime(System.currentTimeMillis() - starttime)));
                Team lapTeam = u.getScoreboard().getTeam("SB_lap");
                lapTeam.setPrefix(ChatColor.WHITE + "Lap: ");
                lapTeam.setSuffix(ChatColor.GRAY + "" + u.getLap() + ChatColor.WHITE + "/" + ChatColor.GRAY + totalLaps);
                Team placeTeam = u.getScoreboard().getTeam("SB_place");
                placeTeam.setPrefix(ChatColor.WHITE + "Place: ");
                placeTeam.setSuffix("#idk lol");
                u.getObjective().getScore("    ").setScore(7);
                u.getObjective().getScore(ChatColor.WHITE.toString() + "").setScore(6);
                u.getObjective().getScore("   ").setScore(5);
                u.getObjective().getScore(ChatColor.BLUE.toString() + "").setScore(4);
                u.getObjective().getScore("  ").setScore(3);
                u.getObjective().getScore(ChatColor.RED.toString() + "").setScore(2);
                u.getObjective().getScore("").setScore(1);
                u.getObjective().getScore(ChatColor.GRAY + "pafias" + ".tk").setScore(0);
                u.getPlayer().setScoreboard(u.getScoreboard());
            });
        }, 2, 2));

        /*

        try {
            Team timeTeam = gameScoreboard.registerNewTeam("SB_time");
            timeTeam.addEntry(ChatColor.WHITE.toString() + "");
            Team lapTeam = gameScoreboard.registerNewTeam("SB_lap");
            lapTeam.addEntry(ChatColor.BLUE.toString() + "");
            Team placeTeam = gameScoreboard.registerNewTeam("SB_place");
            placeTeam.addEntry(ChatColor.RED.toString() + "");
            tasks.put("scoreboard", Schedulers.async().runRepeating(() -> {
                everyone.forEach(u -> {
                    timeTeam.setPrefix(ChatColor.YELLOW + (u.isFinished() ? "Finish" : "Elapsed") + " time: ");
                    timeTeam.setSuffix(ChatColor.GOLD + (u.isFinished() ? u.getFinishTimeFormatted() : RandomUtils.formatTime(System.currentTimeMillis() - starttime)));
                    lapTeam.setPrefix(ChatColor.WHITE + "Lap: ");
                    lapTeam.setSuffix(ChatColor.GRAY + "" + u.getLap() + ChatColor.WHITE + "/" + ChatColor.GRAY + totalLaps);
                    placeTeam.setPrefix(ChatColor.WHITE + "Place: ");
                    placeTeam.setSuffix("#idk lol");
                    gameObjective.getScore(ChatColor.WHITE.toString() + "").setScore(6);
                    gameObjective.getScore("   ").setScore(5);
                    gameObjective.getScore(ChatColor.BLUE.toString() + "").setScore(4);
                    gameObjective.getScore("  ").setScore(3);
                    gameObjective.getScore(ChatColor.RED.toString() + "").setScore(2);
                    gameObjective.getScore("").setScore(1);
                    gameObjective.getScore(ChatColor.GRAY + "pafias" + ".tk").setScore(0);
                    u.getPlayer().setScoreboard(gameScoreboard);
                });
            }, 2, 20));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

         */
    }

    private void handleGameTimer() {
        starttime = System.currentTimeMillis();
    }

    private void handleDerail() {
        tasks.put("derail", Schedulers.sync().runRepeating(() -> {
            getPlayers().forEach(user -> {
                Location userloc = user.getPlayer().isInsideVehicle() ? user.getPlayer().getVehicle().getLocation() : user.getPlayer().getLocation();
                if (user.getLastLocation() == null) user.setLastLocation(user.getPlayer().getEyeLocation());
                if (user.getLastLocation().getY() >= gameY && !userloc.getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR))
                    user.setLastLocation(user.getPlayer().getEyeLocation());
                if (user.getPlayer().getEyeLocation().getY() < gameY) {
                    Boat boat = (Boat) world.spawnEntity(user.getLastLocation(), EntityType.BOAT);
                    boat.setWoodType(TreeSpecies.GENERIC);
                    boat.setCustomName(user.getName() + (user.getName().endsWith("s") ? "'" : "'s") + " boat");
                    user.getPlayer().teleport(user.getLastLocation());
                    boat.setPassenger(user.getPlayer());
                    boats.add(boat);
                }
            });
        }, 20, 20));
    }

    public void handleFinish(User user) {
        boolean allFinished = true;
        for (User u : getPlayers())
            if (!u.isFinished()) {
                allFinished = false;
                break;
            }
        if (allFinished) {
            setGameState(GameState.POSTGAME);
            broadcast(CC.translate("&6&lGame Ended!"));
            broadcast(CC.translate("&7&lEveryone has reached the finish line!"));
            User winner = null;
            for (User u : everyone) {
                if (winner == null) winner = u;
                if (u.getFinishTime() == null) return;
                if (u.getFinishTime() < winner.getFinishTime()) winner = u;
            }
            broadcast(CC.translate("&6&lThe winner is &b&l" + winner.getName() + " &6&lwith a time of &d&l" + winner.getFinishTimeFormatted()));
            Schedulers.sync().runLater(this::stop, 5, TimeUnit.SECONDS);
        } else {
            List<Boat> list = new ArrayList<>(boats);
            for (Boat boat : list)
                if (boat.getCustomName() != null && boat.getCustomName().contains(user.getName())) {
                    boat.remove();
                    boats.remove(boat);
                }
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
            user.getPlayer().sendMessage(CC.translate("&6You finished in &d" + user.getFinishTimeFormatted()));
            broadcast(CC.translate("&e" + user.getName() + " &6has finished!"));
        }
    }

    public void stop() {
        boats.forEach(Entity::remove);
        everyone.forEach(user -> {
            user.getPlayer().getInventory().clear();
            for (PotionEffect pe : user.getPlayer().getActivePotionEffects())
                user.getPlayer().removePotionEffect(pe.getType());
            user.getPlayer().setExp(0);
            user.getPlayer().setLevel(0);
            user.getPlayer().setGameMode(GameMode.SURVIVAL);
            user.getPlayer().setInvulnerable(false);
            user.getPlayer().showPlayer(BoatRacing.getInstance(), user.getPlayer());
            user.getPlayer().teleport(BoatRacing.getInstance().getSM().getVariables().lobby);
            user.getScoreboard().getTeams().forEach(Team::unregister);
            user.getPlayer().setScoreboard(BoatRacing.getInstance().getServer().getScoreboardManager().getNewScoreboard());
        });
        gameScoreboard.getTeams().forEach(Team::unregister);
        world.getEntities().clear();
        tasks.values().forEach(Task::stop);
        BoatRacing.getInstance().getSM().getGameManager().deleteGame(this);
    }

    public void broadcast(String message) {
        everyone.forEach(u -> u.getPlayer().sendMessage(message));
    }

    public World getWorld() {
        return world;
    }

    public Set<User> getEveryone() {
        return everyone;
    }

    public Set<User> getPlayers() {
        return everyone.stream().filter(u -> !u.getPlayer().getGameMode().equals(GameMode.SPECTATOR)).collect(Collectors.toSet());
    }

    public Set<User> getSpectators() {
        return everyone.stream().filter(u -> u.getPlayer().getGameMode().equals(GameMode.SPECTATOR)).collect(Collectors.toSet());
    }

    public GameState getGameState() {
        return state;
    }

    public void setGameState(GameState state) {
        this.state = state;
    }

    public Long getElapsedTime() {
        return System.currentTimeMillis() - starttime;
    }

    public int getTotalLaps() {
        return totalLaps;
    }

    public void addPlayer(User user) {
        if (!everyone.contains(user)) {
            everyone.add(user);
            handleTeleport(user);
            team.addPlayer(user.getPlayer());
            user.getPlayer().setHealth(user.getPlayer().getMaxHealth());
            user.getPlayer().setFoodLevel(20);
            for (PotionEffect pe : user.getPlayer().getActivePotionEffects())
                user.getPlayer().removePotionEffect(pe.getType());
            user.getCheckpoints().clear();
            start(false);
        }
    }

    public void removePlayer(User user) {
        for (Boat b : boats)
            if (b.getCustomName() != null && b.getCustomName().contains(user.getName())) {
                boats.remove(b);
                b.remove();
            }
        everyone.remove(user);
        gameScoreboard.getTeams().forEach(t -> {
            if (t.hasPlayer(user.getPlayer()))
                t.removePlayer(user.getPlayer());
        });
    }

    private Set<Checkpoint> loadCheckpoints() {
        checkpoints = new HashSet<>();
        GameConfig.getConfig().getStringList("checkpoints").forEach(s -> {
            double minX = Double.parseDouble(s.split(",")[0]);
            double maxX = Double.parseDouble(s.split(",")[1]);
            double minZ = Double.parseDouble(s.split(",")[2]);
            double maxZ = Double.parseDouble(s.split(",")[3]);
            checkpoints.add(new Checkpoint(minX, maxX, minZ, maxZ));
        });
        return checkpoints;
    }

    public Set<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

}
