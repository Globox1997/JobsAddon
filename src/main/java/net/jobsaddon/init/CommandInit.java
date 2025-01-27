package net.jobsaddon.init;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.jobsaddon.jobs.Job;
import net.minecraft.command.CommandSource;
import org.apache.commons.lang3.StringUtils;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class CommandInit {

    private static final SuggestionProvider<ServerCommandSource> JOBS_SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(
            JobsManager.JOBS.values().stream().map(Job::getKey), builder);

    public static void init() {

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((CommandManager.literal("job").requires((serverCommandSource) -> {
                return serverCommandSource.hasPermissionLevel(2);
            })).then(CommandManager.argument("targets", EntityArgumentType.players())
                    // Add values
                    .then(CommandManager.literal("add").then(CommandManager.literal("level").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "level",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.argument("jobKey", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER).then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), StringArgumentType.getString(commandContext, "jobKey"),
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))))
                    // Remove values
                    .then(CommandManager.literal("remove").then(CommandManager.literal("level").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "level",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.argument("jobKey", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER).then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), StringArgumentType.getString(commandContext, "jobKey"),
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))))
                    // Set values
                    .then(CommandManager.literal("set").then(CommandManager.literal("level").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "level",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.argument("jobKey", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER).then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), StringArgumentType.getString(commandContext, "jobKey"),
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))))
                    // Print values
                    .then(CommandManager.literal("get").then(CommandManager.literal("level").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "level", 0, 3);
                    })).then(CommandManager.literal("all").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "all", 0, 3);
                    })).then(CommandManager.argument("jobKey", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), StringArgumentType.getString(commandContext, "jobKey"), 0, 3);
                    })))));
        });
    }

    // Reference 0:Add, 1:Remove, 2:Set, 3:Print
    private static int executeJobCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String jobKey, int i, int reference) {
        Iterator<ServerPlayerEntity> player = targets.iterator();

        i = MathHelper.abs(i);
        // loop over players
        while (player.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = player.next();
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();

            int playerJobLevel = 0;
            int jobId = -1;
            for (Job overallJob : JobsManager.JOBS.values()) {
                if (overallJob.getKey().equals(jobKey)) {
                    playerJobLevel = jobsManager.getJobLevel(overallJob.getId());
                    jobId = overallJob.getId();
                    break;
                }
            }

            if (reference == 0) {
                playerJobLevel += i;
            } else if (reference == 1) {
                playerJobLevel = Math.max(playerJobLevel - i, 0);
            } else if (reference == 2) {
                playerJobLevel = i;
            } else if (reference == 3) {
                if (jobKey.equals("all")) {
                    for (Job overallJob: JobsManager.JOBS.values()) {
                        final String finalJobName = overallJob.getKey();
                        source.sendFeedback(() -> Text.translatable("commands.jobmanager.printLevel", serverPlayerEntity.getDisplayName(), StringUtils.capitalize(finalJobName) + " Level:",
                                jobsManager.getJobLevel(overallJob.getId())), true);
                    }
                } else {
                    final int finalPlayerJobLevel = playerJobLevel;
                    source.sendFeedback(
                            () -> Text.translatable("commands.jobmanager.printLevel", serverPlayerEntity.getDisplayName(), StringUtils.capitalize(jobKey) + " Level:", finalPlayerJobLevel), true);
                }
                continue;
            }
            jobsManager.setJobLevel(jobId, playerJobLevel);

            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);

            if (reference != 3) {
                source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
            }
        }

        return targets.size();
    }

}
