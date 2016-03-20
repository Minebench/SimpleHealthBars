package de.themoep.SimpleHealthBars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    Map<UUID,Bar> mobs = new HashMap<UUID, Bar>();
    
    Map<EntityType, Integer> mobheight = new HashMap<EntityType, Integer>();
    
    boolean cnvfix = false;

    public void onEnable() {
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
        if(listsection != null)
            for(String id : listsection.getKeys(false)) {
                String name = listsection.getString(id);

                if(name.contains("{heartbar}"))
                    mobs.put(UUID.fromString(id), new Bar(BarType.HEARTBAR, name));

                if(name.contains("{healthshort}"))
                    if(mobs.containsKey(UUID.fromString(id)))
                        mobs.get(UUID.fromString(id)).getTypes().add(BarType.HEALTHSHORT);
                    else
                        mobs.put(UUID.fromString(id), new Bar(BarType.HEALTHSHORT, name));

                if(name.contains("{pipebar}"))
                    if(mobs.containsKey(UUID.fromString(id)))
                        mobs.get(UUID.fromString(id)).getTypes().add(BarType.HEALTHSHORT);
                    else
                        mobs.put(UUID.fromString(id), new Bar(BarType.HEALTHSHORT, name));

                if(name.contains("{bossbar}"))
                    if(mobs.containsKey(UUID.fromString(id)))
                        mobs.get(UUID.fromString(id)).getTypes().add(BarType.BOSSBAR);
                    else
                        mobs.put(UUID.fromString(id), new Bar(BarType.BOSSBAR, name));

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

            if(name.toLowerCase().contains("{heartbar}")) {
                if(mobs.containsKey(e.getUniqueId()))
                    mobs.get(e.getUniqueId()).getTypes().add(BarType.HEARTBAR);
                else
                    mobs.put(e.getUniqueId(), new Bar(BarType.HEARTBAR, name));
            }

            if(name.toLowerCase().contains("{pipebar}")) {
                if(mobs.containsKey(e.getUniqueId()))
                    mobs.get(e.getUniqueId()).getTypes().add(BarType.PIPEBAR);
                else
                    mobs.put(e.getUniqueId(), new Bar(BarType.PIPEBAR, name));
            }

            if(name.toLowerCase().contains("{healthshort}")) {
                if(mobs.containsKey(e.getUniqueId()))
                    mobs.get(e.getUniqueId()).getTypes().add(BarType.HEALTHSHORT);
                else
                    mobs.put(e.getUniqueId(), new Bar(BarType.HEALTHSHORT, name));
            }

            if(name.toLowerCase().contains("{bossbar}")) {
                if(mobs.containsKey(e.getUniqueId()))
                    mobs.get(e.getUniqueId()).getTypes().add(BarType.BOSSBAR);
                else
                    mobs.put(e.getUniqueId(), new Bar(BarType.BOSSBAR, name));
            }

            setBar(e, (int) (e.getHealth()));
        }
    }

    @EventHandler
    public void onMobDamager(EntityDamageEvent event) {
        if(event.getEntity() instanceof LivingEntity && mobs.containsKey(event.getEntity().getUniqueId())) {
            LivingEntity e = (LivingEntity) event.getEntity();
            setBar(e, (int) (e.getHealth() - event.getDamage()));
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
                // TODO: Work in Progress
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
    
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if(mobs.containsKey(event.getEntity().getUniqueId())) {
            clearSnowballs(event.getEntity());
            mobs.remove(event.getEntity().getUniqueId());
        }
            
    }

}
