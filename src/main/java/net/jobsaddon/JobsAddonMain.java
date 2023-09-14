package net.jobsaddon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.jobsaddon.init.CommandInit;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.CriteriaInit;
import net.jobsaddon.init.EventInit;
import net.jobsaddon.init.JsonReaderInit;
import net.jobsaddon.init.TagInit;
import net.jobsaddon.network.JobsServerPacket;

public class JobsAddonMain implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("JobsManager");

    @Override
    public void onInitialize() {
        JsonReaderInit.init();
        CommandInit.init();
        ConfigInit.init();
        CriteriaInit.init();
        JobsServerPacket.init();
        TagInit.init();
        EventInit.init();
    }

}
