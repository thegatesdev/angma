package com.thegates.angma.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    // Fix for crash with calling for help when hit.

    @Inject(method = "isTeammate(Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    public void isTeamateInject(Entity other, CallbackInfoReturnable<Boolean> cir) {
        // Other could not be null before because they got hit.
        // Since I am setting the target to null if anger is disabled, this can now be null.
        if (other == null) {
            cir.setReturnValue(false);
        }
    }
}
