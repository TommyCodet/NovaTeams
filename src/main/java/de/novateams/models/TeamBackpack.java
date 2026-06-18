// src/main/java/de/novateams/models/TeamBackpack.java
package de.novateams.models;

import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

public class TeamBackpack {
    
    private int size;
    private final Map<Integer, ItemStack> contents;
    
    public TeamBackpack(int size) {
        this.size = size;
        this.contents = new HashMap<>();
    }
    
    public int getSize() { return size; }
    
    public void setSize(int newSize) {
        this.size = newSize;
    }
    
    public Map<Integer, ItemStack> getContents() {
        return new HashMap<>(contents);
    }
    
    public void setContents(Map<Integer, ItemStack> contents) {
        this.contents.clear();
        contents.forEach((slot, item) -> {
            if (slot < size && item != null) {
                this.contents.put(slot, item.clone());
            }
        });
    }
    
    public ItemStack getItem(int slot) {
        return contents.get(slot);
    }
    
    public void setItem(int slot, ItemStack item) {
        if (slot < size) {
            if (item == null) {
                contents.remove(slot);
            } else {
                contents.put(slot, item.clone());
            }
        }
    }
    
    public void clear() {
        contents.clear();
    }
}
