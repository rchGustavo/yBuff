package com.ylabui.yBuff;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin {

    private FileConfiguration config;
    private File configFile;

    public File cooldownFile;
    public FileConfiguration cooldownConfig;

    @Override
    public void onEnable() {
        initializeConfigs();
        registerCommands();
        startCooldownTask();

        logPluginStartup();
    }

    @Override
    public void onDisable() {
        saveCooldownFile();
    }

    // === Inicializações ===

    private void initializeConfigs() {
        setupMainConfig();
        setupCooldownFile();
    }

    private void registerCommands() {
        PluginCommand buffCommand = getCommand("buff");
        if (buffCommand != null) {
            buffCommand.setExecutor(new BuffCommand(this));
            buffCommand.setTabCompleter(new BuffTabComplete(this));
        }
    }

    private void logPluginStartup() {
        getLogger().info(" ");
        getLogger().info("-- yBuff --");
        getLogger().info("Plugin successfully activated!");
        getLogger().info("Developed by yLabui.");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info(" ");
    }

    // === Configuração Principal ===

    private void setupMainConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfigFile() {
        setupMainConfig();
    }

    public FileConfiguration getCustomConfig() {
        return config;
    }

    // === Mensagens com placeholders ===

    public String getString(String path) {
        return getString(path, new HashMap<>());
    }

    public String getString(String path, Map<String, String> placeholders) {
        String value = config.getString(path);
        if (value == null) return "§cMessage not found:: " + path;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return ChatColor.translateAlternateColorCodes('&', value);
    }

    // === Arquivo de cooldown ===

    private void setupCooldownFile() {
        cooldownFile = new File(getDataFolder(), "cooldowns.yml");

        if (!cooldownFile.exists()) {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);
    }

    public void saveCooldownFile() {
        try {
            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === Tarefa de cooldown ===

    private void startCooldownTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long now = System.currentTimeMillis();
            int cooldownMillis = getConfig().getInt("cooldown_time") * 1000;

            for (Player player : Bukkit.getOnlinePlayers()) {
                String uuid = player.getUniqueId().toString();

                if (cooldownConfig.contains(uuid)) {
                    long lastUsed = cooldownConfig.getLong(uuid);

                    if (now - lastUsed >= cooldownMillis) {
                        cooldownConfig.set(uuid, null);
                        saveCooldownFile();
                    }
                }
            }
        }, 20L, 20L);
    }
}