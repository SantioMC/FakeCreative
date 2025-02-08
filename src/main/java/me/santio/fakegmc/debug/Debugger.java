package me.santio.fakegmc.debug;

import com.github.retrooper.packetevents.event.PacketEvent;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles debugging certain packets for this plugin to allow for easier
 * understanding of what is being sent to the server.
 * @author santio
 */
@RequiredArgsConstructor
@Accessors(fluent = true, chain = true)
@Setter
@Getter
public class Debugger implements ForwardingAudience.Single {
    
    private static final Component prefix = Component.text("! ", NamedTextColor.RED);
    private static final Set<PacketTypeCommon> spamPackets = Set.of(
        PacketType.Play.Client.PLAYER_POSITION,
        PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION,
        PacketType.Play.Client.PLAYER_ROTATION,
        PacketType.Play.Client.KEEP_ALIVE,
        PacketType.Play.Client.PLAYER_INPUT,
        PacketType.Play.Client.ENTITY_ACTION,
        PacketType.Play.Client.TAB_COMPLETE,
        PacketType.Play.Client.PONG,
        PacketType.Play.Server.PING,
        PacketType.Play.Server.CHUNK_DATA,
        PacketType.Play.Server.ENTITY_POSITION_SYNC,
        PacketType.Play.Server.ENTITY_HEAD_LOOK,
        PacketType.Play.Server.UPDATE_HEALTH,
        PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION,
        PacketType.Play.Server.ENTITY_RELATIVE_MOVE,
        PacketType.Play.Server.TAB_COMPLETE,
        PacketType.Play.Server.ENTITY_VELOCITY,
        PacketType.Play.Server.TIME_UPDATE,
        PacketType.Play.Server.PLAYER_INFO_UPDATE,
        PacketType.Play.Server.UNLOAD_CHUNK,
        PacketType.Play.Server.UPDATE_VIEW_POSITION,
        PacketType.Play.Server.BUNDLE,
        PacketType.Play.Server.KEEP_ALIVE,
        PacketType.Play.Server.ENTITY_ROTATION,
        PacketType.Play.Server.ENTITY_METADATA,
        PacketType.Play.Server.UPDATE_ATTRIBUTES,
        PacketType.Play.Server.DESTROY_ENTITIES,
        PacketType.Play.Server.SPAWN_ENTITY,
        PacketType.Play.Server.ENTITY_EQUIPMENT,
        PacketType.Play.Server.ENTITY_STATUS,
        PacketType.Play.Server.DAMAGE_EVENT,
        PacketType.Play.Server.UPDATE_LIGHT,
        PacketType.Play.Server.SOUND_EFFECT
    );
    
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static Map<UUID, Debugger> debuggerCache = new HashMap<>();
    
    private final UUID player;
    private Level level = Level.NONE;
    
    public static Debugger player(UUID player) {
        return debuggerCache.computeIfAbsent(player, Debugger::new);
    }
    
    public static void remove(UUID player) {
        debuggerCache.remove(player);
    }
    
    public void debug(PacketEvent event) {
        final Player player = Bukkit.getPlayer(this.player);
        if (player == null) return;
        
        final PacketTypeCommon packetType = switch (event) {
            case PacketReceiveEvent receive -> receive.getPacketType();
            case PacketSendEvent send -> send.getPacketType();
            default -> null;
        };
        
        if (packetType == null) return;
        
        if (level.isAtLeast(Level.ALMOST_ALL)) {
            if (spamPackets.contains(packetType) && level != Level.ALL) {
                return;
            }
            
            executor.execute(() -> {
                final @Nullable Component component = PacketInspection.inspect(event, packetType, player);
                if (component == null) return;
                
                this.sendMessage(component);
            });
        }
    }
    
    @Override
    public @NotNull Audience audience() {
        final Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) return Audience.empty();
        
        return bukkitPlayer;
    }
    
    @Override
    public void sendMessage(@NotNull Component message) {
        this.audience().sendMessage(prefix.append(message));
    }
    
    @Getter
    @Accessors(fluent = true)
    public enum Level {
        ALL(3),
        ALMOST_ALL(2),
        VERBOSE(1),
        NONE(0),
        ;
        
        private final int level;
        
        Level(int level) {
            this.level = level;
        }
        
        public boolean isAtLeast(Level compare) {
            return this.level >= compare.level();
        }
    }
    
}
