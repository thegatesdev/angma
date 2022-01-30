package com.thegates.angma.mixin;

import com.thegates.angma.Saver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinBruteBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PiglinBruteBrain.class)
public class PiglinBruteBrainMixin {

    @Inject(method = "method_30256", at = @At("RETURN"))
    private static void tickActivitiesMixin(PiglinBruteEntity piglinBrute, CallbackInfo ci){
        Optional<LivingEntity> optional = piglinBrute.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isPresent() && Saver.angerDisabled(optional.get(), piglinBrute)){
            piglinBrute.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            piglinBrute.setAttacking(false);
        }
    }
}
