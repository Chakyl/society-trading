package io.github.chakyl.societytrading.events;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.framework.api.event.TickEvents;
import io.github.chakyl.societytrading.integration.ControllableIntegration;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientControllableEvents {
    public static void registerClientTick() {
        TickEvents.START_CLIENT.register(ClientControllableEvents::onClientTick);
    }

    public static void onClientTick() {
        Controller controller = Controllable.getController();
        if (controller == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            return;
        }

        ControllableIntegration.handleScroll(mc, controller);

    }

}