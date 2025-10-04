package org.nakii.mmorpg.utils;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class VectorUtils {
    public static boolean isBehind(Entity attacker, Entity victim) {
        Vector attackerDirection = attacker.getLocation().getDirection();
        Vector victimDirection = victim.getLocation().getDirection();
        return attackerDirection.dot(victimDirection) > 0;
    }
}