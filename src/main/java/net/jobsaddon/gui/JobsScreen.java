package net.jobsaddon.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LibGui;
import net.fabricmc.api.Environment;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.RenderInit;
import net.jobsaddon.jobs.JobsManager;
import net.fabricmc.api.EnvType;
import net.levelz.compat.InventorioScreenCompatibility;
import net.levelz.gui.LevelzGui;
import net.levelz.gui.LevelzScreen;
import net.levelz.init.KeyInit;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JobsScreen extends CottonClientScreen {

    // Specific order for rendering
    private final List<String> jobNames = new ArrayList<String>(List.of("lumberjack", "farmer", "builder", "fisher", "miner", "warrior", "smither", "brewer"));
    @Nullable
    private JobsManager jobsManager;

    public JobsScreen(GuiDescription description) {
        super(description);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.render(matrices, mouseX, mouseY, partialTicks);

        RenderSystem.setShaderTexture(0, net.levelz.init.RenderInit.GUI_ICONS);
        if (LibGui.isDarkMode()) {
            // bag icon
            this.drawTexture(matrices, this.left, this.top - 21, 120, 110, 24, 25);
            // skill icon
            this.drawTexture(matrices, this.left + 25, this.top - 21, 168, 110, 24, 21);
        } else {
            // bag icon
            this.drawTexture(matrices, this.left, this.top - 21, 24, 110, 24, 25);
            // skill icon
            this.drawTexture(matrices, this.left + 25, this.top - 21, 48, 110, 24, 21);
        }
        RenderSystem.setShaderTexture(0, RenderInit.JOB_GUI_ICONS);
        if (LibGui.isDarkMode())
            this.drawTexture(matrices, this.left + 50, this.top - 23, 72, 10, 24, 27);
        else
            this.drawTexture(matrices, this.left + 50, this.top - 23, 24, 10, 24, 27);

        if (this.isPointWithinIconBounds(1, 23, (double) mouseX, (double) mouseY))
            this.renderTooltip(matrices, Text.translatable("container.inventory"), mouseX, mouseY);
        if (this.isPointWithinIconBounds(26, 23, (double) mouseX, (double) mouseY))
            this.renderTooltip(matrices, Text.translatable("screen.levelz.skill_screen"), mouseX, mouseY);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, RenderInit.JOB_GUI_ICONS);

        int scaledWidth = this.client.getWindow().getScaledWidth() / 2;
        int scaledHeight = this.client.getWindow().getScaledHeight() / 2;

        for (int i = 0; i < 2; i++)
            for (int u = 0; u < 4; u++) {
                LevelzScreen.drawTexture(matrices, scaledWidth - 88 + i * 95, scaledHeight - 31 + u * 41, 81, 5, 0F, 0F, 81, 5, 256, 256);
                jobsManager = this.client != null && this.client.player != null ? ((JobsManagerAccess) this.client.player).getJobsManager() : null;
                if (jobsManager != null) {
                    String jobName = jobNames.get((i != 0 ? 4 : 0) + u);
                    int jobXP = jobsManager.getJobXP(jobName);

                    if (jobsManager.getNextJobLevelExperience(jobName) > 0 && jobXP > 0)
                        LevelzScreen.drawTexture(matrices, scaledWidth - 88 + i * 95, scaledHeight - 31 + u * 41, (81 * jobXP / jobsManager.getNextJobLevelExperience(jobName)), 5, 0F, 5F,
                                (81 * jobXP / jobsManager.getNextJobLevelExperience(jobName)), 5, 256, 256);
                }
            }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.client != null) {
            if (this.isPointWithinIconBounds(1, 23, (double) mouseX, (double) mouseY)) {
                assert this.client.player != null;
                if (net.levelz.init.RenderInit.isInventorioLoaded)
                    InventorioScreenCompatibility.setInventorioScreen(client);
                else
                    this.client.setScreen(new InventoryScreen(this.client.player));
            } else if (this.isPointWithinIconBounds(26, 23, (double) mouseX, (double) mouseY))
                this.client.setScreen(new LevelzScreen(new LevelzGui(client)));
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int ch, int keyCode, int modifiers) {
        if (KeyInit.screenKey.matchesKey(ch, keyCode) || Objects.requireNonNull(client).options.inventoryKey.matchesKey(ch, keyCode)) {
            this.close();
            return true;
        } else
            return super.keyPressed(ch, keyCode, modifiers);

    }

    private boolean isPointWithinIconBounds(int x, int width, double pointX, double pointY) {
        int i = this.left;
        int j = this.top;
        return (pointX -= (double) i) >= (double) (x - 1) && pointX < (double) (x + width + 1) && (pointY -= (double) j) >= (double) (-20 - 1) && pointY < (double) (3 + 1);
    }

}