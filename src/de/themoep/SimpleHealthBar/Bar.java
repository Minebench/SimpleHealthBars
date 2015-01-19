package de.themoep.SimpleHealthBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phoenix616 on 19.01.2015.
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
