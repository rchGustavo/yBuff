package com.ylabui.yBuff;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuffTabComplete implements TabCompleter {

    Main plugin;

    public BuffTabComplete(Main plugin) { this.plugin = plugin; }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return suggestions;
        }

        String perm = plugin.getCustomConfig().getString("admin-permission");
        if (!(sender.isOp() || sender.hasPermission(perm))) {
            return suggestions;
        }

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("help", "add", "remove", "mode", "list", "cooldown", "particle", "reload"));
            return suggestions;
        }
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                switch (args.length) {
                    case 2:
                        for (PotionEffectType effect : PotionEffectType.values()) {
                            if (effect != null) {
                                suggestions.add(effect.getName().toLowerCase());
                            }
                        }
                        break;
                    case 3:
                        suggestions.addAll(Arrays.asList("60", "120", "360"));
                        break;
                    case 4:
                        suggestions.addAll(Arrays.asList("1", "50", "100", "255"));
                        break;
                }
                break;

            case "remove":
                if (args.length == 2) {
                    ConfigurationSection section = plugin.getConfig().getConfigurationSection("buff_effets");
                    if (section != null) {
                        for (String effectName : section.getKeys(false)) {
                            if (effectName != null) {
                                suggestions.add(effectName.toUpperCase());
                            }
                        }
                    }
                }
                break;

            case "mode":
                if (args.length == 2) {
                    suggestions.addAll(Arrays.asList("normal", "money", "xp", "item"));
                }
                break;

            case "particle":
            case "cooldown":
                if (args.length == 2) {
                    suggestions.addAll(Arrays.asList("enable", "disable"));
                } else if (subCommand.equals("cooldown") && args.length == 3 && args[1].equalsIgnoreCase("enable")) {
                    suggestions.addAll(Arrays.asList("20", "60", "120"));
                }
                break;
        }
        return suggestions;
    }
}