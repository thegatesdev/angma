package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RevengeGoal.class)
public abstract class RevengeGoalMixin extends TrackTargetGoal {

    public RevengeGoalMixin(MobEntity mob, boolean checkVisibility) {
        super(mob, checkVisibility);
    }

    @Inject(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getAttacker()Lnet/minecraft/entity/LivingEntity;", shift = At.Shift.AFTER), cancellable = true)
    public void canStartInject(CallbackInfoReturnable<Boolean> cir) {
        if (Main.getAngerRegister().isAngerDisabled(mob.getAttacker(), mob)) {
            cir.setReturnValue(false);
        }
    }
}
