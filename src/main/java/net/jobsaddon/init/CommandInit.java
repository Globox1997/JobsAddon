package net.jobsaddon.init;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;

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

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((CommandManager.literal("jobmanager").requires((serverCommandSource) -> {
                return serverCommandSource.hasPermissionLevel(3);
            })).then(CommandManager.argument("targets", EntityArgumentType.players())
                    // Add values
                    .then(CommandManager.literal("add").then(CommandManager.literal("brewer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "brewer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("builder").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "builder",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("farmer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "farmer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("fisher").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "fisher",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("lumberjack").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "lumberjack",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("miner").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "miner",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("smither").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "smither",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))).then(CommandManager.literal("warrior").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "warrior",
                                IntegerArgumentType.getInteger(commandContext, "level"), 0);
                    }))))
                    // Remove values
                    .then(CommandManager.literal("remove").then(CommandManager.literal("brewer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "brewer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("builder").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "builder",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("farmer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "farmer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("fisher").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "fisher",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("lumberjack").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "lumberjack",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("miner").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "miner",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("smither").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "smither",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))).then(CommandManager.literal("warrior").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "warrior",
                                IntegerArgumentType.getInteger(commandContext, "level"), 1);
                    }))))
                    // Set values
                    .then(CommandManager.literal("set").then(CommandManager.literal("brewer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "brewer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("builder").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "builder",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("farmer").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "farmer",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("fisher").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "fisher",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("lumberjack").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "lumberjack",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("miner").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "miner",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("smither").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "smither",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))).then(CommandManager.literal("warrior").then(CommandManager.argument("level", IntegerArgumentType.integer()).executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "warrior",
                                IntegerArgumentType.getInteger(commandContext, "level"), 2);
                    }))))
                    // Print values
                    .then(CommandManager.literal("get").then(CommandManager.literal("brewer").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "brewer", 0, 3);
                    })).then(CommandManager.literal("all").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "all", 0, 3);
                    })).then(CommandManager.literal("builder").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "builder", 0, 3);
                    })).then(CommandManager.literal("farmer").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "farmer", 0, 3);
                    })).then(CommandManager.literal("fisher").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "fisher", 0, 3);
                    })).then(CommandManager.literal("lumberjack").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "lumberjack", 0, 3);
                    })).then(CommandManager.literal("miner").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "miner", 0, 3);
                    })).then(CommandManager.literal("smither").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "smither", 0, 3);
                    })).then(CommandManager.literal("warrior").executes((commandContext) -> {
                        return executeJobCommand(commandContext.getSource(), EntityArgumentType.getPlayers(commandContext, "targets"), "warrior", 0, 3);
                    })))));
        });
    }

    // Reference 0:Add, 1:Remove, 2:Set, 3:Print
    private static int executeJobCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String jobName, int i, int reference) {
        Iterator<ServerPlayerEntity> var3 = targets.iterator();

        i = MathHelper.abs(i);
        // loop over players
        while (var3.hasNext()) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) var3.next();
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();

            int playerJobLevel = jobsManager.getJobLevel(jobName);
            if (reference == 0) {
                playerJobLevel += i;
            }
            if (reference == 1) {
                playerJobLevel = playerJobLevel - i > 0 ? playerJobLevel - i : 0;
            }
            if (reference == 2) {
                playerJobLevel = i;
            }
            if (reference == 3) {
                if (jobName.equals("all")) {
                    for (int u = 0; u < jobStrings().size(); u++) {
                        final String finalBobName = jobStrings().get(u);
                        source.sendFeedback(() -> Text.translatable("commands.jobmanager.printLevel", serverPlayerEntity.getDisplayName(), StringUtils.capitalize(finalBobName) + " Level:",
                                jobsManager.getJobLevel(finalBobName)), true);
                    }
                } else {
                    final int finalPlayerJobLevel = playerJobLevel;
                    source.sendFeedback(
                            () -> Text.translatable("commands.jobmanager.printLevel", serverPlayerEntity.getDisplayName(), StringUtils.capitalize(jobName) + " Level:", finalPlayerJobLevel), true);
                }

                continue;
            }
            jobsManager.setJobLevel(jobName, playerJobLevel);

            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);

            if (reference != 3) {
                source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
            }
        }

        return targets.size();
    }

    private static final List<String> jobStrings() {
        return List.of("brewer", "builder", "farmer", "fisher", "lumberjack", "miner", "smither", "warrior");
    }
}
