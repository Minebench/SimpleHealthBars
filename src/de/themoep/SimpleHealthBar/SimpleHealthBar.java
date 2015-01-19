package de.themoep.SimpleHealthBar;

import de.themoep.utils.SaveUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;


public class SimpleHealthBar extends JavaPlugin implements Listener {

    Map<UUID,Bar> mobs = new HashMap<UUID, Bar>();
    private SaveUtils saveutils;

    public void onEnable() {
        ConfigurationSection section = this.getConfig().getConfigurationSection("moblist");
        if(section != null)
            for(String id : section.getKeys(false)) {
                String name = section.getString(id);

                if(name.contains("{heartbar}"))
                    mobs.put(UUID.fromString(id), new Bar(BarType.HEARTBAR, name));

                if(name.contains("{healthshort}"))
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

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        this.getConfig().set("moblist", null);
        for(UUID id : this.mobs.keySet()) {
            this.getConfig().set("moblist." + id.toString(), mobs.get(id).getName());
        }
        this.saveConfig();
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        String name = event.getEntity().getCustomName();
        if(name != null && !name.equals("")) {
            LivingEntity e = (LivingEntity) event.getEntity();
            if(name.contains("{heartbar}")) {
                mobs.put(event.getEntity().getUniqueId(), new Bar(BarType.HEARTBAR, name));
                double health = e.getHealth();
                String s = ChatColor.RED + "";
                int i = 0;
                while(i < health/2) {
                    s = s + "❤";
                    i++;
                }
                if(health < e.getMaxHealth()) {
                    s += ChatColor.DARK_GRAY + "";
                    while(i < e.getMaxHealth()/2) {
                        s += "❤";
                        i++;
                    }
                }
               name = name.replace("{heartbar}", s);
            }
            if(name.contains("{healthshort}")) {
                if(mobs.containsKey(event.getEntity().getUniqueId()))
                    mobs.get(event.getEntity().getUniqueId()).getTypes().add(BarType.HEALTHSHORT);
                else
                    mobs.put(event.getEntity().getUniqueId(), new Bar(BarType.HEALTHSHORT, name));
                int health = (int) e.getHealth();
               name = name.replace("{healthshort}", ChatColor.RED + Integer.toString(health/2) + ChatColor.GRAY + "/" + Integer.toString((int) e.getMaxHealth()/2));
            }
            if(name.contains("{bossbar}")) {
                if(mobs.containsKey(event.getEntity().getUniqueId()))
                    mobs.get(event.getEntity().getUniqueId()).getTypes().add(BarType.BOSSBAR);
                else
                    mobs.put(event.getEntity().getUniqueId(), new Bar(BarType.BOSSBAR, name));
                saveutils.writeMap(saveutils, "mobbar.map");
                // TODO: Work in Progress
            }
            event.getEntity().setCustomName(name);
        }
    }

    @EventHandler
    public void onMobDamager(EntityDamageEvent event) {
        if(event.getEntity() instanceof LivingEntity && mobs.containsKey(event.getEntity().getUniqueId())) {
            LivingEntity e = (LivingEntity) event.getEntity();
            Bar b = mobs.get(e.getUniqueId());
            String name = b.getName();
            int health = (int) (e.getHealth() - event.getDamage());
            if(health < 0)
                health = 0;
            if(b.getTypes().contains(BarType.HEARTBAR)) {
                String s = ChatColor.RED + "";
                int i = 0;
                while(i < health/2) {
                    s = s + "❤";
                    i++;
                }
                if(health < e.getMaxHealth()) {
                    s += ChatColor.DARK_GRAY + "";
                    while(i < e.getMaxHealth()/2) {
                        s += "❤";
                        i++;
                    }
                }
                name = name.replace("{heartbar}",s);
            }
            if(b.getTypes().contains(BarType.HEALTHSHORT)) {
                name = name.replace("{healthshort}", ChatColor.RED + Integer.toString(health/2) + ChatColor.GRAY + "/" + Integer.toString((int) e.getMaxHealth()/2));
            }
            if(b.getTypes().contains(BarType.BOSSBAR)) {
                // TODO: Work in Progress
            }
            e.setCustomName(name);
        }
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        mobs.remove(event.getEntity().getUniqueId());
    }

}
