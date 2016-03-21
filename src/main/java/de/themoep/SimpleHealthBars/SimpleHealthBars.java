package de.themoep.SimpleHealthBars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SimpleHealthBars - Displayname controlled healthbar Bukkit plugin.
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.*
 */

public class SimpleHealthBars extends JavaPlugin implements Listener {

    private Map<UUID,Bar> mobs = new HashMap<UUID, Bar>();

    private Map<EntityType, Integer> mobheight = new HashMap<EntityType, Integer>();

    private boolean cnvfix = false;

    private int bossBarRange;
    private Pattern bossBarPattern = Pattern.compile("\\{bossbar:(.*)\\}");
    // Default bossbar settings
    private BarColor defaultBossBarColor = BarColor.PURPLE;
    private BarStyle defaultBossBarStyle = BarStyle.SOLID;
    private List<BarFlag> defaultBossBarFlags = new ArrayList<BarFlag>();

    public void onEnable() {
        bossBarRange = getServer().getViewDistance() * 16;
        saveDefaultConfig();
        ConfigurationSection heightsection = getConfig().getConfigurationSection("mobheight");
        if(heightsection != null)
            for(String type : heightsection.getKeys(false))
                try {
                    mobheight.put(EntityType.valueOf(type.toUpperCase()), heightsection.getInt(type));
                } catch (IllegalArgumentException e) {
                    getLogger().warning(type + " is not a valid Bukkit EntityType! Couldn't load height for this mob!");
                }
        
        ConfigurationSection listsection = getConfig().getConfigurationSection("moblist");
        if(listsection != null) {
            for(String id : listsection.getKeys(false)) {
                loadBar(UUID.fromString(id), listsection.getString(id));
            }
        }
        cnvfix = getConfig().getBoolean("CustomNameVisibleFix");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        getConfig().set("moblist", null);
        for(UUID id : mobs.keySet()) {
            getConfig().set("moblist." + id.toString(), mobs.get(id).getName());
        }
        saveConfig();
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity e = event.getEntity();
        String name = e.getCustomName();
        if(name != null && !name.equals("")) {
            loadBar(e.getUniqueId(), name);
            setBar(e, (int) (e.getHealth()));
        }
    }

    @EventHandler
    public void onMobDamaged(EntityDamageEvent event) {
        if(event.getEntity() instanceof LivingEntity && mobs.containsKey(event.getEntity().getUniqueId())) {
            LivingEntity e = (LivingEntity) event.getEntity();
            setBar(e, (int) (e.getHealth() - event.getDamage()));
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if(mobs.containsKey(event.getEntity().getUniqueId())) {
            clearSnowballs(event.getEntity());
            BossBar bossBar = mobs.get(event.getEntity().getUniqueId()).getBossBar();
            if(bossBar != null) {
                bossBar.removeAll();
                bossBar.setVisible(false);
            }
            mobs.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChangeChunk(PlayerMoveEvent event) {
        if(event.getFrom().getChunk() == event.getTo().getChunk()) {
            return;
        }

        for(Map.Entry<UUID, Bar> barMob : mobs.entrySet()) {
            if(barMob.getValue().getBossBar() != null) {
                // Just remove him from all the bars so that we don't have to check the distance twice
                // as the client won't notice any flickering when he already has this bar
                barMob.getValue().getBossBar().removePlayer(event.getPlayer());
            }
        }
        searchForBoss(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        for(Bar bar : mobs.values()) {
            if(bar.getBossBar() != null) {
                bar.getBossBar().removePlayer(event.getPlayer());
            }
        }
        searchForBoss(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        searchForBoss(event.getPlayer());
    }

    private void searchForBoss(Player player) {
        if(mobs.size() == 0) {
            return;
        }

        for(Entity e : player.getNearbyEntities(bossBarRange, bossBarRange, bossBarRange)) {
            if(!mobs.containsKey(e.getUniqueId())) {
                continue;
            }
            Bar bar = mobs.get(e.getUniqueId());
            if(bar.getBossBar() != null) {
                bar.getBossBar().addPlayer(player);
            }
        }
    }

    private void loadBar(UUID id, String name) {
        if(name.contains("{heartbar}")) {
            mobs.put(id, new Bar(BarType.HEARTBAR, name));
        }

        if(name.contains("{healthshort}")) {
            if(mobs.containsKey(id))
                mobs.get(id).getTypes().add(BarType.HEALTHSHORT);
            else
                mobs.put(id, new Bar(BarType.HEALTHSHORT, name));
        }

        if(name.contains("{pipebar}")) {
            if(mobs.containsKey(id))
                mobs.get(id).getTypes().add(BarType.HEALTHSHORT);
            else
                mobs.put(id, new Bar(BarType.HEALTHSHORT, name));
        }

        if(name.contains("{bossbar")) {
            boolean contains = name.contains("{bossbar}");

            BarColor barColor = defaultBossBarColor;
            BarStyle barStyle = defaultBossBarStyle;
            List<BarFlag> barFlags = defaultBossBarFlags;

            if(!contains) {
                Matcher optionMatcher = bossBarPattern.matcher(name);
                while(optionMatcher.find()) {
                    if(optionMatcher.groupCount() < 1) {
                        continue;
                    }
                    contains = true;
                    String[] optionsStr = optionMatcher.group(1).toUpperCase().split(":");
                    for(String s : optionsStr) {
                        try {
                            barColor = BarColor.valueOf(s);
                        } catch(IllegalArgumentException noSuchBarColor) {
                            try {
                                barStyle = BarStyle.valueOf(s);
                            } catch(IllegalArgumentException noSuchBarStyle) {
                                try {
                                    barFlags.add(BarFlag.valueOf(s));
                                } catch(IllegalArgumentException noSuchBarFlag) {
                                    getLogger().log(Level.WARNING, s + " is neither a valid BarColor, BarStyle or BarFlag enum! (Entity: " + id + "/" + name);
                                }
                            }
                        }
                    }
                }
            }

            if(contains) {
                if(mobs.containsKey(id))
                    mobs.get(id).getTypes().add(BarType.BOSSBAR);
                else
                    mobs.put(id, new Bar(BarType.BOSSBAR, name));

                BossBar bossBar = getServer().createBossBar("", barColor, barStyle, barFlags.toArray(new BarFlag[barFlags.size()]));
                bossBar.setVisible(true);
                mobs.get(id).setBossBar(bossBar);
            }
        }
    }

    /**
     * Set the bar above the head of an entity to the bars defined in the map
     * @param e The entity
     * @param health The health to set the bar to
     */
    public void setBar(LivingEntity e, int health) {
        if(mobs.containsKey(e.getUniqueId())) {
            Bar b = mobs.get(e.getUniqueId());
            String name = b.getName();
            if (health < 0)
                health = 0;
            if (b.getTypes().contains(BarType.HEARTBAR)) {
                String s = ChatColor.RED + "";
                int i = 0;
                while (i < health / 2) {
                    s = s + "❤";
                    i++;
                }
                if (health < e.getMaxHealth()) {
                    s += ChatColor.DARK_GRAY + "";
                    while (i < e.getMaxHealth() / 2) {
                        s += "❤";
                        i++;
                    }
                }
                name = name.replace("{heartbar}", s);
            }

            if (b.getTypes().contains(BarType.PIPEBAR)) {
                String s = ChatColor.RED + "";
                int i = 0;
                while (i < health / 2) {
                    s = s + "|";
                    i++;
                }
                if (health < e.getMaxHealth()) {
                    s += ChatColor.DARK_GRAY + "";
                    while (i < e.getMaxHealth() / 2) {
                        s += "|";
                        i++;
                    }
                }
                name = name.replace("{pipebar}", s);
            }

            if (b.getTypes().contains(BarType.HEALTHSHORT)) {
                name = name.replace("{healthshort}", ChatColor.RED + Integer.toString(health / 2) + ChatColor.GRAY + "/" + Integer.toString((int) e.getMaxHealth() / 2));
            }

            if (b.getTypes().contains(BarType.BOSSBAR)) {
                if(name.contains("{bossbar}")) {
                    name = name.replace("{bossbar}", "");
                } else if(name.contains("{bossbar:")){
                    name = bossBarPattern.matcher(name).replaceAll("");
                }
                b.getBossBar().setProgress(health / e.getMaxHealth());

                for(Player player : e.getWorld().getPlayers()) {
                    try {
                        boolean addPlayer = !b.getBossBar().getPlayers().contains(player) && player.getLocation().distanceSquared(e.getLocation()) <= bossBarRange * bossBarRange;
                        boolean removePlayer = !addPlayer && b.getBossBar().getPlayers().contains(player) && player.getLocation().distanceSquared(e.getLocation()) > bossBarRange * bossBarRange;
                        if(addPlayer) {
                            b.getBossBar().addPlayer(player);
                        } else if(removePlayer) {
                            // This method does not send packets to the player if he isn't even in this BossBattle
                            b.getBossBar().removePlayer(player);
                        }
                    } catch(IllegalArgumentException ignored) {
                        // Thrown if the they aren't in the same world (which shouldn't happen) or something is null
                    }
                }
            }
            
            setNameTag(e, name);
        }
    }
    
    public void setNameTag(LivingEntity e, String tag) {
        if(cnvfix && e.isCustomNameVisible()) {
            e.setCustomName(null);

            Entity sb = e.getPassenger();

            Entity top = null;
            
            while(sb != null && sb.getType() == EntityType.SNOWBALL) {
                top = sb;
                sb = sb.getPassenger();
            }
            
            if(top == null) {
                Entity ridden = e;

                for(int i = 0; i < getMobHeight(e.getType()); i ++) {
                    top = e.getWorld().spawnEntity(e.getLocation(), EntityType.SNOWBALL);
                    ridden.setPassenger(top);
                    ridden = top;
                }
               
            }
            
            if(top != null) {
                top.setCustomNameVisible(true);
                top.setCustomName(tag);
            } else {
                getLogger().severe("Top entity is null! This shouldn't be possible to happen... please report that immediately! You can disable the CustonNameVisibleFix option in your config for now.");
                e.setCustomName(tag);
            }

        } else
            e.setCustomName(tag);

        if(mobs.containsKey(e.getUniqueId())) {
            Bar b = mobs.get(e.getUniqueId());

            if(b.getTypes().contains(BarType.BOSSBAR) && b.getBossBar() != null) {
                b.getBossBar().setTitle(tag);
                b.getBossBar().setVisible(true);
            }
        }
    }
    
    public int getMobHeight(EntityType et) {
        if(mobheight.containsKey(et) && mobheight.get(et) > 0)
            return mobheight.get(et);
        return 1;
    }

    public void clearSnowballs(Entity e) {
        if(cnvfix) {
            e = e.getPassenger();
            if(e != null) {
                while (e != null && e.getType() == EntityType.SNOWBALL) {
                    Entity remove = e;
                    e = e.getPassenger();
                    remove.remove();
                }
            }
        }        
    }

}
