package io.github.chakyl.societytrading.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.network.PacketHandler;
import io.github.chakyl.societytrading.network.ServerBoundTradeButtonClickPacket;
import io.github.chakyl.societytrading.network.ServerBoundTriggerBalanceSyncPacket;
import io.github.chakyl.societytrading.trading.ShopOffer;
import io.github.chakyl.societytrading.trading.ShopOffers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static io.github.chakyl.societytrading.util.ShopData.formatPrice;

@OnlyIn(Dist.CLIENT)
public class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    /**
     * The GUI texture for the villager merchant GUI.
     */
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(SocietyTrading.MODID, "textures/gui/shop.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int SELL_ITEM_1_X = 5;
    private static final int SELL_ITEM_2_X = 35;
    private static final int BUY_ITEM_X = 68;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_OFFER_BUTTONS = 4;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_HEIGHT = 22;
    private static final int TRADE_BUTTON_WIDTH = 185;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = TRADE_BUTTON_HEIGHT * NUMBER_OF_OFFER_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 274;
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
    private int shopItem;
    private final ShopScreen.TradeOfferButton[] tradeOfferButtons = new ShopScreen.TradeOfferButton[NUMBER_OF_OFFER_BUTTONS];
    int scrollOff;
    private boolean isDragging;

    public ShopScreen(ShopMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 288;
        this.imageHeight = 204;
    }

    private void postButtonClick() {
//        this.menu.setSelectionHint(this.shopItem);
        PacketHandler.sendToServer(new ServerBoundTradeButtonClickPacket((byte) this.shopItem));
    }

    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 18;

        PacketHandler.sendToServer(new ServerBoundTriggerBalanceSyncPacket());
        for (int l = 0; l < NUMBER_OF_OFFER_BUTTONS; ++l) {
            this.tradeOfferButtons[l] = this.addRenderableWidget(new ShopScreen.TradeOfferButton(i + 88, k, l, (button) -> {
                if (button instanceof ShopScreen.TradeOfferButton) {
                    this.shopItem = ((ShopScreen.TradeOfferButton) button).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            k += TRADE_BUTTON_HEIGHT;
        }

    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int centralX = (5 - this.font.width(TRADES_LABEL) / 2) + 102;
        pGuiGraphics.drawString(this.font, this.title, 6, LABEL_Y, 4210752, false);
        pGuiGraphics.drawString(this.font, TRADES_LABEL, centralX, LABEL_Y, 4210752, false);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, centralX, 110, 4210752, false);
        if (SocietyTrading.NUMISMATICS_INSTALLED) {
            Component priceStr = Component.translatable("gui.society_trading.balance", "§0" + formatPrice(Integer.valueOf(this.menu.getPlayerBalance()).toString(), false));
            pGuiGraphics.drawString(this.font, priceStr, (centralX * 3) - font.width(priceStr) + 6, LABEL_Y, 16777215, false);

        }
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GUI_LOCATION, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        pGuiGraphics.blit(new ResourceLocation(this.menu.getTexture() + ".png"), i + 6, j + 18, 0, 0.0F, 0.0F, 64, 64, 64, 64);
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
        if (!shopOffers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 4 + 1;
            int l = i + 86 + NUMBER_OF_OFFER_BUTTONS;
            this.renderScroller(pGuiGraphics, i, j, shopOffers);
            int i1 = 0;

            for (ShopOffer shopOffer : shopOffers) {
                if (!this.canScroll(shopOffers.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_OFFER_BUTTONS + this.scrollOff) {
                    ItemStack itemstack1 = shopOffer.getCostA();
                    ItemStack itemstack2 = shopOffer.getCostB();
                    ItemStack itemstack3 = shopOffer.getResult();
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int j1 = k + 16;
                    int numismaticOffset = 0;
                    int priceOffset = 5;
                    if (shopOffer.hasNumismaticsCost()) numismaticOffset = l + TRADE_BUTTON_WIDTH - 21;

                    if (!itemstack1.is(NumismaticsTags.AllItemTags.COINS.tag)) {
                        this.renderAndDecorateCostA(pGuiGraphics, itemstack1, itemstack1, numismaticOffset > 0 ? numismaticOffset : l + TRADE_BUTTON_WIDTH - 21, j1);
                        priceOffset += 18;
                    }
                    if (!itemstack2.isEmpty() && !itemstack2.is(NumismaticsTags.AllItemTags.COINS.tag)) {
                        pGuiGraphics.renderFakeItem(itemstack2, numismaticOffset > 0 ? numismaticOffset : i + TRADE_BUTTON_WIDTH + 52, j1);
                        pGuiGraphics.renderItemDecorations(this.font, itemstack2, numismaticOffset > 0 ? numismaticOffset : i + TRADE_BUTTON_WIDTH + 52, j1);
                        priceOffset += 18;
                    }
                    if (shopOffer.hasNumismaticsCost()) {
                        Component priceStr = Component.translatable("gui.society_trading.price", formatPrice(Integer.valueOf(shopOffer.getNumismaticsCost()).toString()));
                        pGuiGraphics.drawString(this.font, priceStr, l + TRADE_BUTTON_WIDTH - font.width(priceStr) - priceOffset, j1 + 4, 16777215, true);

                    }
                    // TODO: render item costs to the right of bank account costs
                    //result
                    int lineLength = 96;
                    Component itemName = itemstack3.getHoverName();
                    boolean oneLine = this.font.split(itemName, lineLength).size() == 1;
                    pGuiGraphics.renderFakeItem(itemstack3, l + 1, j1);
                    pGuiGraphics.renderItemDecorations(this.font, itemstack3, l + 1, j1);
                    pGuiGraphics.drawWordWrap(this.font, itemName.plainCopy().withStyle(ChatFormatting.DARK_GRAY), l + 16 + 5, j1 + (oneLine ? 5 : 1), lineLength, 4210752);
                    pGuiGraphics.drawWordWrap(this.font, itemName.plainCopy().withStyle(ChatFormatting.WHITE), l + 16 + 4, j1 + (oneLine ? 4 : 0), lineLength, 16777215);
                    pGuiGraphics.pose().popPose();
                    k += TRADE_BUTTON_HEIGHT;
                    ++i1;
                } else {
                    ++i1;
                }
            }

            for (ShopScreen.TradeOfferButton ShopScreen$tradeofferbutton : this.tradeOfferButtons) {
                if (ShopScreen$tradeofferbutton.isHoveredOrFocused()) {
                    ShopScreen$tradeofferbutton.renderToolTip(pGuiGraphics, pMouseX, pMouseY);
                }

                ShopScreen$tradeofferbutton.visible = ShopScreen$tradeofferbutton.index < this.menu.getOffers().size();
            }

            RenderSystem.enableDepthTest();
        }

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
            font.drawInBatch(count, (float) (pX + 14) + 19 - 2 - font.width(count), (float) pY + 6 + 3, 0xFFFFFF, true, pGuiGraphics.pose().last().pose(), pGuiGraphics.bufferSource(), net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880, false);
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

        public TradeOfferButton(int pX, int pY, int pIndex, Button.OnPress pOnPress) {
            super(pX, pY, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
            this.index = pIndex;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            if (this.isHovered && ShopScreen.this.menu.getOffers().size() > this.index + ShopScreen.this.scrollOff) {
                if (pMouseX < this.getX() + 20) {
                    ItemStack itemstack = ShopScreen.this.menu.getOffers().get(this.index + ShopScreen.this.scrollOff).getResult();
                    pGuiGraphics.renderTooltip(ShopScreen.this.font, itemstack, pMouseX, pMouseY);
                } else if (pMouseX < this.getX() + 165 && pMouseX > this.getX() + 148) {
                    ItemStack itemstack2 = ShopScreen.this.menu.getOffers().get(this.index + ShopScreen.this.scrollOff).getCostB();
                    if (!itemstack2.isEmpty()) {
                        pGuiGraphics.renderTooltip(ShopScreen.this.font, itemstack2, pMouseX, pMouseY);
                    }
                } else if (pMouseX > this.getX() + 164) {
                    ItemStack itemstack1 = ShopScreen.this.menu.getOffers().get(this.index + ShopScreen.this.scrollOff).getCostA();
                    pGuiGraphics.renderTooltip(ShopScreen.this.font, itemstack1, pMouseX, pMouseY);
                }
            }

        }
    }
}
