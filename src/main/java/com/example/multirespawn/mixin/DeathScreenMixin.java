package com.example.multirespawn.mixin;

import com.example.multirespawn.network.ClientPackets;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screen.DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {
    protected DeathScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void multirespawn$addChoiceButton(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(Text.literal("选择重生点"), button -> ClientPackets.requestRespawnPoints())
                .dimensions(width / 2 - 100, height / 4 + 120, 200, 20)
                .build());
    }
}
