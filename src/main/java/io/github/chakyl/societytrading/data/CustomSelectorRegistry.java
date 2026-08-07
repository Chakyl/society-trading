package io.github.chakyl.societytrading.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.chakyl.societytrading.SocietyTrading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

public class CustomSelectorRegistry extends DynamicRegistry<CustomSelector> {

    public static final CustomSelectorRegistry INSTANCE = new CustomSelectorRegistry();

    private Map<String, CustomSelector> selectorsByID = new HashMap<>();

    public CustomSelectorRegistry() {
        super(SocietyTrading.LOGGER, "selectors", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(loc("selectors"), CustomSelector.CODEC);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.selectorsByID = new HashMap<>();
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        this.selectorsByID = ImmutableMap.copyOf(this.selectorsByID);
    }

    @Override
    protected void validateItem(ResourceLocation key, CustomSelector selector) {
        selector.validate(key);
        if (this.selectorsByID.containsKey(selector.selectorId())) {
            String msg = "Attempted to register two selectors (%s and %s) with the same selectorID: %s!";
            throw new UnsupportedOperationException(String.format(msg, key, this.getKey(this.selectorsByID.get(selector.selectorId())), selector.selectorId()));
        }
        this.selectorsByID.put(selector.selectorId(), selector);
    }

    @Override
    public Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        return super.prepare(pResourceManager, pProfiler);
    }

}