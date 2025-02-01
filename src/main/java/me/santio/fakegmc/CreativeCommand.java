package me.santio.fakegmc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreativeCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("Invalid arguments, use /fakecreative enable|disable [player]");
            return true;
        }
        
        final boolean enable = args[0].equalsIgnoreCase("enable");
        final Player target = args.length == 2 ? Bukkit.getPlayer(args[1]) : (Player) sender;
        
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return true;
        }
        
        if (enable) {
            FakeCreative.apply(target);
        } else {
            FakeCreative.remove(target);
        }
        
        sender.sendMessage(
            Component.empty()
                .append(Component.text("Successfully "))
                .append(enable ? Component.text("enabled", NamedTextColor.GREEN) : Component.text("disabled", NamedTextColor.RED))
                .append(Component.text(" fake creative mode for "))
                .append(Component.text(target.getName(), NamedTextColor.YELLOW))
        );
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final List<String> completions;
        
        if (args.length == 1) {
            completions = List.of("enable", "disable");
        } else if (args.length == 2) {
            completions = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
        } else completions = List.of();
        
        return completions.stream()
            .filter(it -> it.startsWith(args[args.length - 1]))
            .toList();
    }
}
