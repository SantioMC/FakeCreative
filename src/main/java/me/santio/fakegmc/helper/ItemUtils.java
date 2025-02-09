package me.santio.fakegmc.helper;

import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.BannerLayers;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemProfile;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPattern;
import com.github.retrooper.packetevents.protocol.item.banner.BannerPatterns;
import io.github.retrooper.packetevents.adventure.serializer.legacy.LegacyComponentSerializer;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScopedComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A very useful utility class for working with items, specifically with sanitization
 * @author santio
 */
@UtilityClass
public class ItemUtils {
    
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
    
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
        final var copy = com.github.retrooper.packetevents.protocol.item.ItemStack.builder()
            .type(itemStack.getType())
            .amount(itemStack.getAmount())
            .build();
        
        // We want to limit how far data for these items can go
        
        // Add the name
        final Component name = itemStack.getComponentOr(ComponentTypes.ITEM_NAME, Component.empty());
        final String serializedName = serializer.serialize(name);
        if (serializedName.length() <= 256) {
            copy.setComponent(ComponentTypes.ITEM_NAME, serializer.deserialize(serializedName));
        }
        
        // Add the lore
        final ItemLore lore = itemStack.getComponentOr(ComponentTypes.LORE, ItemLore.EMPTY);
        final Component rawLoreComponent = lore.getLines().stream()
            .map(serializer::serialize)
            .map(Component::text)
            .reduce(Component.empty(), ScopedComponent::append);
        
        final String serializedLoreString = serializer.serialize(rawLoreComponent);
        final List<Component> serializedLore = Arrays.stream(serializedLoreString.split("\n"))
            .map(serializer::deserialize)
            .collect(Collectors.toList());
        
        if (serializedName.length() <= 512 && lore.getLines().size() <= 10) {
            copy.setComponent(ComponentTypes.LORE, new ItemLore(serializedLore));
        }
        
        // Copy the banner patterns
        final Optional<BannerLayers> bannerPatterns = itemStack.getComponent(ComponentTypes.BANNER_PATTERNS);
        if (bannerPatterns.isPresent() && bannerPatterns.get().getLayers().size() <= 12) {
            copy.setComponent(ComponentTypes.BANNER_PATTERNS, bannerPatterns.get());
        }
        
        // Copy the profile
        final Optional<ItemProfile> optionalProfile = itemStack.getComponent(ComponentTypes.PROFILE);
        if (optionalProfile.isPresent()) {
            final ItemProfile profile = optionalProfile.get();
            final List<ItemProfile.Property> properties = new ArrayList<>();
            
            for (ItemProfile.Property entry : profile.getProperties()) {
                final String propertyName = entry.getName();
                final String propertyValue = entry.getValue();
                final String propertySignature = entry.getSignature();
                
                if (propertyName.length() >= 32 || propertyName.contains("\n")) continue;
                if (propertyValue.length() >= 64 || propertyValue.contains("\n")) continue;
                if (propertySignature != null && (propertySignature.length() >= 256 || propertySignature.contains("\n"))) continue;
                
                properties.add(new ItemProfile.Property(propertyName, propertyValue, propertySignature));
            }
            
            final ItemProfile newProfile = new ItemProfile(profile.getName(), profile.getId(), properties);
            copy.setComponent(ComponentTypes.PROFILE, newProfile);
        }
        
        // Copy over absolute minimums
        copy.setComponent(ComponentTypes.DYED_COLOR, itemStack.getComponent(ComponentTypes.DYED_COLOR));
        copy.setComponent(ComponentTypes.UNBREAKABLE, itemStack.getComponent(ComponentTypes.UNBREAKABLE));
        copy.setComponent(ComponentTypes.ENCHANTMENTS, itemStack.getComponent(ComponentTypes.ENCHANTMENTS));
        copy.setComponent(ComponentTypes.BASE_COLOR, itemStack.getComponent(ComponentTypes.BASE_COLOR));
        copy.setComponent(ComponentTypes.CUSTOM_MODEL_DATA_LISTS, itemStack.getComponent(ComponentTypes.CUSTOM_MODEL_DATA_LISTS));
        
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
