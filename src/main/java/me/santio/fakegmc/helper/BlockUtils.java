package me.santio.fakegmc.helper;

import com.github.retrooper.packetevents.util.Vector3i;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A utility class for working with Vector3i's (from packets) and Bukkit locations
 * @author santio
 */
@UtilityClass
public class BlockUtils {
    
    /**
     * Converts a Vector3i to a Location
     * @param world The world
     * @param pos The position
     * @return The Location
     */
    public Location toLocation(World world, Vector3i pos) {
        return new Location(
            world,
            pos.x,
            pos.y,
            pos.z
        );
    }
    
}
