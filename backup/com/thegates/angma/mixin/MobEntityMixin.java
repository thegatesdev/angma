package com.thegates.angma.mixin;

import com.thegates.angma.EntityHelper;
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

    @Shadow public abstract @Nullable LivingEntity getTarget();

    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "setTarget(Lnet/minecraft/entity/LivingEntity;)V", cancellable = true)
    private void setTarget(LivingEntity target, CallbackInfo info) {
        // Not on client or no target, return.
        if (getEntityWorld().isClient() || target == null) {return;}


        if (Saver.hasAngerDisabled(target, EntityHelper.getMobId(this))) {
            setTarget(null);
            setAttacker(null);
            info.cancel();
        }

    }

    @Inject(at = @At("TAIL"), method = "getTarget()Lnet/minecraft/entity/LivingEntity;", cancellable = true)
    public void getTarget(CallbackInfoReturnable<LivingEntity> cir){
        LivingEntity target = getTarget();
        if (getEntityWorld().isClient() || target == null) {return;}


        if (!Saver.hasAngerDisabled(target, EntityHelper.getMobId(this))) {return;}

        setTarget(null);
        setAttacker(null);

        cir.setReturnValue(null);

    }


}