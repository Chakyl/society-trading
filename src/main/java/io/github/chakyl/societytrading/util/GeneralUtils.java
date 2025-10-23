package io.github.chakyl.societytrading.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GeneralUtils {
    public static void nameTagEntity(ItemStack pStack, Player pPlayer, LivingEntity pTarget) {
        if (pStack.hasCustomHoverName() && !(pTarget instanceof Player)) {
            if (!pPlayer.level().isClientSide && pTarget.isAlive()) {
                pTarget.setCustomName(pStack.getHoverName());
                if (pTarget instanceof Mob) {
                    ((Mob) pTarget).setPersistenceRequired();
                }
                pStack.shrink(1);
            }
        }
    }
}
