package io.github.chakyl.societytrading.tradelimits;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class TradeLimitCapability {
    private final Map<String, Integer> data = new HashMap<>();

    public void setData(String key, int value) {
        data.put(key, value);
    }

    public int getData(String key) {
        return data.getOrDefault(key, 0);
    }

    public Map<String, Integer> getAllData() {
        return data;
    }

    public void clear() {
        this.data.clear();
    }
    public CompoundTag saveNBT() {
        CompoundTag nbt = new CompoundTag();
        data.forEach(nbt::putInt);
        return nbt;
    }

    public void loadNBT(CompoundTag nbt) {
        data.clear();
        for (String key : nbt.getAllKeys()) {
            data.put(key, nbt.getInt(key));
        }
    }
}