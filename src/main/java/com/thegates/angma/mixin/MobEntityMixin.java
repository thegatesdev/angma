package com.thegates.angma.mixin;

import com.thegates.angma.Saver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
public abstract class MobEntityMixin extends LivingEntity{

    // Stop target of any mobEntity.

    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    @Shadow private LivingEntity target;

    @Shadow @Nullable public abstract LivingEntity getTarget();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", cancellable = true)
    private void setTargetInject(LivingEntity target, CallbackInfo info) {
        System.out.println("set target");
        LivingEntity prevTarget = getTarget();
        // Not on client or no target, return.
        if (getEntityWorld().isClient() || target == null) {return;}


        if (Saver.angerDisabled(target, this)) {
            setTarget(prevTarget);
            setAttacker(prevTarget);
            info.cancel();
        }

    }

    @Inject(method = "getTarget()Lnet/minecraft/entity/LivingEntity;", at = @At("HEAD"), cancellable = true)
    public void getTargetInject(CallbackInfoReturnable<LivingEntity> cir){
        LivingEntity targetEntity = this.target;
        if (getEntityWorld().isClient() || targetEntity == null) {return;}


        if (!Saver.angerDisabled(targetEntity, this)) {return;}



        this.target = null;

        cir.setReturnValue(null);
    }

}