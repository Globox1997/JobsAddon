package net.jobsaddon.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsClientPacket;
import net.levelz.init.KeyInit;
import net.libz.api.Tab;
import net.libz.util.DrawTabHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class JobScreen extends Screen implements Tab {

    private static final Identifier BACKGROUND_TEXTURE = new Identifier("jobsaddon:textures/gui/job_background.png");
    private static final Identifier ICON_TEXTURES = new Identifier("jobsaddon:textures/gui/icons.png");
    private static final Identifier JOB_BUTTON_TEXTURES = new Identifier("jobsaddon:textures/gui/job_buttons.png");
    // Specific order for rendering
    private static final List<String> jobNames = new ArrayList<String>(List.of("lumberjack", "farmer", "builder", "fisher", "miner", "warrior", "smither", "brewer"));

    private final WidgetButtonPage[] jobButtons = new WidgetButtonPage[8];
    @Nullable
    private JobsManager jobsManager = null;

    private int backgroundWidth = 200;
    private int backgroundHeight = 215;
    private int x;
    private int y;

    private boolean hadEmployedTimer = false;

    public JobScreen() {
        super(Text.translatable("screen.jobsaddon.jobs_screen"));
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        this.jobsManager = this.client != null && this.client.player != null ? ((JobsManagerAccess) this.client.player).getJobsManager() : null;

        int l = 7;
        int o = 0;
        for (int i = 0; i < this.jobButtons.length; i++) {
            final int jobInt = i;
            this.jobButtons[i] = this.addDrawableChild(new WidgetButtonPage(this.x + l, this.y + 47 + 41 * i - o, button -> {
                if (button.active) {
                    boolean isEmployed = ((WidgetButtonPage) button).isEmployedButton();
                    if (isEmployed) {
                        this.jobsManager.quitJob(jobNames.get(jobInt));
                    } else {
                        this.jobsManager.employJob(jobNames.get(jobInt));
                        this.jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
                    }
                    JobsClientPacket.writeC2SSelectJobPacket(jobsManager, jobNames.get(jobInt), !isEmployed);
                    ((WidgetButtonPage) button).setEmployedButton(!isEmployed);

                    for (int k = 0; k < this.jobButtons.length; k++) {
                        this.jobButtons[k].active = (!this.jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || this.jobsManager.isEmployedJob(jobNames.get(k));
                    }
                }
            }));
            if (this.jobsManager != null) {
                this.jobButtons[i].setEmployedButton(this.jobsManager.isEmployedJob(jobNames.get(i)));
                this.jobButtons[i].active = this.jobsManager.canEmployJob(jobNames.get(i)) || this.jobsManager.isEmployedJob(jobNames.get(i));
            }
            if (i == 3) {
                l = 102;
                o = 164;
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        DrawableHelper.drawTexture(matrices, i, j, this.getZOffset(), 0.0f, 0.0f, this.backgroundWidth, this.backgroundHeight, 256, 256);

        super.render(matrices, mouseX, mouseY, partialTicks);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderTexture(0, ICON_TEXTURES);

        // render title
        if (this.client.player != null) {
            Text title = Text.translatable("text.jobsaddon.gui.title", this.client.player.getName().getString());
            this.textRenderer.draw(matrices, title, this.x + this.backgroundWidth / 2 - this.client.textRenderer.getWidth(title) / 2, this.y + 7, 0x3F3F3F);
        }
        // render time label
        Text timeText = null;
        Text employedText = null;
        if (jobsManager != null) {
            if (jobsManager.getEmployedJobTime() > 0) {
                int seconds = jobsManager.getEmployedJobTime() / 20;
                String string;
                if (seconds >= 3600) {
                    string = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
                } else {
                    string = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                }
                if (!this.hadEmployedTimer) {
                    this.hadEmployedTimer = true;
                }
                timeText = Text.of(Text.translatable("text.jobsaddon.employedTime").getString() + string);
            } else {
                if (this.hadEmployedTimer) {
                    this.hadEmployedTimer = false;
                    for (int k = 0; k < this.jobButtons.length; k++) {
                        this.jobButtons[k].active = !jobsManager.hasMaxEmployedJobs() || this.jobsManager.isEmployedJob(jobNames.get(k));
                    }
                }
                timeText = Text.of(Text.translatable("text.jobsaddon.employedTime").getString() + "00:00");
            }

            if (jobsManager.getEmployedJobs() != null && jobsManager.getEmployedJobs().size() > 0) {
                if (jobsManager.getEmployedJobs().size() > 2) {
                    employedText = Text.translatable("text.jobsaddon.tooManyEmployedJobs", getJobTitle(jobsManager.getEmployedJobs().get(0)), getJobTitle(jobsManager.getEmployedJobs().get(1)));
                } else if (jobsManager.getEmployedJobs().size() == 1) {
                    employedText = Text.translatable("text.jobsaddon.employedJob", getJobTitle(jobsManager.getEmployedJobs().get(0)));
                } else {
                    employedText = Text.translatable("text.jobsaddon.employedJobs", getJobTitle(jobsManager.getEmployedJobs().get(0)), getJobTitle(jobsManager.getEmployedJobs().get(1)));
                }
            } else {
                employedText = Text.translatable("text.jobsaddon.notEmployed");
            }
        }
        if (timeText != null) {
            this.textRenderer.draw(matrices, timeText, this.x + 12, this.y + 20, 0x3F3F3F);
        }
        if (employedText != null) {
            this.textRenderer.draw(matrices, employedText, this.x + 12, this.y + 33, 0x3F3F3F);
        }

        RenderSystem.setShaderTexture(0, ICON_TEXTURES);
        for (int o = 0; o < 2; o++)
            for (int u = 0; u < 4; u++) {
                int jobInt = (o != 0 ? 4 : 0) + u;
                int xPos = this.x + (jobInt > 3 ? 95 : 0);
                int yPos = this.y + jobInt * 41 - (jobInt > 3 ? 164 : 0);
                // render xp bar background
                this.drawTexture(matrices, xPos + 12, yPos + 76, 0, 0, 81, 5);

                if (jobsManager != null) {

                    String jobName = jobNames.get(jobInt);
                    int jobXP = jobsManager.getJobXP(jobName);
                    // render job title
                    this.textRenderer.draw(matrices, this.getJobTitle(jobName), xPos + 30, yPos + 51, 0xFFFFFF);
                    // render job levels
                    this.textRenderer.draw(matrices, Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel(jobName), ConfigInit.CONFIG.jobMaxLevel), xPos + 42, yPos + 66, 0xFFFFFF);
                    RenderSystem.setShaderTexture(0, ICON_TEXTURES);
                    // render icons
                    this.drawTexture(matrices, xPos + 12, yPos + 52, jobInt * 14, 10, 14, 14);
                    if (this.isPointWithinBounds(xPos + 12 - this.x, yPos + 52 - this.y, 14, 14, mouseX, mouseY)) {
                        List<Text> tooltip = new ArrayList<>();
                        this.getJobTooltip(jobName).forEach((text) -> {
                            tooltip.add(Text.of(text));
                        });
                        this.renderTooltip(matrices, tooltip, mouseX, mouseY);
                    }
                    RenderSystem.setShaderTexture(0, ICON_TEXTURES);
                    // render xp bar
                    if (jobsManager.getNextJobLevelExperience(jobName) > 0 && jobXP > 0) {
                        this.drawTexture(matrices, xPos + 12, yPos + 76, 0, 5, 80 * jobXP / jobsManager.getNextJobLevelExperience(jobName), 5);
                    }
                }
            }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        DrawTabHelper.drawTab(client, matrices, this, x, y, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int ch, int keyCode, int modifiers) {
        if (KeyInit.screenKey.matchesKey(ch, keyCode) || Objects.requireNonNull(client).options.inventoryKey.matchesKey(ch, keyCode)) {
            this.close();
            return true;
        } else {
            return super.keyPressed(ch, keyCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DrawTabHelper.onTabButtonClick(client, this, this.x, this.y, mouseX, mouseY, this.getFocused() != null);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        int i = this.x;
        int j = this.y;
        return (pointX -= (double) i) >= (double) (x - 1) && pointX < (double) (x + width + 1) && (pointY -= (double) j) >= (double) (y - 1) && pointY < (double) (y + height + 1);
    }

    private Text getJobTitle(String string) {
        switch (string) {
        case "brewer":
            return Text.translatable("text.jobsaddon.brewer");
        case "builder":
            return Text.translatable("text.jobsaddon.builder");
        case "farmer":
            return Text.translatable("text.jobsaddon.farmer");
        case "fisher":
            return Text.translatable("text.jobsaddon.fisher");
        case "lumberjack":
            return Text.translatable("text.jobsaddon.lumberjack");
        case "miner":
            return Text.translatable("text.jobsaddon.miner");
        case "smither":
            return Text.translatable("text.jobsaddon.smither");
        case "warrior":
            return Text.translatable("text.jobsaddon.warrior");
        default:
            return Text.translatable("text.jobsaddon.nonJobTitle");
        }
    }

    private List<String> getJobTooltip(String string) {
        String tooltip = switch (string) {
        case "brewer" -> Text.translatable("text.jobsaddon.brewer.tooltip").getString();
        case "builder" -> Text.translatable("text.jobsaddon.builder.tooltip").getString();
        case "farmer" -> Text.translatable("text.jobsaddon.farmer.tooltip").getString();
        case "fisher" -> Text.translatable("text.jobsaddon.fisher.tooltip").getString();
        case "lumberjack" -> Text.translatable("text.jobsaddon.lumberjack.tooltip").getString();
        case "miner" -> Text.translatable("text.jobsaddon.miner.tooltip").getString();
        case "smither" -> Text.translatable("text.jobsaddon.smither.tooltip").getString();
        case "warrior" -> Text.translatable("text.jobsaddon.warrior.tooltip").getString();
        default -> Text.translatable("text.jobsaddon.nonJobTitle.tooltip").getString();
        };
        return List.of(tooltip.split("\n"));
    }

    private class WidgetButtonPage extends ButtonWidget {

        private List<Text> tooltip = new ArrayList<Text>();
        private boolean employed = false;

        public WidgetButtonPage(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 91, 38, ScreenTexts.EMPTY, onPress);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, JOB_BUTTON_TEXTURES);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);

            int i = this.getYImage(this.isHovered());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            if (isEmployedButton()) {
                i = 3;
                if (this.isHovered()) {
                    i = 4;
                }
            }

            this.drawTexture(matrices, this.x, this.y, 0, i * 38, this.width, this.height);

            if (this.isHovered()) {
                this.renderTooltip(matrices, mouseX, mouseY);
            }
        }

        @Override
        public void renderTooltip(MatrixStack matrices, int mouseX, int mouseY) {
            if (!tooltip.isEmpty()) {
                JobScreen.this.renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }

        public void setEmployedButton(boolean employed) {
            this.employed = employed;
        }

        public boolean isEmployedButton() {
            return this.employed;
        }

    }

}
