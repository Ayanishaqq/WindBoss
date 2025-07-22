package com.example.windboss;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class WindBossEntity implements Listener {
    private final Entity entity;
    private final BossBar bossBar;
    private double maxHealth = 500.0;
    private HashMap<UUID, Integer> hitCount = new HashMap<>();
    private int phase = 0;
    private boolean canFly = false;

    public WindBossEntity(Location location) {
        // Spawn custom zombie as the boss
        Zombie boss = location.getWorld().spawn(location, Zombie.class);
        boss.setCustomName(ChatColor.GRAY + "Wind Boss");
        boss.setCustomNameVisible(true);
        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        boss.setHealth(maxHealth);
        boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(10.0);
        
        this.entity = boss;
        
        // Create boss bar
        bossBar = Bukkit.createBossBar(ChatColor.GRAY + "Wind Boss", BarColor.GRAY, BarStyle.SOLID);
        bossBar.setVisible(true);
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("WindBoss"));
        
        // Start ability timers
        startSonicBoom();
        startPhaseChecker();
    }

    private void startSonicBoom() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    bossBar.removeAll();
                    return;
                }
                
                for (Player p : entity.getWorld().getPlayers()) {
                    if (p.getLocation().distance(entity.getLocation()) <= 250) {
                        bossBar.addPlayer(p);
                        if (p.getLocation().distance(entity.getLocation()) <= 15) {
                            p.damage(5.0, entity);
                            p.sendMessage(ChatColor.GRAY + "Wind Boss hits you with Sonic Boom!");
                        }
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("WindBoss"), 0, 100); // Every 5 seconds
    }

    private void startPhaseChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    bossBar.removeAll();
                    return;
                }

                double healthPercent = entity.getHealth() / maxHealth;
                
                if (healthPercent <= 0.75 && phase < 1) {
                    phase = 1;
                    canFly = true;
                    entity.getWorld().createExplosion(entity.getLocation(), 4.0f, false, false);
                    startWitherSkullAttack();
                }
                
                if (healthPercent <= 0.50 && phase < 2) {
                    phase = 2;
                    startMiningFatigue();
                }
                
                if (healthPercent <= 0.25 && phase < 3) {
                    phase = 3;
                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 3));
                }
                
                if (healthPercent > 0.75 && phase >= 3) {
                    ((LivingEntity) entity).removePotionEffect(PotionEffectType.REGENERATION);
                }
                
                bossBar.setProgress(healthPercent);
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("WindBoss"), 0, 20); // Every second
    }

    private void startWitherSkullAttack() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || phase < 1) {
                    cancel();
                    return;
                }
                
                for (Player p : entity.getWorld().getPlayers()) {
                    if (p.getLocation().distance(entity.getLocation()) <= 50) {
                        WitherSkull skull = entity.getWorld().spawn(entity.getLocation().add(0, 1.5, 0), WitherSkull.class);
                        Vector direction = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                        skull.setDirection(direction);
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("WindBoss"), 0, 20); // Every second
    }

    private void startMiningFatigue() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || phase < 2) {
                    cancel();
                    return;
                }
                
                for (Player p : entity.getWorld().getPlayers()) {
                    if (p.getLocation().distance(entity.getLocation()) <= 75) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 0));
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("WindBoss"), 0, 200); // Every 10 seconds
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() != entity || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        
        hitCount.put(playerId, hitCount.getOrDefault(playerId, 0) + 1);
        
        if (hitCount.get(playerId) >= 3) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1));
            player.setVelocity(new Vector(0, 2, 0));
            hitCount.put(playerId, 0);
            player.sendMessage(ChatColor.GRAY + "Wind Boss launches you away!");
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() != entity) {
            return;
        }

        event.getDrops().clear();
        event.getDrops().add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 5));
        event.getDrops().add(new ItemStack(Material.DIAMOND_BLOCK, 3));
        event.getDrops().add(new ItemStack(Material.MACE, 1));
        
        bossBar.removeAll();
    }
}
