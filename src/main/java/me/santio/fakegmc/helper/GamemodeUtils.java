package me.santio.fakegmc.helper;

import lombok.experimental.UtilityClass;
import org.bukkit.GameMode;

/**
 * This class contains utility methods when working with game modes in packets
 * @author santio
 */
@UtilityClass
public class GamemodeUtils {
    
    /**
     * Converts a GameMode to the packet id of the game mode
     * @param gamemode The game mode
     * @return The packet id of the game mode
     */
    public float toId(GameMode gamemode) {
        return switch (gamemode) {
            case SURVIVAL -> 0.0f;
            case CREATIVE -> 1.0f;
            case ADVENTURE -> 2.0f;
            case SPECTATOR -> 3.0f;
        };
    }
    
}
