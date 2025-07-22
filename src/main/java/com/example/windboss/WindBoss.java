package com.example.windboss;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class WindBoss extends JavaPlugin implements Listener {
    private BossBar bossBar;
    private Wither windBoss;
    private Map<UUID, Integer> playerHits = new HashMap<>();
    private boolean isWitherPhase = false;
    private boolean isElderGuardianPhase = false;
    private boolean isEnderDragonPhase = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        getCommand("windboss").setExecutor(new WindBossCommand());
        disableMaceRecipe();
    }

    private void disableMaceRecipe() {
        Iterator<Recipe> iterator = getServer().recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (recipe.getResult().getType() == Material.MACE) {
                iterator.remove();
            }
        }
    }

    private void spawnWindBoss(Player player) {
        Location location = player.getLocation();
        windBoss = (Wither) location.getWorld().spawnEntity(location, EntityType.WITHER);
        windBoss.setCustomName("§6Wind Boss");
        windBoss.setCustomNameVisible(true);
        windBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(500.0);
        windBoss.setHealth(500.0);

        bossBar = Bukkit.createBossBar("§6Wind Boss", BarColor.WHITE, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(location) <= 250) {
                bossBar.addPlayer(p);
            }
        }

        startBossMechanics();
    }

    private void startBossMechanics() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (windBoss == null || !windBoss.isValid()) {
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                double healthPercent = windBoss.getHealth() / windBoss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                bossBar.setProgress(healthPercent);

                // Sonic Boom (every 5 seconds)
                if (windBoss.getWorld().getGameTime() % 100 == 0) {
                    for (Entity nearby : windBoss.getNearbyEntities(10, 10, 10)) {
                        if (nearby instanceof Player) {
                            Player player = (Player) nearby;
                            player.damage(5.0, windBoss);
                            player.getWorld().spawnParticle(Particle.SONIC_BOOM, player.getLocation(), 1);
                        }
                    }
                }

                // Phase transitions
                if (healthPercent <= 0.75 && !isWitherPhase) {
                    isWitherPhase = true;
                    windBoss.getWorld().createExplosion(windBoss.getLocation(), 4.0f, false, false);
                    startWitherSkullAttack();
                }
                if (healthPercent <= 0.50 && !isElderGuardianPhase) {
                    isElderGuardianPhase = true;
                    startMiningFatigueZone();
                }
                if (healthPercent <= 0.25 && !isEnderDragonPhase) {
                    isEnderDragonPhase = true;
                    windBoss.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 3));
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void startWitherSkullAttack() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (windBoss == null || !windBoss.isValid() || !isWitherPhase) {
                    cancel();
                    return;
                }
                for (Entity nearby : windBoss.getNearbyEntities(15, 15, 15)) {
                    if (nearby instanceof Player) {
                        windBoss.launchProjectile(org.bukkit.entity.WitherSkull.class, nearby.getLocation().toVector().subtract(windBoss.getLocation().toVector()).normalize());
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void startMiningFatigueZone() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (windBoss == null || !windBoss.isValid() || !isElderGuardianPhase) {
                    cancel();
                    return;
                }
                for (Entity nearby : windBoss.getNearbyEntities(75, 75, 75)) {
                    if (nearby instanceof Player) {
                        ((Player) nearby).addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 400, 0));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 200L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (windBoss != null && windBoss.isValid() && event.getPlayer().getLocation().distance(windBoss.getLocation()) <= 250) {
            bossBar.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Wither && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains("Wind Boss")) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                UUID playerId = player.getUniqueId();
                playerHits.put(playerId, playerHits.getOrDefault(playerId, 0) + 1);

                if (playerHits.get(playerId) >= 3) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
                    playerHits.put(playerId, 0);
                }

                event.setDamage(10.0); // Melee attack damage
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Wither && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains("Wind Boss")) {
            event.getDrops().clear();
            event.getDrops().add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5));
            event.getDrops().add(new ItemStack(Material.DIAMOND_BLOCK, 3));
            ItemStack mace = new ItemStack(Material.MACE);
            ItemMeta meta = mace.getItemMeta();
            meta.setDisplayName("§6Wind Mace");
            mace.setItemMeta(meta);
            event.getDrops().add(mace);
            bossBar.removeAll();
            windBoss = null;
            isWitherPhase = false;
            isElderGuardianPhase = false;
            isEnderDragonPhase = false;
            playerHits.clear();
        }
    }

    private class WindBossCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players!");
                return true;
            }
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage("Usage: /windboss <codes|reload|info>");
                return true;
            }

            if (args[0].equalsIgnoreCase("codes")) {
                if (!player.hasPermission("windboss.summon")) {
                    player.sendMessage("You don't have permission to summon the Wind Boss!");
                    return true;
                }
                if (windBoss != null && windBoss.isValid()) {
                    player.sendMessage("A Wind Boss is already active!");
                    return true;
                }
                spawnWindBoss(player);
                player.sendMessage("Wind Boss summoned!");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("windboss.reload")) {
                    player.sendMessage("You don't have permission to reload the plugin!");
                    return true;
                }
                reloadConfig();
                player.sendMessage("Wind Boss plugin reloaded!");
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!player.hasPermission("windboss.info")) {
                    player.sendMessage("You don't have permission to view Wind Boss info!");
                    return true;
                }
                player.sendMessage("§6Wind Boss Info:");
                player.sendMessage("Health: " + (windBoss != null ? windBoss.getHealth() : "Not spawned"));
                player.sendMessage("Phases: Wither (" + isWitherPhase + "), Elder Guardian (" + isElderGuardianPhase + "), Ender Dragon (" + isEnderDragonPhase + ")");
            } else {
                player.sendMessage("Usage: /windboss <codes|reload|info>");
            }
            return true;
        }
    }
}
