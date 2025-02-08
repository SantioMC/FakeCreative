package me.santio.fakegmc.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.google.auto.service.AutoService;
import me.santio.fakegmc.debug.Debugger;
import org.bukkit.entity.Player;

@AutoService(PacketListener.class)
public class DebuggerListener implements PacketListener {
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE
            || event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE
            || event.getPacketType() == PacketType.Play.Server.EFFECT) return;
        
        final Player player = event.getPlayer();
        if (player == null) return;
        
        final Debugger debugger = Debugger.player(player.getUniqueId());
        debugger.debug(event.clone());
    }
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final Player player = event.getPlayer();
        if (player == null) return;
        
        final Debugger debugger = Debugger.player(player.getUniqueId());
        if (event.getPacketType() != PacketType.Play.Client.CLIENT_TICK_END) {
            debugger.debug(event.clone());
        }
    }
    
}
