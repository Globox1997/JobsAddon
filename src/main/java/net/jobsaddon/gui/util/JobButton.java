package net.jobsaddon.gui.util;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class JobButton extends WButton {

    private static final Identifier BUTTON_ICON = new Identifier("jobsaddon:textures/gui/job_button.png");
    private static final Identifier BUTTON_HOVERED_ICON = new Identifier("jobsaddon:textures/gui/hovered_job_button.png");
    private static final Identifier BUTTON_DISABLED_ICON = new Identifier("jobsaddon:textures/gui/disabled_job_button.png");
    private static final Identifier BUTTON_EMPLOYED_ICON = new Identifier("jobsaddon:textures/gui/employed_job_button.png");
    private static final Identifier BUTTON_QUIT_ICON = new Identifier("jobsaddon:textures/gui/quit_job_button.png");

    private boolean employed = false;

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        Identifier identifier;
        if (this.isEnabled()) {
            boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
            if (this.employed)
                identifier = BUTTON_EMPLOYED_ICON;
            else
                identifier = BUTTON_ICON;
            if (hovered)
                if (employed)
                    identifier = BUTTON_QUIT_ICON;
                else
                    identifier = BUTTON_HOVERED_ICON;
        } else
            identifier = BUTTON_DISABLED_ICON;

        ScreenDrawing.texturedRect(matrices, x, y, width, height, identifier, 0xFFFFFFFF, 1f);
    }

    @Override
    public void setSize(int x, int y) {
        this.width = 91;
        this.height = 38;
    }

    public void setEmployedButton(boolean employed) {
        this.employed = employed;
    }

    public boolean isEmployedButton() {
        return this.employed;
    }

}
