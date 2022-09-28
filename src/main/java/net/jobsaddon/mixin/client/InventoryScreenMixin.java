package net.jobsaddon.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jobsaddon.gui.JobsGui;
import net.jobsaddon.gui.JobsScreen;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.RenderInit;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(value = InventoryScreen.class, priority = 999)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void mouseClickedMixin(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> info) {
        if (this.client != null && ConfigInit.CONFIG.inventoryButton && this.focusedSlot == null && this.isPointWithinBounds(52, -20, 22, 19, (double) mouseX, (double) mouseY))
            this.client.setScreen(new JobsScreen(new JobsGui(client)));
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    protected void drawBackgroundMixin(MatrixStack matrices, float delta, int mouseX, int mouseY, CallbackInfo info) {
        if (ConfigInit.CONFIG.inventoryButton) {
            RenderSystem.setShaderTexture(0, RenderInit.JOB_GUI_ICONS);
            this.drawTexture(matrices, this.x + 50, this.y - 21, 0, 10, 24, 21);

            if (this.isPointWithinBounds(52, -20, 22, 19, (double) mouseX, (double) mouseY))
                this.renderTooltip(matrices, Text.translatable("screen.jobsaddon.jobs_screen"), mouseX, mouseY);
        }
    }
}
