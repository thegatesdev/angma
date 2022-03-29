package com.thegates.angma.mixin;

import com.thegates.angma.Main;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
public abstract class SlimeEntityMixin extends MobEntity {

    protected SlimeEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    public void onPlayerCollisionInject(PlayerEntity player, CallbackInfo ci){
        if (Main.getSaver().angerDisabled(this, player)){
            ci.cancel();
        }
    }
}
