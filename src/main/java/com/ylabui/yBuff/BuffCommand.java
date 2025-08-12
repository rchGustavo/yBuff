package com.ylabui.yBuff;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuffCommand implements CommandExecutor {

    private Main plugin;

    public BuffCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        FileConfiguration cfg = plugin.getConfig();
        if (args.length == 0) {
            String cldmode = cfg.getString("cooldown_mode");
            if (cldmode.equalsIgnoreCase("disable")) {
                String mode = cfg.getString("mode");
                if (mode != null) {
                    switch (mode.toLowerCase()) {
                        case "normal":
                            applyEffects(p);
                            break;
                        case "money":
                            p.sendMessage(plugin.getString("messages.developing"));
                            break;
                        case "xp":
                            int xpCust = cfg.getInt("custs.xp");
                            if (p.getLevel() >= xpCust) {
                                p.setLevel(p.getLevel() - xpCust);
                                applyEffects(p);
                            } else {
                                int needXp = xpCust - p.getLevel();
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("xp_need", String.valueOf(needXp));
                                p.sendMessage(plugin.getString("messages.not_have_xp", placeholders));
                            }
                            break;
                        case "item":
                            String itemName = cfg.getString("custs.item.name").toUpperCase();
                            int itemAmount = cfg.getInt("custs.item.amount");
                            ItemStack item = new ItemStack(Material.getMaterial(itemName), itemAmount);
                            if (p.getInventory().containsAtLeast(item, itemAmount)) {
                                p.getInventory().removeItem(item);
                                applyEffects(p);
                            } else {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("item_name", itemName.toLowerCase());
                                placeholders.put("item_amount", String.valueOf(itemAmount));
                                p.sendMessage(plugin.getString("messages.not_have_item", placeholders));
                            }
                            break;
                        default:
                            p.sendMessage("A");
                            break;
                    }
                }
            } else if (cldmode.equalsIgnoreCase("enable")) {
                if (isInCooldown(p)) {
                    return true;
                }
                String mode = cfg.getString("mode");
                if (mode != null) {
                    switch (mode.toLowerCase()) {
                        case "normal":
                            applyEffects(p);
                            setCooldown(p);
                            break;
                        case "money":
                            p.sendMessage(plugin.getString("messages.developing"));
                            break;
                        case "xp":
                            int xpCust = cfg.getInt("custs.xp");
                            if (p.getLevel() >= xpCust) {
                                p.setLevel(p.getLevel() - xpCust);
                                applyEffects(p);
                                setCooldown(p);
                            } else {
                                int needXp = xpCust - p.getLevel();
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("xp_need", String.valueOf(needXp));
                                p.sendMessage(plugin.getString("messages.not_have_xp", placeholders));
                            }
                            break;
                        case "item":
                            String itemName = cfg.getString("custs.item.name").toUpperCase();
                            int itemAmount = cfg.getInt("custs.item.amount");
                            ItemStack item = new ItemStack(Material.getMaterial(itemName), itemAmount);
                            if (p.getInventory().containsAtLeast(item, itemAmount)) {
                                p.getInventory().removeItem(item);
                                applyEffects(p);
                                setCooldown(p);
                            } else {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("item_name", itemName.toLowerCase());
                                placeholders.put("item_amount", String.valueOf(itemAmount));
                                p.sendMessage(plugin.getString("messages.not_have_item", placeholders));
                            }
                            break;
                        default:
                            p.sendMessage("A");
                            break;
                    }
                }
            }
            return true;
        }

        String perm = plugin.getString("admin-permission");
        if (!sender.isOp() && !sender.hasPermission(perm) || !(p instanceof Player)) {
            sender.sendMessage(plugin.getString("messages.not_permission"));
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            p.sendMessage("§d== Buff Commands: ==");
            p.sendMessage("§d - /buff");
            p.sendMessage("§d - /buff add [effect name] [time] [amplification]");
            p.sendMessage("§d - /buff remove [effect name]");
            p.sendMessage("§d - /buff list");
            p.sendMessage("§d - /buff mode [normal | money | xp | item]");
            p.sendMessage("§d - /buff particle [enable/disable]");
            p.sendMessage("§d - /buff cooldown [enable/disable] [time]");
            p.sendMessage("§d - /buff reload");
            return true;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("add")) {
            String effectName = args[1].toUpperCase();
            String configPath = "buff_effets." + effectName;

            ConfigurationSection buffEffects = cfg.getConfigurationSection("buff_effets");

            if (buffEffects != null && buffEffects.contains(effectName)) {
                p.sendMessage(plugin.getString("messages.error_add_effect"));
                return true;
            }

            try {
                int duration = Integer.parseInt(args[2]);
                int amplifier = Integer.parseInt(args[3]);

                cfg.set(configPath + ".time", duration);
                cfg.set(configPath + ".amplification", amplifier);

                plugin.saveConfig();
                plugin.reloadConfigFile();

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("effect", effectName);
                p.sendMessage(plugin.getString("messages.add_effect", placeholders));

            } catch (NumberFormatException e) {
                p.sendMessage(plugin.getString("messages.error_effect"));
            }
            return true;

        } else if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
            p.sendMessage(plugin.getString("messages.use_add"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String effectName = args[1].toUpperCase();
            String configPath = "buff_effets." + effectName;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("effect", effectName);

            if (cfg.contains(configPath)) {
                cfg.set(configPath, null);
                plugin.saveConfig();
                plugin.reloadConfigFile();

                p.sendMessage(plugin.getString("messages.remove_effect", placeholders));
            } else {
                p.sendMessage(plugin.getString("messages.not_found_effect", placeholders));
            }

            return true;

        } else if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
            p.sendMessage(plugin.getString("messages.use_remove"));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            ConfigurationSection section = cfg.getConfigurationSection("buff_effets");

            if (section == null || section.getKeys(false).isEmpty()) {
                p.sendMessage(plugin.getString("messages.not_list"));
                return true;
            }

            p.sendMessage(plugin.getString("messages.title_list"));

            for (String effectName : section.getKeys(false)) {
                int time = cfg.getInt("buff_effets." + effectName + ".time");
                int amplification = cfg.getInt("buff_effets." + effectName + ".amplification");
                String formattime = formatTime(time);

                Map<String, String> placeholders = Map.of(
                        "effect", effectName,
                        "time", formattime,
                        "amplification", String.valueOf(amplification)
                );

                p.sendMessage(plugin.getString("messages.show_list", placeholders));
            }
            return true;

        } else if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            p.sendMessage(plugin.getString("messages.use_list"));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
            String inputMode = args[1].toLowerCase();
            List<String> validModes = Arrays.asList("normal", "money", "xp", "item");

            if (!validModes.contains(inputMode)) {
                p.sendMessage(plugin.getString("messages.error_set_mode"));
                return true;
            }

            String currentMode = cfg.getString("mode");
            if (currentMode != null && currentMode.equalsIgnoreCase(inputMode)) {
                p.sendMessage(plugin.getString("messages.is_set_mode"));
                return true;
            }

            cfg.set("mode", inputMode);
            plugin.saveConfig();

            Map<String, String> placeholders = Map.of("mode", inputMode);
            p.sendMessage(plugin.getString("messages.set_mode", placeholders));
            return true;

        } else if (args[0].equalsIgnoreCase("mode")) {
            p.sendMessage(plugin.getString("messages.use_mode"));
            return true;
        }


        if (args.length == 2 && args[0].equalsIgnoreCase("particle")) {
            String input = args[1].toLowerCase();

            if (!input.equals("enable") && !input.equals("disable")) {
                p.sendMessage(plugin.getString("messages.error_set_mode"));
                return true;
            }

            String current = cfg.getString("show_particle");
            if (current != null && current.equalsIgnoreCase(input)) {
                p.sendMessage(plugin.getString("messages.is_set_mode"));
                return true;
            }

            String chosen = input.equals("enable") ? "&aAtivado" : "&cDesativado";
            Map<String, String> placeholders = Map.of("particle_mode", chosen);
            p.sendMessage(plugin.getString("messages.set_particle", placeholders));

            cfg.set("show_particle", input);
            plugin.saveConfig();
            return true;

        } else if (args[0].equalsIgnoreCase("particle")) {
            p.sendMessage(plugin.getString("messages.use_particle"));
            return true;
        }

        if (args[0].equalsIgnoreCase("cooldown")) {
            String modeArg = args.length >= 2 ? args[1].toLowerCase() : "";
            String currentMode = cfg.getString("cooldown_mode");

            if (args.length == 3 && modeArg.equals("enable")) {
                int cooldownTime;
                try {
                    cooldownTime = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(plugin.getString("messages.error_set_mode"));
                    return true;
                }

                if (currentMode != null && currentMode.equalsIgnoreCase(modeArg)) {
                    p.sendMessage(plugin.getString("messages.is_set_mode"));
                    return true;
                }

                cfg.set("cooldown_mode", modeArg);
                cfg.set("cooldown_time", cooldownTime);
                plugin.saveConfig();

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", formatTime(cooldownTime));
                p.sendMessage(plugin.getString("messages.enable_cooldown", placeholders));
                return true;

            } else if (args.length == 2 && modeArg.equals("disable")) {
                if (currentMode != null && currentMode.equalsIgnoreCase(modeArg)) {
                    p.sendMessage(plugin.getString("messages.is_set_mode"));
                    return true;
                }

                cfg.set("cooldown_mode", modeArg);
                plugin.saveConfig();
                p.sendMessage(plugin.getString("messages.disable_cooldown"));
                return true;

            } else {
                p.sendMessage(plugin.getString("messages.error_set_mode"));
                return true;
            }
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigFile();
            p.sendMessage(plugin.getString("messages.reload_sucess"));
            return true;
        } else {
            p.sendMessage(plugin.getString("messages.invalid_usage"));
            return true;
        }
    }

    public void applyEffects(Player p) {
        FileConfiguration cfg = plugin.getConfig();
        ConfigurationSection section = cfg.getConfigurationSection("buff_effets");
        boolean showParticles = cfg.getString("show_particle", "enable").equalsIgnoreCase("enable");
        if (section != null) {
            for (String effectName : section.getKeys(false)) {
                int time = cfg.getInt("buff_effets." + effectName + ".time");
                int amp = cfg.getInt("buff_effets." + effectName + ".amplification");
                PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
                if (type != null) {
                    PotionEffect effect = new PotionEffect(type, time * 20, amp, false, showParticles);
                    p.addPotionEffect(effect);
                } else {
                    p.sendMessage(plugin.getString("messages.not_found_effect"));
                }
            }
            p.sendMessage(plugin.getString("messages.buff_command"));
        } else {
            p.sendMessage(plugin.getString("messages.not_list"));
        }
    }

    public boolean isInCooldown(Player p) {
        String uuid = p.getUniqueId().toString();

        if (plugin.cooldownConfig.contains(uuid)) {
            long ultimoUso = plugin.cooldownConfig.getLong(uuid);
            int tempoCooldown = plugin.getConfig().getInt("cooldown_time") * 1000;
            long agora = System.currentTimeMillis();

            if ((agora - ultimoUso) < tempoCooldown) {
                int restante = (int) ((tempoCooldown - (agora - ultimoUso)) / 1000);
                Map<String, String> placeholders = new HashMap<>();
                String timeFormatted = formatTime(restante);
                placeholders.put("wait_time", timeFormatted);
                p.sendMessage(plugin.getString("messages.wait_cooldown", placeholders));
                return true;
            }
        }
        return false;
    }

    public void setCooldown(Player p) {
        String uuid = p.getUniqueId().toString();
        plugin.cooldownConfig.set(uuid, System.currentTimeMillis());
        plugin.saveCooldownFile();
    }

    public static String formatTime(int totalSeconds) {
        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}