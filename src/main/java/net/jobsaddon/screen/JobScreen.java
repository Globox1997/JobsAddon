package net.jobsaddon.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.EmployPacket;
import net.levelz.init.KeyInit;
import net.libz.api.Tab;
import net.libz.util.DrawTabHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JobScreen extends Screen implements Tab {

    private static final Identifier BACKGROUND_TEXTURE = JobsAddonMain.identifierOf("textures/gui/jobs_background.png");
    private static final Identifier JOB_BUTTON_TEXTURES = JobsAddonMain.identifierOf("textures/gui/jobs_buttons.png");

    private final int backgroundWidth = 200;
    private final int backgroundHeight = 215;
    private int x;
    private int y;

    private JobsManager jobsManager;

    private final WidgetButtonPage[] jobButtons = new WidgetButtonPage[8];
    private int jobRow = 0;

    private boolean hadEmployedTimer = false;

    public JobScreen() {
        super(Text.translatable("screen.jobsaddon.jobs_screen"));
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.jobsManager = ((JobsManagerAccess) this.client.player).getJobsManager();

        for (int i = 0; i < 8; i++) {
            if (this.jobsManager.getPlayerJobs().size() <= i) {
                break;
            }
            final int jobIds = i;
            this.jobButtons[i] = this.addDrawableChild(new WidgetButtonPage(this.x + (i % 2 == 0 ? 8 : 96), this.y + 55 + i / 2 * 38, button -> {
                int jobId = jobIds + this.jobRow * 2;
                if (button.active) {
                    boolean isEmployed = ((WidgetButtonPage) button).isEmployedButton();
                    if (isEmployed) {
                        this.jobsManager.quitJob(jobId);
                    } else {
                        this.jobsManager.employJob(jobId);
                        this.jobsManager.setEmployedJobTime(ConfigInit.CONFIG.jobChangeTime);
                    }
                    ClientPlayNetworking.send(new EmployPacket(jobId, !isEmployed));
                    ((WidgetButtonPage) button).setEmployedButton(!isEmployed);
                    updateJobButtons();
                }
            }));
        }
        updateJobButtons();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.jobsManager.getPlayerJobs().size() > 8 && isPointWithinBounds(8, 55, 186, 154, mouseX, mouseY)) {

            int maxJobRow = (this.jobsManager.getPlayerJobs().size() - 8) / 2;
            if (this.jobsManager.getPlayerJobs().size() % 2 != 0) {
                maxJobRow += 1;
            }
            int oldJobRow = this.jobRow;
            int newJobRow = this.jobRow;
            newJobRow = newJobRow - (int) (verticalAmount);
            if (newJobRow < 0) {
                this.jobRow = 0;
            } else {
                this.jobRow = Math.min(newJobRow, maxJobRow);
            }
            if (oldJobRow != this.jobRow) {
                updateJobButtons();
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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
                    updateJobButtons();
                }
                timeText = Text.of(Text.translatable("text.jobsaddon.employedTime").getString() + "00:00");
            }

            if (!jobsManager.getEmployedJobsList().isEmpty()) {
                if (jobsManager.getEmployedJobsList().size() > ConfigInit.CONFIG.employedJobs) {
                    employedText = Text.translatable("text.jobsaddon.tooManyEmployedJobs", getJobTitle(jobsManager.getEmployedJobsList().get(0)), getJobTitle(jobsManager.getEmployedJobsList().get(1)));
                } else if (jobsManager.getEmployedJobsList().size() == 1) {
                    employedText = Text.translatable("text.jobsaddon.employedJob", getJobTitle(jobsManager.getEmployedJobsList().get(0)));
                } else {
                    employedText = Text.translatable("text.jobsaddon.employedJobs", getJobTitle(jobsManager.getEmployedJobsList().get(0)), getJobTitle(jobsManager.getEmployedJobsList().get(1)));
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

        for (int i = 0; i < 8; i++) {
            int jobId = i + this.jobRow * 2;

            if (this.jobsManager.getPlayerJobs().size() <= jobId) {
                break;
            }
            int xPos = this.x + i % 2 * 88 + 8;
            int yPos = this.y + i / 2 * 38 + 55;

            // render job title
            context.drawText(this.textRenderer, this.getJobTitle(jobId), xPos + 22, yPos + 4, 0xFFFFFF, false);
            // render job levels
            Text jobLevelText = Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel(jobId), jobsManager.getPlayerJobs().get(jobId).getMaxLevel());
            context.drawText(this.textRenderer, jobLevelText, xPos + 48 - this.textRenderer.getWidth(jobLevelText) / 2, yPos + 19, 0xFFFFFF,
                    false);
            // render icons
            context.drawTexture(JobsAddonMain.identifierOf("textures/gui/" + this.jobsManager.getPlayerJobs().get(jobId).getKey() + ".png"), xPos + 5, yPos + 5, 0, 0, 14, 14, 14, 14);
            if (this.isPointWithinBounds(xPos + 5 - this.x, yPos + 5 - this.y, 14, 14, mouseX, mouseY)) {
                context.drawTooltip(this.textRenderer, this.getJobTooltip(jobId), mouseX, mouseY);
            }
            // render xp bar
            context.drawTexture(JOB_BUTTON_TEXTURES, xPos + 4, yPos + 29, 88, 0, 80, 5);
            if (jobsManager.getNextJobLevelExperience(jobId) > 0 && jobsManager.getJobXP(jobId) > 0) {
                context.drawTexture(JOB_BUTTON_TEXTURES, xPos + 4, yPos + 29, 88, 5, 79 * jobsManager.getJobXP(jobId) / jobsManager.getNextJobLevelExperience(jobId), 5);
                if (this.isPointWithinBounds(xPos + 4 - this.x, yPos + 29 - this.y, 80, 5, mouseX, mouseY)) {
                    context.drawTooltip(textRenderer, Text.translatable("text.jobsaddon.jobLevelExperience", jobsManager.getJobXP(jobId), jobsManager.getNextJobLevelExperience(jobId)), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderInGameBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        if (this.jobsManager.getPlayerJobs().size() > 8) {
            int scrollLevels = (this.jobsManager.getPlayerJobs().size() - 8) / 2;
            if (this.jobsManager.getPlayerJobs().size() % 2 != 0) {
                scrollLevels += 1;
            }

            int sliderY = this.jobRow * 114 / scrollLevels;
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 55 + sliderY, 200, 0, 6, 38);
        } else {
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 55, 206, 0, 6, 38);
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

    private void updateJobButtons() {
        for (int i = 0; i < this.jobButtons.length; i++) {
            if (this.jobsManager.getPlayerJobs().size() <= i) {
                break;
            }
            int jobId = i + this.jobRow * 2;
            if (this.jobsManager.getPlayerJobs().size() <= jobId) {
                this.jobButtons[i].visible = false;
                return;
            } else {
                this.jobButtons[i].visible = true;
            }

            if (this.jobsManager.getPlayerJobs().get(jobId).getMaxLevel() <= this.jobsManager.getPlayerJobs().get(jobId).getLevel()) {
                this.jobButtons[i].active = false;
            } else if (this.jobsManager.getEmployedJobTime() > 0) {
                this.jobButtons[i].active = false;
            } else {
                this.jobButtons[i].active = this.jobsManager.canEmployJob(jobId) || this.jobsManager.isEmployedJob(jobId);
            }
            this.jobButtons[i].setEmployedButton(this.jobsManager.isEmployedJob(jobId));
        }
    }

    private boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        int i = this.x;
        int j = this.y;
        return (pointX -= (double) i) >= (double) (x - 1) && pointX < (double) (x + width + 1) && (pointY -= (double) j) >= (double) (y - 1) && pointY < (double) (y + height + 1);
    }

    private Text getJobTitle(int jobId) {
        return this.jobsManager.getPlayerJobs().get(jobId).getJobName();
    }

    private List<Text> getJobTooltip(int jobId) {
        List<Text> tooltipList = new ArrayList<>();
        String[] tooltips = this.jobsManager.getPlayerJobs().get(jobId).getJobTooltip().getString().split("\n");
        for (String tooltip : tooltips) {
            tooltipList.add(Text.of(tooltip));
        }
        return tooltipList;
    }

    private static class WidgetButtonPage extends ButtonWidget {

        private boolean employed;

        public WidgetButtonPage(int x, int y, ButtonWidget.PressAction onPress) {
            super(x, y, 88, 38, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int i = this.getTextureY();

            context.drawTexture(JOB_BUTTON_TEXTURES, this.getX(), this.getY(), 0, i * 38, this.width, this.height);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
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
            if (isEmployedButton()) {
                i = 3;
                if (this.isHovered()) {
                    i = 4;
                }
            }
            return i;
        }

    }

}
