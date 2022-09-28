package net.jobsaddon.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LibGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jobsaddon.gui.JobsGui;
import net.jobsaddon.gui.JobsScreen;
import net.jobsaddon.init.RenderInit;
import net.levelz.gui.LevelzScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(LevelzScreen.class)
public abstract class LevelzScreenMixin extends CottonClientScreen {

    public LevelzScreenMixin(GuiDescription description) {
        super(description);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V", ordinal = 1, shift = Shift.BEFORE))
    private void renderMixin(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        RenderSystem.setShaderTexture(0, RenderInit.JOB_GUI_ICONS);
        if (LibGui.isDarkMode())
            this.drawTexture(matrices, this.left + 50, this.top - 21, 48, 10, 24, 21);
        else
            this.drawTexture(matrices, this.left + 50, this.top - 21, 0, 10, 24, 21);

        if (this.isPointWithinIconBounds(50, 23, (double) mouseX, (double) mouseY))
            this.renderTooltip(matrices, Text.translatable("screen.jobsaddon.jobs_screen"), mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClickedMixin(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> info) {
        if (this.client != null && this.isPointWithinIconBounds(50, 23, (double) mouseX, (double) mouseY))
            this.client.setScreen(new JobsScreen(new JobsGui(client)));
    }

    @Shadow
    private boolean isPointWithinIconBounds(int x, int width, double pointX, double pointY) {
        return false;
    }
}
