package me.santio.fakegmc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("MissingJavadoc")
public class CreativeListener implements Listener {
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FakeCreative.remove(event.getPlayer());
    }
    
    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        FakeCreative.remove(event.getPlayer());
    }
    
}
