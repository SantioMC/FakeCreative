package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.google.auto.service.AutoService;
import me.santio.fakegmc.FakeCreative;
import me.santio.fakegmc.helper.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/**
 * Handles interactions when interacting with chests and other inventories, this is specifically for the
 * ability to clone items in the inventory.
 * @author santio
 */
@AutoService(PacketListener.class)
public class ClickWindowPacket implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CLICK_WINDOW) return;
        
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
        final WrapperPlayClientClickWindow.WindowClickType windowClickType = packet.getWindowClickType();
        final int slot = packet.getSlot();
        final InventoryView inventoryView = player.getOpenInventory();
        
        if (slot < 0) return;
        final Inventory inventory = player.getOpenInventory().getInventory(slot);
        if (inventory == null) return;
        
        final int inventorySlot = slot >= inventoryView.getTopInventory().getSize()
            ? ItemUtils.toBottomInvSlot(slot - inventoryView.getTopInventory().getSize())
            : slot;
        
        final ItemStack item = inventory.getItem(inventorySlot);
        if (item == null || item.isEmpty()) return;
        
        if (windowClickType == WrapperPlayClientClickWindow.WindowClickType.CLONE) {
            final ItemStack cloned = item.clone();
            cloned.setAmount(item.getMaxStackSize());
            player.setItemOnCursor(cloned);
        }
    }
    
}
