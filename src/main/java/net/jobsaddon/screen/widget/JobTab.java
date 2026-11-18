package net.jobsaddon.screen.widget;

import net.jobsaddon.screen.JobScreen;
import net.libz.api.InventoryTab;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class JobTab extends InventoryTab {

    public JobTab(Component title, ResourceLocation texture, int preferedPos, Class<?>... screenClasses) {
        super(title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(Minecraft client) {
        client.setScreen(new JobScreen());
    }

}
