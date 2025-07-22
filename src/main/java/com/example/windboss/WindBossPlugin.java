package com.example.windboss;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class WindBossPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Register commands
        getCommand("windboss").setExecutor(new WindBossCommand());
        
        // Disable Mace crafting
        Bukkit.removeRecipe(new NamespacedKey("minecraft", "mace"));
        
        getLogger().info("WindBoss Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("WindBoss Plugin Disabled!");
    }
}

class WindBossCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /windboss <codes|reload|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "codes":
                if (!player.hasPermission("windboss.summon")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to summon the Wind Boss!");
                    return true;
                }
                Location loc = player.getLocation();
                new WindBossEntity(loc);
                player.sendMessage(ChatColor.GREEN + "Wind Boss summoned!");
                break;
                
            case "reload":
                if (!player.hasPermission("windboss.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to reload the plugin!");
                    return true;
                }
                player.getServer().getPluginManager().getPlugin("WindBoss").reloadConfig();
                player.sendMessage(ChatColor.GREEN + "WindBoss configuration reloaded!");
                break;
                
            case "info":
                player.sendMessage(ChatColor.AQUA + "=== Wind Boss Info ===");
                player.sendMessage(ChatColor.GRAY + "Health: 500 HP");
                player.sendMessage(ChatColor.GRAY + "Phases:");
                player.sendMessage(ChatColor.GRAY + "- 75% HP: Wither Phase (Explosions, Wither Skulls, Flight)");
                player.sendMessage(ChatColor.GRAY + "- 50% HP: Elder Guardian Phase (Mining Fatigue)");
                player.sendMessage(ChatColor.GRAY + "- 25% HP: Ender Dragon Phase (Regeneration IV)");
                player.sendMessage(ChatColor.GRAY + "Drops: 5 Enchanted Golden Apples, 3 Diamond Blocks, 1 Mace");
                break;
                
            default:
                player.sendMessage(ChatColor.RED + "Usage: /windboss <codes|reload|info>");
        }
        return true;
    }
}
