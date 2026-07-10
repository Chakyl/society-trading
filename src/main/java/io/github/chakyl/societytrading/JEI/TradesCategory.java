package io.github.chakyl.societytrading.JEI;

import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.util.ScreenUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.chakyl.societytrading.util.ShopData.formatPrice;

public class TradesCategory implements IRecipeCategory<TradeRecipe> {

    public static final RecipeType<TradeRecipe> TYPE = RecipeType.create(SocietyTrading.MODID, "trade", TradeRecipe.class);
    public static final ResourceLocation TRADE_TEXTURE = new ResourceLocation(SocietyTrading.MODID, "textures/jei/trade.png");
    private static final int INDICATOR_ICON_SIZE = 8;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component name;


    public TradesCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TRADE_TEXTURE, 0, 0, 164, 56);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.EMERALD));
        this.name = Component.translatable("jei.society_trading.category.trades");
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
    public RecipeType<TradeRecipe> getRecipeType() {
        return TYPE;
    }

    public void setRecipe(IRecipeLayoutBuilder builder, TradeRecipe recipe, IFocusGroup focuses) {
        int priceOffset = 53;

        IRecipeSlotBuilder slotA = builder.addSlot(RecipeIngredientRole.INPUT, !recipe.costB.isEmpty() && recipe.hideCostB ? priceOffset : 45, 21);
        if (!recipe.hideCostA) {
            slotA.addIngredient(VanillaTypes.ITEM_STACK, recipe.costA);
        } else {
            makeSlotInvisible(slotA);
        }

        IRecipeSlotBuilder slotB = builder.addSlot(RecipeIngredientRole.INPUT, recipe.hideCostA ? priceOffset : 21, 21);
        if (!recipe.costB.isEmpty() && !recipe.hideCostB) {
            slotB.addIngredient(VanillaTypes.ITEM_STACK, recipe.costB);
        } else {
            makeSlotInvisible(slotB);
        }

        builder.addSlot(RecipeIngredientRole.CATALYST, 0, 0).addIngredient(VanillaTypes.ITEM_STACK, recipe.catalyst);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 113, 21).addIngredient(VanillaTypes.ITEM_STACK, recipe.result);
    }

    private void makeSlotInvisible(IRecipeSlotBuilder slotBuilder) {
        slotBuilder.setBackground(new mezz.jei.api.gui.drawable.IDrawable() {
            @Override public int getWidth() { return 16; }
            @Override public int getHeight() { return 16; }
            @Override public void draw(net.minecraft.client.gui.GuiGraphics guiGraphics, int xOffset, int yOffset) {}
        }, 0, 0);

        slotBuilder.addTooltipCallback((recipeSlotView, tooltip) -> tooltip.clear());
    }

    @Override
    public void draw(TradeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(recipe.shopName), 19, 4, 4210752, false);

        if (recipe.numismaticsCost > 0) {
            boolean replaced = recipe.hideCostA && recipe.hideCostB;
            Component priceStr = Component.translatable("jei.society_trading.price", formatPrice(Integer.valueOf(recipe.numismaticsCost).toString()));
            guiGraphics.drawString(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(priceStr), replaced ? 58 - Minecraft.getInstance().font.width(priceStr) : 5, 25, 16777215, true);
        }
        if (!recipe.seasonsRequired.isEmpty()) {
            List<String> seasonsAdded = new ArrayList<>(4);
            for (int i = 0; i < recipe.seasonsRequired.size(); i++) {
                String season = recipe.seasonsRequired.get(i).split("_")[1];
                if (!seasonsAdded.contains(season)) {
                    guiGraphics.blit(TRADE_TEXTURE, 164 - INDICATOR_ICON_SIZE - ((INDICATOR_ICON_SIZE + 1) * seasonsAdded.size()) - 4, 4, 0, (8 * ScreenUtils.getSeasonOrder(recipe.seasonsRequired.get(i))), 58, INDICATOR_ICON_SIZE, INDICATOR_ICON_SIZE, 256, 256);
                    seasonsAdded.add(season);
                }

            }
        }
        if (!Objects.equals(recipe.unlockDescription, Component.empty())) {
            guiGraphics.drawString(Minecraft.getInstance().font, Language.getInstance().getVisualOrder(recipe.unlockDescription), 2, 46, 4210752, false);
        }
    }

    @Override
    public List<Component> getTooltipStrings(TradeRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (!recipe.seasonsRequired.isEmpty()) {
            int iconX = 164 - INDICATOR_ICON_SIZE - ((INDICATOR_ICON_SIZE + 1) * 3) - 4;
            int iconY = 4;
            if (mouseX >= iconX && mouseX < iconX + ((INDICATOR_ICON_SIZE + 1) * 4) && mouseY >= iconY && mouseY < iconY + INDICATOR_ICON_SIZE) {
                List<Component> seasonTooltips = new ArrayList<>(recipe.seasonsRequired.size() + 1);
                seasonTooltips.add(Component.translatable("jei.society_trading.seasons").withStyle(ChatFormatting.GOLD));
                for (String season : recipe.seasonsRequired) {
                    seasonTooltips.add(Component.translatable("desc.sereneseasons." + season));
                }
                return seasonTooltips;
            }
        }

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }
}
