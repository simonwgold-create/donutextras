package dev.simon.donutextras.mixin;

import dev.simon.donutextras.voice.MouthSkins;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void donutextras$openMouthWhenTalking(CallbackInfoReturnable<PlayerSkin> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		cir.setReturnValue(MouthSkins.apply(self.getUUID(), cir.getReturnValue()));
	}
}
