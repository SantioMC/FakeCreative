package me.santio.fakegmc;

import me.santio.fakegmc.debug.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings({"MissingJavadoc", "MethodMayBeStatic"})
public class CreativeListener implements Listener {
    
    @SuppressWarnings("FeatureEnvy")
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        FakeCreative.remove(event.getPlayer()); // Clean any existing fake creative modifications
        
        final boolean auto = FakeCreative.instance().getConfig().getBoolean("auto.on-join");
        if (!auto) return;
        
        Bukkit.getScheduler().runTaskLater(FakeCreative.instance(), () -> {
            if (FakeCreative.isCreative(event.getPlayer()) || !event.getPlayer().isOnline()) return;
            FakeCreative.apply(event.getPlayer());
        }, 2L);
    }
    
    @SuppressWarnings("FeatureEnvy")
    @EventHandler
    private void onCreativeEnter(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() != GameMode.CREATIVE) return;
        
        final boolean auto = FakeCreative.instance().getConfig().getBoolean("auto.on-creative-enter");
        if (!auto) return;
        
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(FakeCreative.instance(), () -> {
            if (FakeCreative.isCreative(event.getPlayer()) || !event.getPlayer().isOnline()) return;
            FakeCreative.apply(event.getPlayer());
        }, 2L);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FakeCreative.remove(event.getPlayer());
        Debugger.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        FakeCreative.remove(event.getPlayer());
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!FakeCreative.isCreative(player)) return;
        
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!FakeCreative.isCreative(player)) return;
        
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!FakeCreative.isCreative(player)) return;
        
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onHungerLoss(PlayerItemDamageEvent event) {
        if (!FakeCreative.isCreative(event.getPlayer())) return;
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!FakeCreative.isCreative(player)) return;
        
        if (!(event.getEntity() instanceof LivingEntity)) {
            event.getEntity().remove();
        }
    }
    
}
