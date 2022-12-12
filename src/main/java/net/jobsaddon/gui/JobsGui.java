package net.jobsaddon.gui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.gui.util.JobButton;
import net.jobsaddon.gui.util.JobSprite;
import net.jobsaddon.init.ConfigInit;
import net.jobsaddon.init.RenderInit;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsClientPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class JobsGui extends LightweightGuiDescription {

    private boolean hadEmployedTimer = false;
    // Can store in list
    // private final List<String> jobNames = new ArrayList<String>(List.of("brewer", "builder", "farmer", "fisher", "lumberjack", "miner", "smither", "warrior"));
    // private List<JobButton> jobButtons = new ArrayList<JobButton>(
    // List.of(new JobButton(), new JobButton(), new JobButton(), new JobButton(), new JobButton(), new JobButton(), new JobButton(), new JobButton()));

    public JobsGui(MinecraftClient client) {
        PlayerEntity playerEntity = client.player;

        WPlainPanel root = new WPlainPanel();
        setRootPanel(root);
        root.setSize(200, 215);

        // Top label
        MutableText title = Text.translatable("text.jobsaddon.gui.title", playerEntity.getName().getString());
        WLabel topLlabel = new WLabel(title);

        root.add(topLlabel, 20 + client.textRenderer.getWidth(title) / 2, 7);

        JobsManager jobsManager = ((JobsManagerAccess) playerEntity).getJobsManager();

        // Job buttons
        JobButton brewerButton = new JobButton();
        JobButton builderButton = new JobButton();
        JobButton farmerButton = new JobButton();
        JobButton fisherButton = new JobButton();
        JobButton lumberjackButton = new JobButton();
        JobButton minerButton = new JobButton();
        JobButton smitherButton = new JobButton();
        JobButton warriorButton = new JobButton();

        root.add(brewerButton, 102, 170);
        root.add(builderButton, 7, 129);
        root.add(farmerButton, 7, 88);
        root.add(fisherButton, 7, 170);
        root.add(lumberjackButton, 7, 47);
        root.add(minerButton, 102, 47);
        root.add(smitherButton, 102, 129);
        root.add(warriorButton, 102, 88);

        // Job sprites
        JobSprite brewerIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.21875F, 0.1875F, 0.2734375F, 0.2421875F);
        JobSprite builderIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.3828125F, 0.1875F, 0.4375F, 0.2421875F);
        JobSprite farmerIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.0546875F, 0.1875F, 0.109375F, 0.2421875F);
        JobSprite fisherIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.1640625F, 0.1875F, 0.21875F, 0.2421875F);
        JobSprite lumberjackIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0F, 0.1875F, 0.0546875F, 0.2421875F);
        JobSprite minerIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.109375F, 0.1875F, 0.1640625F, 0.2421875F);
        JobSprite smitherIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.2734375F, 0.1875F, 0.328125F, 0.2421875F);
        JobSprite warriorIcon = new JobSprite(RenderInit.JOB_GUI_ICONS, 0.328125F, 0.1875F, 0.3828125F, 0.2421875F);

        brewerIcon.addText(jobTooltip("brewer"));
        builderIcon.addText(jobTooltip("builder"));
        farmerIcon.addText(jobTooltip("farmer"));
        fisherIcon.addText(jobTooltip("fisher"));
        lumberjackIcon.addText(jobTooltip("lumberjack"));
        minerIcon.addText(jobTooltip("miner"));
        smitherIcon.addText(jobTooltip("smither"));
        warriorIcon.addText(jobTooltip("warrior"));

        root.add(lumberjackIcon, 12, 52, 14, 14);
        root.add(minerIcon, 107, 52, 14, 14);
        root.add(farmerIcon, 12, 93, 14, 14);
        root.add(warriorIcon, 107, 93, 14, 14);
        root.add(builderIcon, 12, 134, 14, 14);
        root.add(smitherIcon, 107, 134, 14, 14);
        root.add(fisherIcon, 12, 175, 14, 14);
        root.add(brewerIcon, 107, 175, 14, 14);

        // Job Title
        WLabel brewerTitle = new WLabel(jobTitle("brewer"), 0xFFFFFF);
        WLabel builderTitle = new WLabel(jobTitle("builder"), 0xFFFFFF);
        WLabel farmerTitle = new WLabel(jobTitle("farmer"), 0xFFFFFF);
        WLabel fisherTitle = new WLabel(jobTitle("fisher"), 0xFFFFFF);
        WLabel lumberjackTitle = new WLabel(jobTitle("lumberjack"), 0xFFFFFF);
        WLabel minerTitle = new WLabel(jobTitle("miner"), 0xFFFFFF);
        WLabel smitherTitle = new WLabel(jobTitle("smither"), 0xFFFFFF);
        WLabel warriorTitle = new WLabel(jobTitle("warrior"), 0xFFFFFF);

        root.add(lumberjackTitle, 30, 51);
        root.add(minerTitle, 125, 51);
        root.add(farmerTitle, 30, 92);
        root.add(warriorTitle, 125, 92);
        root.add(builderTitle, 30, 133);
        root.add(smitherTitle, 125, 133);
        root.add(fisherTitle, 30, 174);
        root.add(brewerTitle, 125, 174);

        // Job Level
        WDynamicLabel brewerLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("brewer"), ConfigInit.CONFIG.jobMaxLevel).getString(), 0xFFFFFF);
        WDynamicLabel builderLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("builder"), ConfigInit.CONFIG.jobMaxLevel).getString(),
                0xFFFFFF);
        WDynamicLabel farmerLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("farmer"), ConfigInit.CONFIG.jobMaxLevel).getString(), 0xFFFFFF);
        WDynamicLabel fisherLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("fisher"), ConfigInit.CONFIG.jobMaxLevel).getString(), 0xFFFFFF);
        WDynamicLabel lumberjackLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("lumberjack"), ConfigInit.CONFIG.jobMaxLevel).getString(),
                0xFFFFFF);
        WDynamicLabel minerLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("miner"), ConfigInit.CONFIG.jobMaxLevel).getString(), 0xFFFFFF);
        WDynamicLabel smitherLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("smither"), ConfigInit.CONFIG.jobMaxLevel).getString(),
                0xFFFFFF);
        WDynamicLabel warriorLabel = new WDynamicLabel(() -> "" + Text.translatable("text.jobsaddon.jobLevel", jobsManager.getJobLevel("warrior"), ConfigInit.CONFIG.jobMaxLevel).getString(),
                0xFFFFFF);

        root.add(lumberjackLabel, 42, 66);
        root.add(minerLabel, 137, 66);
        root.add(farmerLabel, 42, 107);
        root.add(warriorLabel, 137, 107);
        root.add(builderLabel, 42, 148);
        root.add(smitherLabel, 137, 148);
        root.add(fisherLabel, 42, 189);
        root.add(brewerLabel, 137, 189);

        WDynamicLabel employedTimeLabel = new WDynamicLabel(() -> {

            if (jobsManager.getEmployedJobTime() > 0) {
                int seconds = jobsManager.getEmployedJobTime() / 20;
                String string;
                if (seconds >= 3600)
                    string = String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
                else
                    string = String.format("%02d:%02d", (seconds % 3600) / 60, (seconds % 60));
                if (!hadEmployedTimer)
                    hadEmployedTimer = true;
                return Text.translatable("text.jobsaddon.employedTime").getString() + string;
            } else {
                if (hadEmployedTimer) {
                    setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
                    hadEmployedTimer = false;
                }
                return Text.translatable("text.jobsaddon.employedTime").getString() + "00:00";
            }
        });

        root.add(employedTimeLabel, 12, 20);

        WDynamicLabel employedJobsLavel = new WDynamicLabel(() -> {

            if (jobsManager.getEmployedJobs() != null && jobsManager.getEmployedJobs().size() > 0) {
                if (jobsManager.getEmployedJobs().size() > 2)
                    return Text.translatable("text.jobsaddon.tooManyEmployedJobs", jobTitle(jobsManager.getEmployedJobs().get(0)), jobTitle(jobsManager.getEmployedJobs().get(1))).getString();
                else if (jobsManager.getEmployedJobs().size() == 1)
                    return Text.translatable("text.jobsaddon.employedJob", jobTitle(jobsManager.getEmployedJobs().get(0))).getString();
                else
                    return Text.translatable("text.jobsaddon.employedJobs", jobTitle(jobsManager.getEmployedJobs().get(0)), jobTitle(jobsManager.getEmployedJobs().get(1))).getString();
            } else
                return Text.translatable("text.jobsaddon.notEmployed").getString();
        });

        root.add(employedJobsLavel, 12, 33);

        // Button mechanic
        setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);

        brewerButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("brewer")) {
                brewerButton.setEmployedButton(false);
                jobsManager.quitJob("brewer");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "brewer", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("brewer")) {
                brewerButton.setEmployedButton(true);
                jobsManager.employJob("brewer");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "brewer", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        builderButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("builder")) {
                builderButton.setEmployedButton(false);
                jobsManager.quitJob("builder");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "builder", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("builder")) {
                builderButton.setEmployedButton(true);
                jobsManager.employJob("builder");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "builder", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        farmerButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("farmer")) {
                farmerButton.setEmployedButton(false);
                jobsManager.quitJob("farmer");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "farmer", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("farmer")) {
                farmerButton.setEmployedButton(true);
                jobsManager.employJob("farmer");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "farmer", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        fisherButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("fisher")) {
                fisherButton.setEmployedButton(false);
                jobsManager.quitJob("fisher");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "fisher", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("fisher")) {
                fisherButton.setEmployedButton(true);
                jobsManager.employJob("fisher");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "fisher", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        lumberjackButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("lumberjack")) {
                lumberjackButton.setEmployedButton(false);
                jobsManager.quitJob("lumberjack");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "lumberjack", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("lumberjack")) {
                lumberjackButton.setEmployedButton(true);
                jobsManager.employJob("lumberjack");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "lumberjack", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        minerButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("miner")) {
                minerButton.setEmployedButton(false);
                jobsManager.quitJob("miner");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "miner", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("miner")) {
                minerButton.setEmployedButton(true);
                jobsManager.employJob("miner");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "miner", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        smitherButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("smither")) {
                smitherButton.setEmployedButton(false);
                jobsManager.quitJob("smither");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "smither", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("smither")) {
                smitherButton.setEmployedButton(true);
                jobsManager.employJob("smither");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "smither", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });
        warriorButton.setOnClick(() -> {
            if (jobsManager.isEmployedJob("warrior")) {
                warriorButton.setEmployedButton(false);
                jobsManager.quitJob("warrior");
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "warrior", false, jobsManager.getEmployedJobTime());
            } else if (jobsManager.canEmployJob("warrior")) {
                warriorButton.setEmployedButton(true);
                jobsManager.employJob("warrior");
                jobsManager.setEmployedJobTime(jobsManager.hasMaxEmployedJobs() ? ConfigInit.CONFIG.jobChangeTime : 0);
                JobsClientPacket.writeC2SSelectJobPacket(jobsManager, "warrior", true, jobsManager.getEmployedJobTime());
            }
            setButtonEnabled(brewerButton, builderButton, farmerButton, fisherButton, lumberjackButton, minerButton, smitherButton, warriorButton, jobsManager);
        });

        root.validate(this);
    }

    private void setButtonEnabled(JobButton wButton1, JobButton wButton2, JobButton wButton3, JobButton wButton4, JobButton wButton5, JobButton wButton6, JobButton wButton7, JobButton wButton8,
            JobsManager jobsManager) {
        wButton1.setEmployedButton(jobsManager.isEmployedJob("brewer"));
        wButton2.setEmployedButton(jobsManager.isEmployedJob("builder"));
        wButton3.setEmployedButton(jobsManager.isEmployedJob("farmer"));
        wButton4.setEmployedButton(jobsManager.isEmployedJob("fisher"));
        wButton5.setEmployedButton(jobsManager.isEmployedJob("lumberjack"));
        wButton6.setEmployedButton(jobsManager.isEmployedJob("miner"));
        wButton7.setEmployedButton(jobsManager.isEmployedJob("smither"));
        wButton8.setEmployedButton(jobsManager.isEmployedJob("warrior"));

        wButton1.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton1.isEmployedButton());
        wButton2.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton2.isEmployedButton());
        wButton3.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton3.isEmployedButton());
        wButton4.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton4.isEmployedButton());
        wButton5.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton5.isEmployedButton());
        wButton6.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton6.isEmployedButton());
        wButton7.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton7.isEmployedButton());
        wButton8.setEnabled((!jobsManager.hasMaxEmployedJobs() && jobsManager.getEmployedJobTime() == 0) || wButton8.isEmployedButton());
    }

    private static MutableText jobTitle(String string) {
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

    private static String[] jobTooltip(String string) {
        String tooltip = switch (string) {
            case "brewer" -> Text.translatable("text.jobsaddon.brewer.tooltop").getString();
            case "builder" -> Text.translatable("text.jobsaddon.builder.tooltop").getString();
            case "farmer" -> Text.translatable("text.jobsaddon.farmer.tooltop").getString();
            case "fisher" -> Text.translatable("text.jobsaddon.fisher.tooltop").getString();
            case "lumberjack" -> Text.translatable("text.jobsaddon.lumberjack.tooltop").getString();
            case "miner" -> Text.translatable("text.jobsaddon.miner.tooltop").getString();
            case "smither" -> Text.translatable("text.jobsaddon.smither.tooltop").getString();
            case "warrior" -> Text.translatable("text.jobsaddon.warrior.tooltop").getString();
            default -> Text.translatable("text.jobsaddon.nonJobTitle.tooltop").getString();
        };
        return tooltip.split("\n");
    }

}