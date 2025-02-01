package me.santio.fakegmc;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemAttributeModifiers;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class CreativePacketListener implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {
            final WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);
            
            final var packetItemStack = cleanItemStack(packet.getItemStack());
            final ItemStack item = SpigotReflectionUtil.encodeBukkitItemStack(packetItemStack);
            final int slot = toSpigotSlot(packet.getSlot());
            
            if (slot == -1) {
                player.setItemOnCursor(null);
                return;
            }
            
            if (slot < 0 || slot >= 9) {
                return;
            }
            
            Bukkit.getScheduler().runTask(FakeCreative.instance(), () -> {
                player.getInventory().setItem(slot, item);
            });
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if (packet.getAction() != DiggingAction.START_DIGGING) return;
            
            final Location location = new Location(
                player.getWorld(),
                packet.getBlockPosition().x,
                packet.getBlockPosition().y,
                packet.getBlockPosition().z
            );
            
            Bukkit.getScheduler().runTask(FakeCreative.instance(), () -> {
                location.getBlock().setType(Material.AIR);
            });
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            final WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
            final ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
            if (itemStack.isEmpty()) return;
            
            final int slot = player.getInventory().getHeldItemSlot();
            Bukkit.getScheduler().runTask(FakeCreative.instance(), () -> {
                player.getInventory().setItem(slot, itemStack);
            });
        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            final WrapperPlayClientClickWindow.WindowClickType windowClickType = packet.getWindowClickType();
            final int slot = packet.getSlot();
            final InventoryView inventoryView = player.getOpenInventory();
            
            if (slot < 0) return;
            final Inventory inventory = player.getOpenInventory().getInventory(slot);
            if (inventory == null) return;
            
            final int inventorySlot = slot >= inventoryView.getTopInventory().getSize()
                ? toBottomInvSlot(slot - inventoryView.getTopInventory().getSize())
                : slot;
            
            final ItemStack item = inventory.getItem(inventorySlot);
            if (item == null || item.isEmpty()) return;
            
            if (windowClickType == WrapperPlayClientClickWindow.WindowClickType.CLONE) {
                player.setItemOnCursor(item);
            }
        }
    }
    
    private static com.github.retrooper.packetevents.protocol.item.ItemStack cleanItemStack(
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
        
        copy.setComponent(
            ComponentTypes.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.EMPTY
        );
        
        return copy;
    }
    
    @SuppressWarnings("IfStatementWithTooManyBranches")
    private static int toSpigotSlot(int rawSlot) {
        if (rawSlot == -1) return -1;
        else if (rawSlot <= 8) return rawSlot + 31;
        else if (rawSlot <= 44 && rawSlot >= 36) return rawSlot - 36;
        else return rawSlot;
    }
    
    private static int toBottomInvSlot(int rawSlot) {
        if (rawSlot >= 27) return rawSlot - 27;
        else return rawSlot + 9;
    }
    
}
