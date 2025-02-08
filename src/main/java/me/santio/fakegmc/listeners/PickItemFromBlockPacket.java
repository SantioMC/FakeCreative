package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPickItemFromBlock;
import com.google.auto.service.AutoService;
import me.santio.fakegmc.FakeCreative;
import me.santio.fakegmc.helper.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This packet is used for handling when items are spawned in when the player does a pickup block action
 * @author santio
 */
@AutoService(PacketListener.class)
public class PickItemFromBlockPacket implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PICK_ITEM_FROM_BLOCK) return;
        
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        final WrapperPlayClientPickItemFromBlock packet = new WrapperPlayClientPickItemFromBlock(event);
        
        final Location location = new Location(
            player.getWorld(),
            packet.getBlockPos().x,
            packet.getBlockPos().y,
            packet.getBlockPos().z
        );
        
        final Material material = location.getBlock().getType();
        if (ItemUtils.isInHotbar(player, material)) return; // No need, the player will switch to the item
        
        // Get the block item
        this.swapItem(player, material);
    }
    
    private void swapItem(Player player, Material material) {
        ItemStack previousItem;
        ItemStack setItem = new ItemStack(material);
        
        // See if we have the item already in the inventory
        final int existingSlot = player.getInventory().first(material);
        if (existingSlot != -1) {
            setItem = player.getInventory().getItem(existingSlot);
            player.getInventory().setItem(existingSlot, null);
        }
        
        // Check if we have a hotbar slot available
        final int slot = ItemUtils.getNextHotbarSlot(player);
        if (slot == -1) {
            // No hotbar slot available, swap the item in the main hand
            previousItem = player.getInventory().getItemInMainHand();
            player.getInventory().setItemInMainHand(setItem);
        } else {
            // Use the hotbar slot
            previousItem = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, setItem);
        }
        
        if (previousItem == null || previousItem.isEmpty()) return;
        player.getInventory().addItem(previousItem);
    }
    
}
