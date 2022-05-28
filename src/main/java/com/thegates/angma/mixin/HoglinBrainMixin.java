package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.HoglinBrain;
import net.minecraft.entity.mob.HoglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(HoglinBrain.class)
public class HoglinBrainMixin {

    @Inject(method = "refreshActivities", at = @At("RETURN"))
    private static void refreshActivitiesInject(HoglinEntity hoglin, CallbackInfo ci) {
        Optional<LivingEntity> optional = hoglin.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isPresent() && Main.getAngerRegister().isAngerDisabled(optional.get(), hoglin)) {
            hoglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            hoglin.setAttacking(false);
        }
    }
}
