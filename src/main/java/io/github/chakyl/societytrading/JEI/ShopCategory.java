package io.github.chakyl.societytrading.JEI;

import io.github.chakyl.societytrading.SocietyTrading;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ShopCategory implements IRecipeCategory<ShopRecipe> {

    public static final RecipeType<ShopRecipe> TYPE = RecipeType.create(SocietyTrading.MODID, "shop", ShopRecipe.class);
    public static final ResourceLocation TRADE_TEXTURE = new ResourceLocation(SocietyTrading.MODID, "textures/jei/shop.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final Component name;


    public ShopCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TRADE_TEXTURE, 0, 0, 147, 82);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.VILLAGER_SPAWN_EGG));
        this.name = Component.translatable("jei.society_trading.category.shops");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public Component getTitle() {
        return this.name;
    }

    @Override
    public RecipeType<ShopRecipe> getRecipeType() {
        return TYPE;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, ShopRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 0, 0).addIngredient(VanillaTypes.ITEM_STACK, recipe.catalyst);
    }

    @Override
    public void draw(ShopRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(recipe.shopName), 19, 4, 4210752, false);
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, FormattedText.of(Component.translatable("shop.society_trading." + recipe.shopId + ".description").getString()), 70, 17, 77, 4210752);
        guiGraphics.blit(new ResourceLocation(recipe.texture + ".png"), 3, 17, 0, 0.0F, 0.0F, 64, 64, 64, 64);

    }

}
