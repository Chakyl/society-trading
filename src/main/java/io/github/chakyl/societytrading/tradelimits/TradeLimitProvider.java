package io.github.chakyl.societytrading.tradelimits;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class TradeLimitProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<TradeLimitCapability> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    private TradeLimitCapability instance = new TradeLimitCapability();
    private final LazyOptional<TradeLimitCapability> optional = LazyOptional.of(() -> instance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return PLAYER_DATA.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.saveNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.loadNBT(nbt);
    }
}