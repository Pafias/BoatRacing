package me.pafias.boatracing;

import me.pafias.boatracing.game.Checkpoint;
import me.pafias.boatracing.utils.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final Player player;

    private int lap;
    private Long finishTime;
    private Location lastLocation;
    private boolean finished;
    private Set<Checkpoint> checkpoints;

    private Scoreboard scoreboard;
    private Objective objective;

    public User(Player player) {
        this.player = player;
        finished = false;
        lap = 1;
        checkpoints = new HashSet<>();
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUUID() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
    }

    public int getLap() {
        return lap;
    }

    public void setLap(int lap) {
        this.lap = lap;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public String getFinishTimeFormatted() {
        // return new SimpleDateFormat("mm:ss:SS").format(new Date(finishTime));
        return RandomUtils.formatTime(finishTime);
    }

    public void setFinishTime(Long time) {
        this.finishTime = time;
        finished = true;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location location) {
        this.lastLocation = location;
    }

    public boolean isFinished() {
        return finished;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public Objective getObjective() {
        return objective;
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public Set<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoint(Checkpoint checkpoint) {
        checkpoints.add(checkpoint);
    }

}
