package io.github.chakyl.societytrading.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static io.github.chakyl.societytrading.util.ShopData.formatPrice;

@OnlyIn(Dist.CLIENT)
public class AutoTraderScreen extends AbstractContainerScreen<AutoTraderMenu> {
    /**
     * The GUI texture for the villager merchant GUI.
     */
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(SocietyTrading.MODID, "textures/gui/auto_trader.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 3;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 22;
    private static final int TRADE_BUTTON_WIDTH = 106;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = TRADE_BUTTON_HEIGHT * NUMBER_OF_OFFER_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 195;
    private int shopItem;
    private final TradeOfferButton[] tradeOfferButtons = new TradeOfferButton[NUMBER_OF_OFFER_BUTTONS];
    int scrollOff;
    private boolean isDragging;

    public AutoTraderScreen(AutoTraderMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 284;
        this.imageHeight = 179; // 22px increase
    }

//    private void postSelectorButtonClick() {
//        PacketHandler.sendToServer(new ServerBoundAutoTradeSelectionButtonClickPacket((byte) this.shopItem));
//    }
//
//    private void postTradeButtonClick() {
//        PacketHandler.sendToServer(new ServerBoundAutoTradeButtonClickPacket((byte) this.shopItem));
//    }

    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 18;
        for (int l = 0; l < NUMBER_OF_OFFER_BUTTONS; ++l) {
            this.tradeOfferButtons[l] = this.addRenderableWidget(new TradeOfferButton(i + 88, k, l, (button) -> {
                if (button instanceof TradeOfferButton) {
                    this.shopItem = ((TradeOfferButton) button).getIndex() + this.scrollOff;
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, this.shopItem);
                }
            }));
            k += TRADE_BUTTON_HEIGHT;
        }
        this.addRenderableWidget(Button.builder(Component.literal("Change Shop"), (button) -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
                })
                .bounds(i, j + 92, 76, 18) // Position and size
                .build());
    }


    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int centralX = 88;
        pGuiGraphics.drawString(this.font, this.title, centralX, LABEL_Y, 4210752, false);
        pGuiGraphics.drawString(this.font, this.menu.getName(), 7, LABEL_Y, 4210752, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, centralX, 87, 4210752, false);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GUI_LOCATION, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        pGuiGraphics.blit(new ResourceLocation(this.menu.getTexture() + ".png"), i + 6, j + 18, 0, 0.0F, 0.0F, 64, 64, 64, 64);

        renderProgressArrow(pGuiGraphics, i, j);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if (menu.isCrafting()) {
            guiGraphics.blit(GUI_LOCATION, x + 224, y + 41, 304, 0, 8, menu.getScaledProgress(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    private void renderScroller(GuiGraphics pGuiGraphics, int pPosX, int pPosY, ShopOffers pShopOffers) {
        int i = pShopOffers.size() + 1 - NUMBER_OF_OFFER_BUTTONS;
        if (i > 1) {
            int j = SCROLL_BAR_HEIGHT - (SCROLLER_HEIGHT + (i - 1) * SCROLL_BAR_HEIGHT / i);
            int k = j / i + SCROLL_BAR_HEIGHT / i;
            int l = SCROLL_BAR_HEIGHT - SCROLLER_HEIGHT;
            int i1 = Math.min(l, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = l;
            }
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y + i1, 0, 288.0F, 0.0F, 6, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y, 0, 294.0F, 0.0F, 6, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

    }


    public void renderTransparentFakeItem(GuiGraphics pGuiGraphics, ItemStack stack, int x, int y) {
        pGuiGraphics.renderFakeItem(stack, x, y);
        pGuiGraphics.renderItemDecorations(this.font, stack, x, y);
        pGuiGraphics.fill(RenderType.guiGhostRecipeOverlay(), x, y, x + 16, y + 16, 0x80888888);
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param pGuiGraphics the GuiGraphics object used for rendering.
     * @param pMouseX      the x-coordinate of the mouse cursor.
     * @param pMouseY      the y-coordinate of the mouse cursor.
     * @param pPartialTick the partial tick time.
     */
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        ShopOffers shopOffers = this.menu.getOffers();
        this.menu.syncShopData();
        if (!shopOffers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 4 + 1;
            int l = i + 86 + NUMBER_OF_OFFER_BUTTONS;
            this.renderScroller(pGuiGraphics, i, j, shopOffers);
            int i1 = 0;
            ShopOffer selectedTrade = this.menu.getSelectedTrade();
            if (selectedTrade != null) {
                if (this.menu.resultSlotEmpty())
                    renderTransparentFakeItem(pGuiGraphics, selectedTrade.getResult(), i + 220, j + 64);
                if (this.menu.costASlotEmpty())
                    renderTransparentFakeItem(pGuiGraphics, selectedTrade.getCostA(), i + 209, j + 18);
                if (selectedTrade.getCostB() != null && this.menu.costBSlotEmpty()) {
                    renderTransparentFakeItem(pGuiGraphics, selectedTrade.getCostB(), i + 231, j + 18);
                }
            }
            for (ShopOffer shopOffer : shopOffers) {
                if (!this.canScroll(shopOffers.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_OFFER_BUTTONS + this.scrollOff) {
                    ItemStack itemstack1 = shopOffer.getCostA();
                    ItemStack itemstack2 = shopOffer.getCostB();
                    ItemStack itemstack3 = shopOffer.getResult();
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int j1 = k + 16;
                    boolean noBalance = !this.menu.cardSlotHasCard();
                    int numismaticOffset = 0;
                    int priceOffset = 5;
                    if (shopOffer.hasNumismaticsCost() && !noBalance) numismaticOffset = l + TRADE_BUTTON_WIDTH - 21;

                    if ((SocietyTrading.NUMISMATICS_INSTALLED && (!itemstack1.is(NumismaticsTags.AllItemTags.COINS.tag)) || noBalance)) {
                        this.renderAndDecorateCostA(pGuiGraphics, itemstack1, itemstack1, numismaticOffset > 0 ? numismaticOffset : l + TRADE_BUTTON_WIDTH - 21, j1);
                        priceOffset += 18;
                    }
                    if (!itemstack2.isEmpty() && ((SocietyTrading.NUMISMATICS_INSTALLED && !itemstack2.is(NumismaticsTags.AllItemTags.COINS.tag)) || noBalance)) {
                        pGuiGraphics.renderFakeItem(itemstack2, numismaticOffset > 0 ? numismaticOffset : i + TRADE_BUTTON_WIDTH + 52, j1);
                        pGuiGraphics.renderItemDecorations(this.font, itemstack2, numismaticOffset > 0 ? numismaticOffset : i + TRADE_BUTTON_WIDTH + 52, j1);
                        priceOffset += 18;
                    }
                    if (SocietyTrading.NUMISMATICS_INSTALLED && shopOffer.hasNumismaticsCost() && !noBalance) {
                        Component priceStr = Component.translatable("gui.society_trading.price", formatPrice(Integer.valueOf(shopOffer.getNumismaticsCost()).toString()));
                        pGuiGraphics.drawString(this.font, priceStr, l + TRADE_BUTTON_WIDTH - font.width(priceStr) - priceOffset, j1 + 4, 16777215, true);
                    }
                    pGuiGraphics.renderFakeItem(itemstack3, l + 1, j1);
                    pGuiGraphics.renderItemDecorations(this.font, itemstack3, l + 1, j1);
                    pGuiGraphics.pose().popPose();
                    k += TRADE_BUTTON_HEIGHT;
                    ++i1;
                } else {
                    ++i1;
                }
            }
        }
        for (TradeOfferButton AutoTraderScreen$tradeofferbutton : this.tradeOfferButtons) {
            if (AutoTraderScreen$tradeofferbutton.isHoveredOrFocused()) {
                AutoTraderScreen$tradeofferbutton.renderToolTip(pGuiGraphics, pMouseX, pMouseY);
            }

            AutoTraderScreen$tradeofferbutton.visible = AutoTraderScreen$tradeofferbutton.index < this.menu.getOffers().size();
        }

        RenderSystem.enableDepthTest();
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    private void renderAndDecorateCostA(GuiGraphics pGuiGraphics, ItemStack pRealCost, ItemStack pBaseCost, int pX, int pY) {
        pGuiGraphics.renderFakeItem(pRealCost, pX, pY);
        if (pBaseCost.getCount() == pRealCost.getCount()) {
            pGuiGraphics.renderItemDecorations(this.font, pRealCost, pX, pY);
        } else {
            pGuiGraphics.renderItemDecorations(this.font, pBaseCost, pX, pY, pBaseCost.getCount() == 1 ? "1" : null);
            // Forge: fixes Forge-8806, code for count rendering taken from GuiGraphics#renderGuiItemDecorations
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
            String count = pRealCost.getCount() == 1 ? "1" : String.valueOf(pRealCost.getCount());
            font.drawInBatch(count, (float) (pX + 14) + 19 - 2 - font.width(count), (float) pY + 6 + 3, 0xFFFFFF, true, pGuiGraphics.pose().last().pose(), pGuiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880, false);
            pGuiGraphics.pose().popPose();
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            pGuiGraphics.blit(GUI_LOCATION, pX + 7, pY + 12, 0, 0.0F, 176.0F, 9, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            pGuiGraphics.pose().popPose();
        }

    }

    private boolean canScroll(int pNumOffers) {
        return pNumOffers > NUMBER_OF_OFFER_BUTTONS;
    }

    /**
     * Called when the mouse wheel is scrolled within the GUI element.
     * <p>
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pDelta  the scrolling delta.
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     */
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int i = this.menu.getOffers().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_OFFER_BUTTONS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - pDelta), 0, j);
        }

        return true;
    }

    /**
     * Called when the mouse is dragged within the GUI element.
     * <p>
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that is being dragged.
     * @param pDragX  the X distance of the drag.
     * @param pDragY  the Y distance of the drag.
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     */
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int i = this.menu.getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - NUMBER_OF_OFFER_BUTTONS;
            float f = ((float) pMouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    /**
     * Called when a mouse button is clicked within the GUI element.
     * <p>
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that was clicked.
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     */
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size()) && pMouseX > (double) (i + SCROLL_BAR_START_X) && pMouseX < (double) (i + SCROLL_BAR_START_X + 6) && pMouseY > (double) (j + SCROLL_BAR_TOP_POS_Y) && pMouseY <= (double) (j + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @OnlyIn(Dist.CLIENT)
    class TradeOfferButton extends Button {
        final int index;

        public TradeOfferButton(int pX, int pY, int pIndex, OnPress pOnPress) {
            super(pX, pY, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
            this.index = pIndex;
            this.visible = false;
        }

        private void priceTooltip(GuiGraphics pGuiGraphics, int price, int pMouseX, int pMouseY) {
            List<Component> tooltipList = new ArrayList<>(2);
            tooltipList.add(Component.translatable("gui.society_trading.hover_price_one", formatPrice(String.valueOf(price), false)));
            tooltipList.add(Component.translatable("gui.society_trading.hover_price_two").withStyle(ChatFormatting.GREEN));

            pGuiGraphics.renderTooltip(AutoTraderScreen.this.font, tooltipList, Items.ACACIA_FENCE.getDefaultInstance().getTooltipImage(), pMouseX, pMouseY);
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            if (this.isHovered && AutoTraderScreen.this.menu.getOffers().size() > this.index + AutoTraderScreen.this.scrollOff) {
                boolean noBalance = !AutoTraderScreen.this.menu.cardSlotHasCard();
                ShopOffer offer = AutoTraderScreen.this.menu.getOffers().get(this.index + AutoTraderScreen.this.scrollOff);
                ItemStack itemstack1 = offer.getCostA();
                ItemStack itemstack2 = offer.getCostB();
                ItemStack rightMostStack = itemstack1;
                boolean renderPrice = SocietyTrading.NUMISMATICS_INSTALLED && offer.hasNumismaticsCost() && !noBalance;

                // Determining order of the tooltip since items shift to the right when Numismatics value provided and player has a balance
                if (renderPrice) {
                    if (!itemstack2.isEmpty()) {
                        if (itemstack2.is(NumismaticsTags.AllItemTags.COINS.tag)) {
                            itemstack2 = ItemStack.EMPTY;
                        } else {
                            rightMostStack = itemstack2;
                            itemstack2 = ItemStack.EMPTY;
                        }
                    }
                    if (rightMostStack.is(NumismaticsTags.AllItemTags.COINS.tag)) {
                        rightMostStack = ItemStack.EMPTY;
                    }
                }
                if (pMouseX < this.getX() + 20) {
                    ItemStack itemstack = offer.getResult();
                    pGuiGraphics.renderTooltip(AutoTraderScreen.this.font, itemstack, pMouseX, pMouseY);
                } else if (pMouseX > this.getX() + 69 && pMouseX < this.getX() + 85) {
                    if (!itemstack2.isEmpty()) {
                        pGuiGraphics.renderTooltip(AutoTraderScreen.this.font, itemstack2, pMouseX, pMouseY);
                    }
                } else if (pMouseX > this.getX() + 85) {
                    if (!rightMostStack.isEmpty()) {
                        pGuiGraphics.renderTooltip(AutoTraderScreen.this.font, rightMostStack, pMouseX, pMouseY);
                    } else if (renderPrice) {
                        this.priceTooltip(pGuiGraphics, offer.getNumismaticsCost(), pMouseX, pMouseY);
                    }
                }
                if (renderPrice && pMouseX > this.getX() + 25 && pMouseX < this.getX() + 85) {
                    this.priceTooltip(pGuiGraphics, offer.getNumismaticsCost(), pMouseX, pMouseY);
                }
            }

        }
    }
}
