package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    // Stop target of any mobEntity.


    @Shadow
    public abstract void setTarget(@Nullable LivingEntity target);


    @Shadow
    private LivingEntity target;


    @Shadow
    @Nullable
    public abstract LivingEntity getTarget();

    @Shadow
    public abstract MoveControl getMoveControl();


    @Shadow
    public abstract void setAttacking(boolean attacking);


    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(at = @At("HEAD"), method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", cancellable = true)
    private void setTargetInject(LivingEntity target, CallbackInfo info) {
        LivingEntity prevTarget = getTarget();
        // Not on client or no target, return.
        if (getEntityWorld().isClient() || target == null) {
            return;
        }


        if (Main.getSaver().angerDisabled(target, this)) {
            setTarget(prevTarget);
            setAttacker(prevTarget);
            setAttacking(false);
            getBrain().forget(MemoryModuleType.ATTACK_TARGET);
            info.cancel();
        }

    }


    @Inject(method = "getTarget()Lnet/minecraft/entity/LivingEntity;", at = @At("HEAD"), cancellable = true)
    public void getTargetInject(CallbackInfoReturnable<LivingEntity> cir) {
        LivingEntity targetEntity = this.target;
        if (getEntityWorld().isClient() || targetEntity == null) {
            return;
        }


        if (!Main.getSaver().angerDisabled(targetEntity, this)) {
            return;
        }


        this.target = null;

        cir.setReturnValue(null);
    }

}