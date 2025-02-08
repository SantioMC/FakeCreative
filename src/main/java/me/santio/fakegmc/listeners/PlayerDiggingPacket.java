package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgeBlockChanges;
import com.google.auto.service.AutoService;
import me.santio.fakegmc.FakeCreative;
import me.santio.fakegmc.helper.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This packet is used for handling when the player digs, and making sure that they can insta-break blocks.
 * @author santio
 */
@AutoService(PacketListener.class)
public class PlayerDiggingPacket implements PacketListener {
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PLAYER_DIGGING) return;
        
        final Player player = event.getPlayer();
        if (player == null || !FakeCreative.isCreative(player)) return;
        
        final WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
        
        if (packet.getAction() != DiggingAction.START_DIGGING) return;
        final Location location = BlockUtils.toLocation(player.getWorld(), packet.getBlockPosition());
        
        // If the player is holding a sword, we cancel the digging
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().name().endsWith("_SWORD")) {
            event.setCancelled(true);
            player.sendBlockChange(location, location.getBlock().getBlockData());
            
            final WrapperPlayServerAcknowledgeBlockChanges acknowledgement = new WrapperPlayServerAcknowledgeBlockChanges(
                packet.getSequence()
            );
            
            event.getUser().sendPacket(acknowledgement);
            return;
        }
        
        // Update it on the server
        Bukkit.getScheduler().runTask(FakeCreative.instance(), () -> {
            location.getBlock().setType(Material.AIR);
        });
    }
}
