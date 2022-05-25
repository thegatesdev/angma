package com.thegates.angma;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DisabledContainer<T, G> {

    private final Map<T, Set<G>> disabledMap = new HashMap<>(1, 0.85f);

    public void put(T t, G g) {
        putOverHead(t, g, 1);
    }

    public void putOverHead(T t, G g, int overHead) {
        disabledMap.putIfAbsent(t, new HashSet<>(overHead));
        disabledMap.get(t).add(g);
    }

    public boolean remove(T t, G g) {
        Set<G> gs = disabledMap.get(t);
        if (gs == null) return false;
        return gs.remove(g);
    }

    public boolean has(T t, G g) {
        if (!disabledMap.containsKey(t)) return false;  // Short circuit because it is likely to be false.
        Set<G> gs = disabledMap.get(t);
        return gs.contains(g);
    }

    public void clear() {
        disabledMap.clear();
    }

    public NbtCompound populateNbt(Function<T, String> tToString, Function<G, String> gToString) {
        NbtCompound compound = new NbtCompound();
        disabledMap.keySet().forEach(t -> {
            NbtList nbtList = new NbtList();
            disabledMap.get(t).forEach(g -> nbtList.add(NbtString.of(gToString.apply(g))));
            compound.put(tToString.apply(t), nbtList);
        });
        return compound;
    }
}
