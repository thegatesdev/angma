package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ActiveTargetGoal.class)
public class ActiveTargetGoalMixin {
    @Shadow
    protected TargetPredicate targetPredicate;

    @Inject(method = "<init>(Lnet/minecraft/entity/mob/MobEntity;Ljava/lang/Class;IZZLjava/util/function/Predicate;)V", at = @At("RETURN"))
    private <T> void initInject(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, Predicate<LivingEntity> targetPredicateInput, CallbackInfo ci) {
        if (targetPredicate != null)
            if (targetPredicateInput == null) {
                targetPredicate.setPredicate(livingEntity -> !Main.getAngerRegister().isAngerDisabled(livingEntity, mob));
            } else {
                targetPredicate.setPredicate(targetPredicateInput.and(livingEntity -> !Main.getAngerRegister().isAngerDisabled(livingEntity, mob)));
            }
    }
}
