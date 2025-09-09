package io.github.chakyl.societytrading.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.chakyl.societytrading.SocietyTrading;
import io.github.chakyl.societytrading.data.Shop;
import io.github.chakyl.societytrading.network.ServerBoundOpenShopMenuPacket;
import io.github.chakyl.societytrading.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class SelectorScreen extends AbstractContainerScreen<SelectorMenu> {
    /**
     * The GUI texture for the villager merchant GUI.
     */
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(SocietyTrading.MODID, "textures/gui/selector.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int LABEL_Y = 6;
    private static final int NUMBER_OF_SHOP_BUTTONS = 6;
    private static final int SHOP_BUTTON_HEIGHT = 20;
    private static final int SHOP_BUTTON_WIDTH = 137;
    private static final int SCROLLER_HEIGHT = 27;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = SHOP_BUTTON_HEIGHT * NUMBER_OF_SHOP_BUTTONS;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_START_X = 146;
    private static final Component SHOP_LABEL = Component.translatable("gui.society_trading.shop_selector.title");
    private int shopItem;
    private final ShopSelectorButton[] shopSelectorButtons = new ShopSelectorButton[NUMBER_OF_SHOP_BUTTONS];
    int scrollOff;
    private boolean isDragging;

    public SelectorScreen(SelectorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 159;
        this.imageHeight = 145;
    }

    private void postButtonClick() {
        Collection<Shop> shops = this.menu.getShops();
        int searchIndex = 0;
        for(Shop shop: shops) {
            if (searchIndex == this.shopItem) {
                PacketHandler.sendToServer(new ServerBoundOpenShopMenuPacket(shop.shopID()));
            }
            searchIndex++;
        }

    }

    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 18;

        for(int l = 0; l < NUMBER_OF_SHOP_BUTTONS; ++l) {
            this.shopSelectorButtons[l] = this.addRenderableWidget(new SelectorScreen.ShopSelectorButton(i + 8, k, l, (button) -> {
                if (button instanceof SelectorScreen.ShopSelectorButton) {
                    this.shopItem = ((SelectorScreen.ShopSelectorButton)button).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            k += 20;
        }

    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int centralX = (5 - this.font.width(SHOP_LABEL) / 2) + 18;
        pGuiGraphics.drawString(this.font, SHOP_LABEL, centralX, LABEL_Y, 4210752, false);
    }

    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(GUI_LOCATION, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        Collection<Shop> shops = this.menu.getShops();
        if (!shops.isEmpty()) {
            int k = this.shopItem;
            if (k < 0 || k >= shops.size()) {
                return;
            }
        }
    }

    private void renderScroller(GuiGraphics pGuiGraphics, int pPosX, int pPosY, Collection<Shop> pShops) {
        int i = pShops.size() + 1 - NUMBER_OF_SHOP_BUTTONS;
        if (i > 1) {
            int j = SCROLL_BAR_HEIGHT - (SCROLLER_HEIGHT + (i - 1) * SCROLL_BAR_HEIGHT / i);
            int k = j / i + SCROLL_BAR_HEIGHT / i;
            int l = SCROLL_BAR_HEIGHT - SCROLLER_HEIGHT;
            int i1 = Math.min(l, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = l;
            }
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y + i1, 0, 160.0F, 0.0F, SCROLLER_WIDTH, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            pGuiGraphics.blit(GUI_LOCATION, pPosX + SCROLL_BAR_START_X, pPosY + SCROLL_BAR_TOP_POS_Y, 0, 199, 0.0F, SCROLLER_WIDTH, SCROLLER_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
        Collection<Shop> shops = this.menu.getShops();
        if (!shops.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 4 + 1;
            int l = i + 16;
            this.renderScroller(pGuiGraphics, i, j, shops);
            int i1 = 0;

            for (Shop shop : shops) {
                if (!this.canScroll(shops.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_SHOP_BUTTONS + this.scrollOff) {
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int j1 = k + 14;
                    pGuiGraphics.drawString(this.font, shop.name(), l , j1 + 5, 16777215, true);
                    pGuiGraphics.pose().popPose();
                    k += 20;
                    ++i1;
                } else {
                    ++i1;
                }
            }

            for (SelectorScreen.ShopSelectorButton button : this.shopSelectorButtons) {
                if (button != null) button.visible = button.index < this.menu.getShops().size();
            }
            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    private boolean canScroll(int pNumOffers) {
        return pNumOffers > NUMBER_OF_SHOP_BUTTONS;
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
        int i = this.menu.getShops().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_SHOP_BUTTONS;
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
        int i = this.menu.getShops().size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - NUMBER_OF_SHOP_BUTTONS;
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
        if (this.canScroll(this.menu.getShops().size()) && pMouseX > (double) (i + SCROLL_BAR_START_X) && pMouseX < (double) (i + SCROLL_BAR_START_X + 6) && pMouseY > (double) (j + SCROLL_BAR_TOP_POS_Y) && pMouseY <= (double) (j + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @OnlyIn(Dist.CLIENT)
    class ShopSelectorButton extends Button {
        final int index;

        public ShopSelectorButton(int pX, int pY, int pIndex, OnPress pOnPress) {
            super(pX, pY, SHOP_BUTTON_WIDTH, SHOP_BUTTON_HEIGHT, CommonComponents.EMPTY, pOnPress, DEFAULT_NARRATION);
            this.index = pIndex;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }
    }
}
