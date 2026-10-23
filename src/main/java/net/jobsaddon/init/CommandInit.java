package net.jobsaddon.init;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jobsaddon.access.JobsManagerAccess;
import net.jobsaddon.jobs.Job;
import net.jobsaddon.jobs.JobsManager;
import net.jobsaddon.network.JobsServerPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class CommandInit {

    private static final DynamicCommandExceptionType UNKNOWN_JOB_EXCEPTION = new DynamicCommandExceptionType(key -> Text.translatable("commands.jobsaddon.unknownJob", key));

    private static final SuggestionProvider<ServerCommandSource> JOBS_SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(
            JobsManager.JOBS.values().stream().map(Job::getKey), builder);
    private static final SuggestionProvider<ServerCommandSource> JOBS_AND_ALL_SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestMatching(
            Stream.concat(Stream.of("all"), JobsManager.JOBS.values().stream().map(Job::getKey)), builder);

    private enum Operation {
        ADD, REMOVE, SET
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("job").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                            .then(valueCommand("add", Operation.ADD))
                            .then(valueCommand("remove", Operation.REMOVE))
                            .then(valueCommand("set", Operation.SET))
                            .then(CommandManager.literal("get").then(CommandManager.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeGet(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(CommandManager.literal("employ").then(CommandManager.argument("job", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER)
                                    .executes(context -> executeEmploy(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), getJobs(context, false).getFirst()))))
                            .then(CommandManager.literal("quit").then(CommandManager.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeQuit(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(CommandManager.literal("reset").then(CommandManager.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeReset(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(CommandManager.literal("cooldown").then(CommandManager.argument("ticks", IntegerArgumentType.integer(0))
                                    .executes(context -> executeCooldown(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "ticks")))))));
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> valueCommand(String name, Operation operation) {
        return CommandManager.literal(name)
                .then(CommandManager.literal("level").then(jobAmountArgument(operation, false)))
                .then(CommandManager.literal("xp").then(jobAmountArgument(operation, true)))
                .then(jobAmountArgument(operation, false));
    }

    private static RequiredArgumentBuilder<ServerCommandSource, String> jobAmountArgument(Operation operation, boolean experience) {
        return CommandManager.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(0)).executes(context -> executeValue(context.getSource(), EntityArgumentType.getPlayers(context, "targets"),
                        getJobs(context, true), operation, experience, IntegerArgumentType.getInteger(context, "amount"))));
    }

    private static List<Job> getJobs(CommandContext<ServerCommandSource> context, boolean allowAll) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "job");
        if (allowAll && key.equals("all")) {
            return JobsManager.JOBS.values().stream().sorted(Comparator.comparingInt(Job::getId)).toList();
        }
        Job job = JobsManager.getJobByKey(key);
        if (job == null) {
            throw UNKNOWN_JOB_EXCEPTION.create(key);
        }
        return List.of(job);
    }

    private static int executeValue(ServerCommandSource source, Collection<ServerPlayerEntity> targets, List<Job> jobs, Operation operation, boolean experience, int amount) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                int jobId = job.getId();
                if (experience) {
                    switch (operation) {
                        case ADD -> jobsManager.addJobXP(serverPlayerEntity, jobId, amount);
                        case REMOVE -> jobsManager.setJobXP(jobId, jobsManager.getJobXP(jobId) - amount);
                        case SET -> {
                            jobsManager.setJobXP(jobId, 0);
                            jobsManager.addJobXP(serverPlayerEntity, jobId, amount);
                        }
                    }
                } else {
                    switch (operation) {
                        case ADD -> jobsManager.setJobLevel(jobId, jobsManager.getJobLevel(jobId) + amount);
                        case REMOVE -> jobsManager.setJobLevel(jobId, jobsManager.getJobLevel(jobId) - amount);
                        case SET -> jobsManager.setJobLevel(jobId, amount);
                    }
                }
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeGet(ServerCommandSource source, Collection<ServerPlayerEntity> targets, List<Job> jobs) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                int jobId = job.getId();
                Text text = Text.translatable("commands.jobsaddon.info", serverPlayerEntity.getDisplayName(), job.getJobName(), jobsManager.getJobLevel(jobId), job.getMaxLevel(),
                        jobsManager.getJobXP(jobId), jobsManager.getNextJobLevelExperience(jobId));
                if (jobsManager.isEmployedJob(jobId)) {
                    text = text.copy().append(" ").append(Text.translatable("commands.jobsaddon.employed"));
                }
                Text finalText = text;
                source.sendFeedback(() -> finalText, false);
            }
        }
        return targets.size();
    }

    private static int executeEmploy(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Job job) {
        int count = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            if (jobsManager.isEmployedJob(job.getId())) {
                source.sendError(Text.translatable("commands.jobsaddon.alreadyEmployed", serverPlayerEntity.getDisplayName(), job.getJobName()));
                continue;
            }
            if (!jobsManager.hasFreeJobSlot()) {
                source.sendError(Text.translatable("commands.jobsaddon.noFreeJobSlot", serverPlayerEntity.getDisplayName()));
                continue;
            }
            jobsManager.employJob(job.getId());
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
            count++;
        }
        return count;
    }

    private static int executeQuit(ServerCommandSource source, Collection<ServerPlayerEntity> targets, List<Job> jobs) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                jobsManager.quitJob(job.getId());
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeReset(ServerCommandSource source, Collection<ServerPlayerEntity> targets, List<Job> jobs) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                jobsManager.setJobLevel(job.getId(), 0);
                jobsManager.setJobXP(job.getId(), 0);
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeCooldown(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int ticks) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            jobsManager.setEmployedJobTime(ticks);
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendFeedback(() -> Text.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

}
