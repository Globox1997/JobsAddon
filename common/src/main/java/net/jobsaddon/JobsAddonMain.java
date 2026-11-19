package net.jobsaddon;

import net.jobsaddon.init.ConfigInit;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JobsAddonMain {

    public static final String MOD_ID = "jobsaddon";
    public static final Logger LOGGER = LogManager.getLogger("JobsManager");

    public static void init() {
        ConfigInit.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
