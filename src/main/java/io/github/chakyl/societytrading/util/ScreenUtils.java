package io.github.chakyl.societytrading.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.SeasonHelper;

public class ScreenUtils {
    public static void drawWordWrapShadow(GuiGraphics pGuiGraphics, Font pFont, FormattedText pText, int pX, int pY, int pLineWidth, int pColor) {
        for(FormattedCharSequence formattedcharsequence : pFont.split(pText, pLineWidth)) {
            pGuiGraphics.drawString(pFont, formattedcharsequence, pX, pY, pColor, true);
            pY += 9;
        }
    }
    public static int getSeasonOrder(Level level) {
        if (level != null) {
            switch (SeasonHelper.getSeasonState(level).getSeason()) {
                case SPRING -> {
                    return 0;
                }
                case SUMMER -> {
                    return 1;
                }
                case AUTUMN -> {
                    return 2;
                }
                case WINTER -> {
                    return 3;
                }
            }
        }
        return 1;
    }
}
