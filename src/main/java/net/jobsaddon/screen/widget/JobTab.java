package net.jobsaddon.screen.widget;

// import net.jobsaddon.gui.JobsGui;
// import net.jobsaddon.gui.JobsScreen;
import net.jobsaddon.screen.JobScreen;
import net.libz.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class JobTab extends InventoryTab {

    public JobTab(Text title, Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(MinecraftClient client) {
        client.setScreen(new JobScreen());
        // client.setScreen(new JobsScreen(new JobsGui(client)));
    }

}
