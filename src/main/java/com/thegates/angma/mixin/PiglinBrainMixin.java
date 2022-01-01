package com.thegates.angma.mixin;

import com.thegates.angma.Saver;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @Inject(method = "becomeAngryWith(Lnet/minecraft/entity/mob/AbstractPiglinEntity;Lnet/minecraft/entity/LivingEntity;)V", at = @At("HEAD"), cancellable = true)
    private static void becomeAngryWithMixin(AbstractPiglinEntity piglin, LivingEntity target, CallbackInfo ci){
        System.out.println("1");
        //TODO Inject after if statement for performance?
        if (Sensor.testAttackableTargetPredicateIgnoreVisibility(piglin, target)){
            System.out.println("2");
            if (Saver.angerDisabled(target, piglin)){
                System.out.println("Disabled anger!");
                ci.cancel();
            }
        }
    }

    @Inject(method = "tickActivities(Lnet/minecraft/entity/mob/PiglinEntity;)V", at = @At(value = "INVOKE", target = "net/minecraft/entity/mob/PiglinEntity.setAttacking(Z)V"))
    private static void tickActivitiesInject(@NotNull PiglinEntity piglin, CallbackInfo ci){
        piglin.setAttacking(false);
    }
}
