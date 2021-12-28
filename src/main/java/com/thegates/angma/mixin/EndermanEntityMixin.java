package com.thegates.angma.mixin;

import com.thegates.angma.Saver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin extends HostileEntity {

    // Fix enderman head bug.

    protected EndermanEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "isPlayerStaring(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At("HEAD"), cancellable = true)
    void isPlayerStaringInject(PlayerEntity player, CallbackInfoReturnable<Boolean> info){
        if (Saver.angerDisabled(player, this)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At("TAIL"))
    public void setAttackerInject(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info){
        if (getAttacker() == null) {return;}

        if (Saver.angerDisabled(getAttacker(), this)){
            setAttacker(null);
        }
    }

}
