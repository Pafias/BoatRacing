package me.pafias.boatracing.utils;

import org.bukkit.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomUtils {

    public static boolean isInBetween(Location loc, double minX, double maxX, double minZ, double maxZ) {
        return loc.getX() > minX && loc.getX() < maxX && loc.getZ() > minZ && loc.getZ() < maxZ;
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("mm:ss:SS").format(new Date(millis));
    }

    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }


}
