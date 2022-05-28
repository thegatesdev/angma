package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {

    @Inject(method = "tickActivities", at = @At(value = "RETURN"))
    private static void tickActivitiesInject(PiglinEntity piglin, CallbackInfo ci) {
        Optional<LivingEntity> optional = piglin.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isPresent() && Main.getAngerRegister().isAngerDisabled(optional.get(), piglin)) {
            piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            piglin.setAttacking(false);
        }
    }
}
