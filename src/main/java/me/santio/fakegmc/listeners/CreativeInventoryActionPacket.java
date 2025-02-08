package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.google.auto.service.AutoService;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import me.santio.fakegmc.FakeCreative;
import me.santio.fakegmc.helper.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This packet is used for handling when items are spawned in from the creative inventory.
 * @author santio
 */
@AutoService(PacketListener.class)
public class CreativeInventoryActionPacket implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) return;
        
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        final WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);
        
        final var packetItemStack = ItemUtils.cleanItemStack(packet.getItemStack());
        final ItemStack item = SpigotReflectionUtil.encodeBukkitItemStack(packetItemStack);
        final int slot = ItemUtils.toSpigotSlot(packet.getSlot());
        
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
    }
    
}
