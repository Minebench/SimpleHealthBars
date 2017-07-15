package de.themoep.SimpleHealthBars;

import org.bukkit.boss.BossBar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * SimpleHealthBars - Displayname controlled healthbar Bukkit plugin.
 * Copyright (C) 2015 Max Lee (https://github.com/Phoenix616/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class Bar {
    private String name;
    private Set<BarType> types = new HashSet<>();
    private BossBar bossBar;
    private int bossBarRange;

    public Bar(List<String> types, String name) {
        for (String s : types) {
            this.types.add(BarType.valueOf(s.toUpperCase()));
        }
        this.name = name;
    }

    public Bar(BarType type, String name) {
        this.types.add(type);
        this.name = name;
    }

    public Set<BarType> getTypes() {
        return types;
    }

    public String getName() {
        return name;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public void setBossBar(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public void setBossBarRange(int bossBarRange) {
        this.bossBarRange = bossBarRange;
    }

    public int getBossBarRange() {
        return bossBarRange;
    }
}
