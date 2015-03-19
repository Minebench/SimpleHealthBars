package de.themoep.SimpleHealthBars;

import java.util.ArrayList;
import java.util.List;

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

public class Bar {
    private String name;
    private List<BarType> types = new ArrayList<BarType>();

    public Bar(List<String> types, String name) {
        for(String s : types) {
            this.types.add(BarType.valueOf(s.toUpperCase()));
        }
        this.name = name;
    }

    public Bar(BarType type, String name) {
        this.types.add(type);
        this.name = name;
    }

    public List<BarType> getTypes() {
        return types;
    }

    public String getName() {
        return name;
    }
}
