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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        super.render(context, mouseX, mouseY, delta);

        // render title
        if (this.client.player != null) {
            Text title = Text.translatable("text.jobsaddon.gui.title", this.client.player.getName().getString());
            context.drawText(this.textRenderer, title, this.x + this.backgroundWidth / 2 - this.client.textRenderer.getWidth(title) / 2, this.y + 7, 0x3F3F3F, false);
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
            context.drawText(this.textRenderer, timeText, this.x + 12, this.y + 20, 0x3F3F3F, false);
        }
        if (employedText != null) {
            context.drawText(this.textRenderer, employedText, this.x + 12, this.y + 33, 0x3F3F3F, false);
        }

        for (int o = 0; o < 2; o++)
            for (int u = 0; u < 4; u++) {
                int jobInt = (o != 0 ? 4 : 0) + u;
                int xPos = this.x + (jobInt > 3 ? 95 : 0);
                int yPos = this.y + jobInt * 41 - (jobInt > 3 ? 164 : 0);
                // render xp bar background
                context.drawTexture(ICON_TEXTURES, xPos + 12, yPos + 76, 0, 0, 81, 5);

                if (jobsManager != null) {
                    String jobName = jobNames.get(jobInt);
                    int jobXP = jobsManager.getJobXP(jobName);
                    // render job title
                    context.drawText(this.textRenderer, this.getJobTitle(jobName), xPos + 30, yPos + 51, 0xFFFFFF, false);
                    // render job levels
                    context.drawText(this.textRenderer, Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel(jobName), ConfigInit.CONFIG.jobMaxLevel), xPos + 42, yPos + 66, 0xFFFFFF,
                            false);
                    // render icons
                    context.drawTexture(ICON_TEXTURES, xPos + 12, yPos + 52, jobInt * 14, 10, 14, 14);
                    if (this.isPointWithinBounds(xPos + 12 - this.x, yPos + 52 - this.y, 14, 14, mouseX, mouseY)) {
                        List<Text> tooltip = new ArrayList<>();
                        this.getJobTooltip(jobName).forEach((text) -> {
                            tooltip.add(Text.of(text));
                        });
                        context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
                    }
                    // render xp bar
                    if (jobsManager.getNextJobLevelExperience(jobName) > 0 && jobXP > 0) {
                        context.drawTexture(ICON_TEXTURES, xPos + 12, yPos + 76, 0, 5, 80 * jobXP / jobsManager.getNextJobLevelExperience(jobName), 5);

                        if (isPointWithinBounds(xPos + 12 - this.x, yPos + 76 - this.y, 81, 5, mouseX, mouseY)) {
                            context.drawTooltip(textRenderer, Text.translatable("text.jobsaddon.jobLevelExperience", jobXP, jobsManager.getNextJobLevelExperience(jobName)), mouseX, mouseY);
                        }
                    }
                }
            }

        DrawTabHelper.drawTab(client, context, this, x, y, mouseX, mouseY);
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
        DrawTabHelper.onTabButtonClick(client, this, this.x, this.y, mouseX, mouseY, false);
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
            super(x, y, 91, 38, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int i = this.getTextureY();
            if (isEmployedButton()) {
                i = 3;
                if (this.isHovered()) {
                    i = 4;
                }
            }
            context.drawTexture(JOB_BUTTON_TEXTURES, this.getX(), this.getY(), 0, i * 38, this.width, this.height);
            if (this.isHovered()) {
                context.drawTooltip(minecraftClient.textRenderer, this.tooltip, mouseX, mouseY);
            }
        }

        public void setEmployedButton(boolean employed) {
            this.employed = employed;
        }

        public boolean isEmployedButton() {
            return this.employed;
        }

        private int getTextureY() {
            int i = 1;
            if (!this.active) {
                i = 0;
            } else if (this.isSelected()) {
                i = 2;
            }
            return i;
        }

    }

}
