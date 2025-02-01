package me.santio.fakegmc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FakeCreative extends JavaPlugin {
    
    private static final Set<UUID> creativePlayers = new HashSet<>();
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        
        PacketEvents.getAPI().getEventManager().registerListener(
            new CreativePacketListener(),
            PacketListenerPriority.LOWEST
        );
        
        this.getServer().getPluginManager().registerEvents(new CreativeListener(), this);
        
        final CreativeCommand command = new CreativeCommand();
        this.getServer().getPluginCommand("fakecreative").setExecutor(command);
        this.getServer().getPluginCommand("fakecreative").setTabCompleter(command);
    }
    
    @Override
    public void onDisable() {
        creativePlayers.clear();
        PacketEvents.getAPI().terminate();
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    public static boolean isCreative(Player player) {
        return creativePlayers.contains(player.getUniqueId());
    }
    
    public static void apply(Player player) {
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        
        // Send client-side creative game mode change packet
        final WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(
            WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE,
            1.0f
        );
        
        player.setAllowFlight(true);
        player.setGameMode(GameMode.SURVIVAL);
        
        creativePlayers.add(player.getUniqueId());
        user.sendPacket(packet);
    }
    
    public static void remove(Player player) {
        if (player.isOnline()) {
            final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
            
            // Send client-side creative game mode change packet
            final WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(
                WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE,
                GamemodeMapping.fromBukkit(player.getGameMode()).id()
            );
            
            user.sendPacket(packet);
        }
        
        player.setAllowFlight(false);
        creativePlayers.remove(player.getUniqueId());
    }
    
    public static @NotNull Plugin instance() {
        return getPlugin(FakeCreative.class);
    }
    
}
