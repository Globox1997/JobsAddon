package net.jobsaddon;

import net.fabricmc.api.ModInitializer;
import net.jobsaddon.init.*;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobsAddonMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("JobsManager");

    @Override
    public void onInitialize() {
        LoaderInit.init();
        CommandInit.init();
        ConfigInit.init();
        CriteriaInit.init();
        JobsServerPacket.init();
        TagInit.init();
        EventInit.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.of("jobsaddon", name);
    }
}
