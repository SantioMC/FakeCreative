package me.santio.fakegmc.debug;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import lombok.experimental.UtilityClass;
import me.santio.fakegmc.FakeCreative;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Reads packets and any data they are holding to create a nice-looking adventure
 * component for the player to read.
 * @author santio
 */
@UtilityClass
public class PacketInspection {
    
    private final Set<String> ignored = Set.of("copy", "read", "write");
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();
    
    // This is really stupid, but I don't believe there's a way to map the packet
    private String getWrapperPacketClassName(PacketReceiveEvent event) {
        final PacketTypeCommon wrapper = event.getPacketType();
        final Enum<?> value = (Enum<?>) wrapper;
        final String group = wrapper.getClass().getName();
        
        final String namespace = group.substring(group.indexOf('$'))
            .replace("$", "");
        final String wrapperName = "Wrapper" + namespace + toPascalCase(value.name());
        
        final String wrapperGroup = group.replace("protocol.packettype.PacketType", "wrapper")
            .replace("$", ".")
            .toLowerCase();
        
        return wrapperGroup + "." + wrapperName;
    }
    
    @SuppressWarnings({"unchecked", "MethodWithMultipleReturnPoints"})
    private @Nullable PacketWrapper<?> getWrapper(PacketReceiveEvent event) {
        final String wrapperClassName = getWrapperPacketClassName(event);
        
        final Class<? extends PacketWrapper<?>> wrapperClass;
        try {
            wrapperClass = (Class<? extends PacketWrapper<?>>) Class.forName(wrapperClassName);
        } catch (ClassNotFoundException e) {
            FakeCreative.instance().getLogger().warning("Unable to find wrapper class for " + event.getPacketType() + " (" + wrapperClassName + ")");
            return null;
        }
        
        final MethodType methodType = MethodType.methodType(
            void.class,
            PacketReceiveEvent.class
        );
        
        final MethodHandle constructor;
        try {
            constructor = lookup.findConstructor(wrapperClass, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        
        try {
            return (PacketWrapper<?>) constructor.invoke(event);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Map<String, String> getData(PacketWrapper<?> wrapper) {
        // Find all getters for the wrapper
        final Method[] methods = wrapper.getClass().getDeclaredMethods();
        
        final Map<String, String> data = new HashMap<>();
        for (Method method : methods) {
            if (ignored.contains(method.getName()) || method.getParameterCount() != 0) continue;
            
            try {
                final String name = getGetterName(method.getName()).toLowerCase();
                final Object value = method.invoke(wrapper);
                data.put(name, value == null ? "null" : value.toString());
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        
        data.put("wrapper", wrapper.getClass().getSimpleName());
        return data;
    }
    
    public @Nullable Component inspect(PacketReceiveEvent event) {
        final PacketWrapper<?> wrapper = getWrapper(event);
        if (wrapper == null) return null;
        
        final Player player = event.getPlayer();
        final Map<String, String> data = getData(wrapper);
        
        Component hover = Component.newline();
        for (var entry : data.entrySet()) {
            hover = hover.append(
                Component.text(entry.getKey(), NamedTextColor.AQUA)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.text(entry.getValue(), NamedTextColor.WHITE))
                    .appendNewline()
            );
        }
        
        return Component.text(
            player.getName() + ": ",
            NamedTextColor.GRAY
        ).append(Component.text(
            toPascalCase(event.getPacketType().getName()),
            NamedTextColor.AQUA
        )).hoverEvent(HoverEvent.showText(hover));
    }
    
    /**
     * Converts snake case to pascal case
     * @param string The string in snake case
     * @return The provided string, converted to pascal case
     */
    private String toPascalCase(String string) {
        final String[] split = string.split("_");
        final StringBuilder builder = new StringBuilder();
        
        for (String part : split) {
            builder.append(part.substring(0, 1).toUpperCase());
            builder.append(part.substring(1).toLowerCase());
        }
        
        return builder.toString();
    }
    
    /**
     * Removes the "get" or "is" prefix from a method name
     * @param methodName The method name
     * @return The method name without the prefix
     */
    private String getGetterName(String methodName) {
        final char[] chars = methodName.toCharArray();
        int index = 0;
        
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                index = i;
                break;
            }
        }
        
        return methodName.substring(index);
    }

}
