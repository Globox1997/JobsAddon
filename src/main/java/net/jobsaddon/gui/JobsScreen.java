package net.jobsaddon.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.Environment;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.RenderInit;
import net.jobsaddon.jobs.JobsManager;
import net.fabricmc.api.EnvType;
import net.levelz.init.KeyInit;
import net.libz.api.Tab;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JobsScreen extends CottonClientScreen implements Tab {

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

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, RenderInit.JOB_GUI_ICONS);

        int scaledWidth = this.client.getWindow().getScaledWidth() / 2;
        int scaledHeight = this.client.getWindow().getScaledHeight() / 2;

        for (int i = 0; i < 2; i++)
            for (int u = 0; u < 4; u++) {
                DrawableHelper.drawTexture(matrices, scaledWidth - 88 + i * 95, scaledHeight - 31 + u * 41, 81, 5, 0F, 0F, 81, 5, 256, 256);
                jobsManager = this.client != null && this.client.player != null ? ((JobsManagerAccess) this.client.player).getJobsManager() : null;
                if (jobsManager != null) {
                    String jobName = jobNames.get((i != 0 ? 4 : 0) + u);
                    int jobXP = jobsManager.getJobXP(jobName);

                    if (jobsManager.getNextJobLevelExperience(jobName) > 0 && jobXP > 0)
                        DrawableHelper.drawTexture(matrices, scaledWidth - 88 + i * 95, scaledHeight - 31 + u * 41, (81 * jobXP / jobsManager.getNextJobLevelExperience(jobName)), 5, 0F, 5F,
                                (81 * jobXP / jobsManager.getNextJobLevelExperience(jobName)), 5, 256, 256);
                }
            }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    @Override
    public boolean keyPressed(int ch, int keyCode, int modifiers) {
        if (KeyInit.screenKey.matchesKey(ch, keyCode) || Objects.requireNonNull(client).options.inventoryKey.matchesKey(ch, keyCode)) {
            this.close();
            return true;
        } else
            return super.keyPressed(ch, keyCode, modifiers);

    }

}
