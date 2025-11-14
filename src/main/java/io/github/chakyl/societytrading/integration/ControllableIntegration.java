package io.github.chakyl.societytrading.integration;

import com.mrcrayfish.controllable.client.Thumbstick;
import com.mrcrayfish.controllable.client.input.Controller;
import io.github.chakyl.societytrading.screen.SelectorScreen;
import io.github.chakyl.societytrading.screen.ShopScreen;
import io.github.chakyl.societytrading.util.ControllableUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ControllableIntegration {
    private static long lastScroll = 0;


    public static void handleScroll(Minecraft mc, Controller controller) {
        double dir = 0;
        float yValue =
                com.mrcrayfish.controllable.Config.CLIENT.client.options.cursorThumbstick.get() == Thumbstick.LEFT ?
                        controller.getRThumbStickYValue() :
                        controller.getLThumbStickYValue();
        if (Math.abs(yValue) >= 0.5f) {
            dir = -yValue;
        } else {
            ControllableIntegration.lastScroll = 0;
        }
        long scrollTime = Util.getMillis();

        if (mc.screen instanceof ShopScreen || mc.screen instanceof SelectorScreen ) {
            if (dir != 0 && scrollTime - ControllableIntegration.lastScroll >= 150) {
                mc.screen.mouseScrolled(ControllableUtils.getCursorX(), ControllableUtils.getCursorY(), dir);
                ControllableIntegration.lastScroll = scrollTime;
            }
        }
    }


}