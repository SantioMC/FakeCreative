package me.santio.fakegmc;

import me.santio.fakegmc.debug.Debugger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings({"MissingJavadoc", "MethodMayBeStatic"})
public class CreativeListener implements Listener {
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        FakeCreative.remove(event.getPlayer());
        Debugger.remove(event.getPlayer().getUniqueId());
    }
    
    @EventHandler
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
