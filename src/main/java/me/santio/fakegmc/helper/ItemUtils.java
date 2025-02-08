package me.santio.fakegmc.helper;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A very useful utility class for working with items, specifically with sanitization
 * @author santio
 */
@UtilityClass
public class ItemUtils {
    
    /**
     * Sanitizes an item stack by removing all components that are not really relevant, especially for
     * build servers. This is a pretty strict sanitization however it's like that to prevent issues
     * with abuse.
     * @param itemStack The packet item stack
     * @return The sanitized item stack
     */
    public com.github.retrooper.packetevents.protocol.item.ItemStack cleanItemStack(
        com.github.retrooper.packetevents.protocol.item.ItemStack itemStack
    ) {
        final var copy = itemStack.copy();
        
        copy.unsetComponent(ComponentTypes.BLOCK_ENTITY_DATA);
        copy.unsetComponent(ComponentTypes.BLOCK_STATE);
        copy.unsetComponent(ComponentTypes.CONTAINER);
        copy.unsetComponent(ComponentTypes.CONTAINER_LOOT);
        copy.unsetComponent(ComponentTypes.CUSTOM_DATA);
        copy.unsetComponent(ComponentTypes.BUNDLE_CONTENTS);
        copy.unsetComponent(ComponentTypes.ENCHANTABLE);
        copy.unsetComponent(ComponentTypes.WRITTEN_BOOK_CONTENT);
        copy.unsetComponent(ComponentTypes.WRITABLE_BOOK_CONTENT);
        copy.unsetComponent(ComponentTypes.STORED_ENCHANTMENTS);
        copy.unsetComponent(ComponentTypes.POTION_CONTENTS);
        copy.unsetComponent(ComponentTypes.ENTITY_DATA);
        copy.unsetComponent(ComponentTypes.BUCKET_ENTITY_DATA);
        copy.unsetComponent(ComponentTypes.BEES);
        
        copy.setComponent(
            ComponentTypes.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.EMPTY
        );
        
        return copy;
    }
    
    /**
     * Converts a raw slot to the spigot slot
     * @param rawSlot The raw slot from the packet
     * @return The spigot slot
     */
    @SuppressWarnings("IfStatementWithTooManyBranches")
    public int toSpigotSlot(int rawSlot) {
        if (rawSlot == -1) return -1;
        else if (rawSlot <= 8) return rawSlot + 31;
        else if (rawSlot <= 44 && rawSlot >= 36) return rawSlot - 36;
        else return rawSlot;
    }
    
    /**
     * Converts a spigot slot to the raw slot, however this is when moving from the top inventory to the
     * bottom inventory
     * @param rawSlot The raw slot from the packet
     * @return The spigot slot
     */
    public int toBottomInvSlot(int rawSlot) {
        if (rawSlot >= 27) return rawSlot - 27;
        else return rawSlot + 9;
    }
    
    /**
     * Checks if a material is an item in a hotbar
     * @param player The player to check
     * @param material The material to check
     * @return Whether the material is in the player's hotbar
     */
    @SuppressWarnings("TypeMayBeWeakened")
    public boolean isInHotbar(Player player, Material material) {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) return true;
        }
        
        return false;
    }
    
    /**
     * Get the next available hotbar slot
     * @param player The player to check
     * @return The next available hotbar slot
     */
    public int getNextHotbarSlot(Player player) {
        for (int i = 0; i < 9; i++) {
            final ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.isEmpty()) return i;
        }
        
        return -1;
    }

}
