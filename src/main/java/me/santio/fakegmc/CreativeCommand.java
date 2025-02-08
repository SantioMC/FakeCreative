package me.santio.fakegmc;

import me.santio.fakegmc.debug.Debugger;
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

import java.util.Arrays;
import java.util.List;

public class CreativeCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 1) {
            sender.sendMessage("Invalid arguments, use /fakecreative enable|disable [player]");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
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
                    .append(Component.text("Successfully ", NamedTextColor.GRAY))
                    .append(enable ? Component.text("enabled", NamedTextColor.GREEN) : Component.text("disabled", NamedTextColor.RED))
                    .append(Component.text(" fake creative mode for ", NamedTextColor.GRAY))
                    .append(Component.text(target.getName(), NamedTextColor.YELLOW))
            );
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Invalid arguments, use /fakecreative debug <level>",
                    NamedTextColor.RED
                ));
                return true;
            }
            
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use this command", NamedTextColor.RED));
                return true;
            }
            
            final Debugger.Level level;
            try {
                level = Debugger.Level.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Invalid level, please specify a valid level",
                    NamedTextColor.RED
                ));
                return true;
            }
            
            final Debugger debugger = Debugger.player(player.getUniqueId());
            debugger.level(level);
            
            sender.sendMessage(
                Component.empty()
                    .append(Component.text("Successfully set debug level to ", NamedTextColor.GRAY))
                    .append(Component.text(level.name(), NamedTextColor.YELLOW))
            );
        } else if (args[0].equalsIgnoreCase("reload")) {
            FakeCreative.instance().reloadConfig();
            sender.sendMessage(Component.text("Successfully reloaded config!", NamedTextColor.GREEN));
        }
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        final List<String> completions;
        
        if (args.length == 1) {
            // Hide debug since it's meant for developers
            completions = List.of("enable", "disable", "reload");
        } else if (args.length == 2) {
            switch (args[0]) {
                case "enable", "disable" -> completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
                case "debug" -> completions = Arrays.stream(Debugger.Level.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .toList();
                default -> completions = List.of();
            }
        } else completions = List.of();
        
        return completions.stream()
            .filter(it -> it.startsWith(args[args.length - 1]))
            .toList();
    }
}
