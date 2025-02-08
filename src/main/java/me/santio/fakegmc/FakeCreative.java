package me.santio.fakegmc;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.santio.fakegmc.helper.GamemodeUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;

public class FakeCreative extends JavaPlugin {
    
    public static final Key REACH_KEY = Key.key("fakecreative", "reach");
    private static final Set<UUID> creativePlayers = new HashSet<>();
    
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }
    
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        PacketEvents.getAPI().init();
        
        // Register packet listeners
        final ServiceLoader<PacketListener> listeners = ServiceLoader.load(
            PacketListener.class,
            FakeCreative.class.getClassLoader()
        );
        
        for (PacketListener listener : listeners) {
            PacketEvents.getAPI().getEventManager().registerListener(
                listener,
                PacketListenerPriority.LOWEST
            );
        }
        
        // Register event listeners
        this.getServer().getPluginManager().registerEvents(new CreativeListener(), this);
        
        // Register command
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
    
    @SuppressWarnings({"DataFlowIssue", "removal"})
    public static void apply(Player player) {
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        
        // Send client-side creative game mode change packet
        final WrapperPlayServerChangeGameState gamemodePacket = new WrapperPlayServerChangeGameState(
            WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE,
            1.0f
        );
        
        player.setGameMode(GameMode.SURVIVAL);
        creativePlayers.add(player.getUniqueId());
        
        Bukkit.getScheduler().runTaskLater(instance(), () -> {
            player.setAllowFlight(true);
            user.sendPacket(gamemodePacket);
        }, 1L);
    }
    
    @SuppressWarnings("DataFlowIssue")
    public static void remove(Player player) {
        final User user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        
        if (user != null) {
            // Send client-side creative game mode change packet
            final WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(
                WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE,
                GamemodeUtils.toId(player.getGameMode())
            );
            
            user.sendPacket(packet);
        }
        
        // todo: Used later, needs impl
        player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).removeModifier(REACH_KEY);
        player.setAllowFlight(GamemodeUtils.hasFlight(player.getGameMode()));
        
        creativePlayers.remove(player.getUniqueId());
    }
    
    public static @NotNull Plugin instance() {
        return getPlugin(FakeCreative.class);
    }
    
}
