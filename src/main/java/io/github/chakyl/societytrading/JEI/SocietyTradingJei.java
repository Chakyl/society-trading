package io.github.chakyl.societytrading.JEI;

import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.data.ShopRegistry;
import io.github.chakyl.societytrading.trading.ShopOffer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static io.github.chakyl.societytrading.SocietyTrading.loc;

@JeiPlugin
public class SocietyTradingJei implements IModPlugin {

    public static final ResourceLocation UID = loc("plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new TradesCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new ShopCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<ShopRecipe> shopRecipeList = new ArrayList<>();
        List<TradeRecipe> tradeRecipeList = new ArrayList<>();
        for (Shop shop : ShopRegistry.INSTANCE.getValues()) {
            shopRecipeList.add(new ShopRecipe(shop));
            for (ShopOffer offer : shop.trades()) {
                tradeRecipeList.add(new TradeRecipe(offer, shop.jeiCatalyst(), shop.name()));

            }
        }
        registration.addRecipes(ShopCategory.TYPE, shopRecipeList);
        registration.addRecipes(TradesCategory.TYPE, tradeRecipeList);

    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

}