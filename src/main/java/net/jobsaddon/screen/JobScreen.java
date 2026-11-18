package net.jobsaddon.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jobsaddon.JobsAddonMain;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.KeyInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.packet.EmployPacket;
import net.libz.api.Tab;
import net.libz.util.DrawTabHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class JobScreen extends Screen implements Tab {

    private static final ResourceLocation BACKGROUND_TEXTURE = JobsAddonMain.identifierOf("textures/gui/jobs_background.png");
    private static final ResourceLocation JOB_BUTTON_TEXTURES = JobsAddonMain.identifierOf("textures/gui/jobs_buttons.png");

    private final int backgroundWidth = 200;
    private final int backgroundHeight = 215;
    private int x;
    private int y;

    private JobsManager jobsManager;

    private final WidgetButtonPage[] jobButtons = new WidgetButtonPage[8];
    private int jobRow = 0;

    public JobScreen() {
        super(Component.translatable("screen.jobsaddon.jobs_screen"));
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.jobsManager = ((JobsManagerAccess) this.minecraft.player).getJobsManager();

        for (int i = 0; i < 8; i++) {
            final int buttonIndex = i;
            this.jobButtons[i] = this.addRenderableWidget(new WidgetButtonPage(this.x + (i % 2 == 0 ? 8 : 96), this.y + 55 + i / 2 * 38, button -> {
                List<Integer> jobIds = getJobIds();
                int index = buttonIndex + this.jobRow * 2;
                if (button.active && index < jobIds.size()) {
                    int jobId = jobIds.get(index);
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
    public void tick() {
        super.tick();
        updateJobButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.jobsManager.getPlayerJobs().size() > 8 && isPointWithinBounds(8, 55, 186, 154, mouseX, mouseY)) {
            int maxJobRow = getMaxJobRow();
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // render title
        if (this.minecraft.player != null) {
            Component title = Component.translatable("text.jobsaddon.gui.title", this.minecraft.player.getName().getString());
            context.drawString(this.font, title, this.x + this.backgroundWidth / 2 - this.minecraft.font.width(title) / 2, this.y + 7, 0x3F3F3F, false);
        }
        // render time label
        Component timeText = null;
        Component employedText = null;
        if (jobsManager != null) {
            if (jobsManager.getEmployedJobTime() > 0) {
                int seconds = jobsManager.getEmployedJobTime() / 20;
                String string;
                if (seconds >= 3600) {
                    string = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
                } else {
                    string = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                }
                timeText = Component.nullToEmpty(Component.translatable("text.jobsaddon.employedTime").getString() + string);
            } else {
                timeText = Component.nullToEmpty(Component.translatable("text.jobsaddon.employedTime").getString() + "00:00");
            }

            List<Integer> employedJobs = jobsManager.getEmployedJobsList().stream().filter(jobId -> jobsManager.getJob(jobId) != null).toList();
            if (employedJobs.size() == 1) {
                employedText = Component.translatable("text.jobsaddon.employedJob", getJobTitle(employedJobs.get(0)));
            } else if (employedJobs.size() == 2) {
                employedText = Component.translatable("text.jobsaddon.employedJobs", getJobTitle(employedJobs.get(0)), getJobTitle(employedJobs.get(1)));
            } else if (employedJobs.size() > 2) {
                MutableComponent jobList = Component.empty();
                for (int i = 0; i < employedJobs.size(); i++) {
                    if (i > 0) {
                        jobList.append(", ");
                    }
                    jobList.append(getJobTitle(employedJobs.get(i)));
                }
                employedText = Component.translatable("text.jobsaddon.employedJobList", jobList);
            } else {
                employedText = Component.translatable("text.jobsaddon.notEmployed");
            }
        }
        if (timeText != null) {
            context.drawString(this.font, timeText, this.x + 12, this.y + 20, 0x3F3F3F, false);
        }
        if (employedText != null) {
            context.drawString(this.font, Language.getInstance().getVisualOrder(this.font.substrByWidth(employedText, this.backgroundWidth - 24)), this.x + 12, this.y + 33, 0x3F3F3F, false);
        }

        List<Integer> jobIds = getJobIds();
        for (int i = 0; i < 8; i++) {
            int index = i + this.jobRow * 2;
            if (jobIds.size() <= index) {
                break;
            }
            int jobId = jobIds.get(index);
            int xPos = this.x + i % 2 * 88 + 8;
            int yPos = this.y + i / 2 * 38 + 55;

            // render job title
            context.drawString(this.font, this.getJobTitle(jobId), xPos + 22, yPos + 4, 0xFFFFFF, false);
            // render job levels
            Component jobLevelText = Component.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel(jobId), jobsManager.getPlayerJobs().get(jobId).getMaxLevel());
            context.drawString(this.font, jobLevelText, xPos + 48 - this.font.width(jobLevelText) / 2, yPos + 19, 0xFFFFFF,
                    false);
            // render icons
            context.blit(JobsAddonMain.identifierOf("textures/gui/" + this.jobsManager.getPlayerJobs().get(jobId).getKey() + ".png"), xPos + 5, yPos + 5, 0, 0, 14, 14, 14, 14);
            if (this.isPointWithinBounds(xPos + 5 - this.x, yPos + 5 - this.y, 14, 14, mouseX, mouseY)) {
                context.renderComponentTooltip(this.font, this.getJobTooltip(jobId), mouseX, mouseY);
            }
            // render xp bar
            context.blit(JOB_BUTTON_TEXTURES, xPos + 4, yPos + 29, 88, 0, 80, 5);
            if (jobsManager.getNextJobLevelExperience(jobId) > 0 && jobsManager.getJobXP(jobId) > 0) {
                context.blit(JOB_BUTTON_TEXTURES, xPos + 4, yPos + 29, 88, 5, Math.min(79, 79 * jobsManager.getJobXP(jobId) / jobsManager.getNextJobLevelExperience(jobId)), 5);
                if (this.isPointWithinBounds(xPos + 4 - this.x, yPos + 29 - this.y, 80, 5, mouseX, mouseY)) {
                    context.renderTooltip(font, Component.translatable("text.jobsaddon.jobLevelExperience", jobsManager.getJobXP(jobId), jobsManager.getNextJobLevelExperience(jobId)), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderTransparentBackground(context);
        context.blit(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        if (this.jobsManager.getPlayerJobs().size() > 8) {
            int scrollLevels = getMaxJobRow();
            int sliderY = this.jobRow * 114 / scrollLevels;
            context.blit(BACKGROUND_TEXTURE, this.x + 186, this.y + 55 + sliderY, 200, 0, 6, 38);
        } else {
            context.blit(BACKGROUND_TEXTURE, this.x + 186, this.y + 55, 206, 0, 6, 38);
        }
        DrawTabHelper.drawTab(minecraft, context, this, x, y, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int ch, int keyCode, int modifiers) {
        if (KeyInit.screenKey.matches(ch, keyCode) || net.levelz.init.KeyInit.screenKey.matches(ch, keyCode) || Objects.requireNonNull(minecraft).options.keyInventory.matches(ch, keyCode)) {
            this.onClose();
            return true;
        } else {
            return super.keyPressed(ch, keyCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DrawTabHelper.onTabButtonClick(minecraft, this, this.x, this.y, mouseX, mouseY, false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateJobButtons() {
        this.jobRow = Math.min(this.jobRow, getMaxJobRow());
        List<Integer> jobIds = getJobIds();
        for (int i = 0; i < this.jobButtons.length; i++) {
            int index = i + this.jobRow * 2;
            if (jobIds.size() <= index) {
                this.jobButtons[i].visible = false;
                continue;
            }
            this.jobButtons[i].visible = true;
            int jobId = jobIds.get(index);
            // employed jobs can always be quit
            this.jobButtons[i].active = this.jobsManager.isEmployedJob(jobId) || this.jobsManager.canEmployJob(jobId);
            this.jobButtons[i].setEmployedButton(this.jobsManager.isEmployedJob(jobId));
        }
    }

    private int getMaxJobRow() {
        int jobCount = this.jobsManager.getPlayerJobs().size();
        if (jobCount <= 8) {
            return 0;
        }
        return (jobCount - 8 + 1) / 2;
    }

    private List<Integer> getJobIds() {
        List<Integer> jobIds = new ArrayList<>(this.jobsManager.getPlayerJobs().keySet());
        Collections.sort(jobIds);
        return jobIds;
    }

    private boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY) {
        int i = this.x;
        int j = this.y;
        return (pointX -= (double) i) >= (double) (x - 1) && pointX < (double) (x + width + 1) && (pointY -= (double) j) >= (double) (y - 1) && pointY < (double) (y + height + 1);
    }

    private Component getJobTitle(int jobId) {
        return this.jobsManager.getPlayerJobs().get(jobId).getJobName();
    }

    private List<Component> getJobTooltip(int jobId) {
        List<Component> tooltipList = new ArrayList<>();
        String[] tooltips = this.jobsManager.getPlayerJobs().get(jobId).getJobTooltip().getString().split("\n");
        for (String tooltip : tooltips) {
            tooltipList.add(Component.nullToEmpty(tooltip));
        }
        return tooltipList;
    }

    private static class WidgetButtonPage extends Button {

        private boolean employed;

        public WidgetButtonPage(int x, int y, Button.OnPress onPress) {
            super(x, y, 88, 38, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
            context.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int i = this.getTextureY();

            context.blit(JOB_BUTTON_TEXTURES, this.getX(), this.getY(), 0, i * 38, this.width, this.height);
            context.setColor(1.0f, 1.0f, 1.0f, 1.0f);
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
            } else if (this.isHoveredOrFocused()) {
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
