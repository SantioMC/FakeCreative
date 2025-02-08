package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.google.auto.service.AutoService;
import me.santio.fakegmc.FakeCreative;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This packet is used for handling when the player places a block, and making sure that it doesn't actually
 * take the item away from the player.
 * @author santio
 */
@AutoService(PacketListener.class)
public class PlayerBlockPlacementPacket implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) return;
        
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        // todo: fix race condition
        final WrapperPlayClientPlayerBlockPlacement packet = new WrapperPlayClientPlayerBlockPlacement(event);
        final ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack.isEmpty()) return;
        
        final int slot = player.getInventory().getHeldItemSlot();
        Bukkit.getScheduler().runTask(FakeCreative.instance(), () -> {
            player.getInventory().setItem(slot, itemStack);
        });
    }
    
}
