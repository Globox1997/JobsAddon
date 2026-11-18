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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class CommandInit {

    private static final DynamicCommandExceptionType UNKNOWN_JOB_EXCEPTION = new DynamicCommandExceptionType(key -> Component.translatable("commands.jobsaddon.unknownJob", key));

    private static final SuggestionProvider<CommandSourceStack> JOBS_SUGGESTION_PROVIDER = (context, builder) -> SharedSuggestionProvider.suggest(
            JobsManager.JOBS.values().stream().map(Job::getKey), builder);
    private static final SuggestionProvider<CommandSourceStack> JOBS_AND_ALL_SUGGESTION_PROVIDER = (context, builder) -> SharedSuggestionProvider.suggest(
            Stream.concat(Stream.of("all"), JobsManager.JOBS.values().stream().map(Job::getKey)), builder);

    private enum Operation {
        ADD, REMOVE, SET
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("job").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .then(Commands.argument("targets", EntityArgument.players())
                            .then(valueCommand("add", Operation.ADD))
                            .then(valueCommand("remove", Operation.REMOVE))
                            .then(valueCommand("set", Operation.SET))
                            .then(Commands.literal("get").then(Commands.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeGet(context.getSource(), EntityArgument.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(Commands.literal("employ").then(Commands.argument("job", StringArgumentType.string()).suggests(JOBS_SUGGESTION_PROVIDER)
                                    .executes(context -> executeEmploy(context.getSource(), EntityArgument.getPlayers(context, "targets"), getJobs(context, false).getFirst()))))
                            .then(Commands.literal("quit").then(Commands.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeQuit(context.getSource(), EntityArgument.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(Commands.literal("reset").then(Commands.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                                    .executes(context -> executeReset(context.getSource(), EntityArgument.getPlayers(context, "targets"), getJobs(context, true)))))
                            .then(Commands.literal("cooldown").then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                    .executes(context -> executeCooldown(context.getSource(), EntityArgument.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "ticks")))))));
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> valueCommand(String name, Operation operation) {
        return Commands.literal(name)
                .then(Commands.literal("level").then(jobAmountArgument(operation, false)))
                .then(Commands.literal("xp").then(jobAmountArgument(operation, true)))
                .then(jobAmountArgument(operation, false));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> jobAmountArgument(Operation operation, boolean experience) {
        return Commands.argument("job", StringArgumentType.string()).suggests(JOBS_AND_ALL_SUGGESTION_PROVIDER)
                .then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes(context -> executeValue(context.getSource(), EntityArgument.getPlayers(context, "targets"),
                        getJobs(context, true), operation, experience, IntegerArgumentType.getInteger(context, "amount"))));
    }

    private static List<Job> getJobs(CommandContext<CommandSourceStack> context, boolean allowAll) throws CommandSyntaxException {
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

    private static int executeValue(CommandSourceStack source, Collection<ServerPlayer> targets, List<Job> jobs, Operation operation, boolean experience, int amount) {
        for (ServerPlayer serverPlayerEntity : targets) {
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
                        case ADD -> jobsManager.setJobLevel(jobId, (int) Math.min((long) jobsManager.getJobLevel(jobId) + amount, Integer.MAX_VALUE));
                        case REMOVE -> jobsManager.setJobLevel(jobId, jobsManager.getJobLevel(jobId) - amount);
                        case SET -> jobsManager.setJobLevel(jobId, amount);
                    }
                }
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendSuccess(() -> Component.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeGet(CommandSourceStack source, Collection<ServerPlayer> targets, List<Job> jobs) {
        for (ServerPlayer serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                int jobId = job.getId();
                Component text = Component.translatable("commands.jobsaddon.info", serverPlayerEntity.getDisplayName(), job.getJobName(), jobsManager.getJobLevel(jobId), job.getMaxLevel(),
                        jobsManager.getJobXP(jobId), jobsManager.getNextJobLevelExperience(jobId));
                if (jobsManager.isEmployedJob(jobId)) {
                    text = text.copy().append(" ").append(Component.translatable("commands.jobsaddon.employed"));
                }
                Component finalText = text;
                source.sendSuccess(() -> finalText, false);
            }
        }
        return targets.size();
    }

    private static int executeEmploy(CommandSourceStack source, Collection<ServerPlayer> targets, Job job) {
        int count = 0;
        for (ServerPlayer serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            if (jobsManager.isEmployedJob(job.getId())) {
                source.sendFailure(Component.translatable("commands.jobsaddon.alreadyEmployed", serverPlayerEntity.getDisplayName(), job.getJobName()));
                continue;
            }
            if (!jobsManager.hasFreeJobSlot()) {
                source.sendFailure(Component.translatable("commands.jobsaddon.noFreeJobSlot", serverPlayerEntity.getDisplayName()));
                continue;
            }
            jobsManager.employJob(job.getId());
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendSuccess(() -> Component.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
            count++;
        }
        return count;
    }

    private static int executeQuit(CommandSourceStack source, Collection<ServerPlayer> targets, List<Job> jobs) {
        for (ServerPlayer serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                jobsManager.quitJob(job.getId());
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendSuccess(() -> Component.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeReset(CommandSourceStack source, Collection<ServerPlayer> targets, List<Job> jobs) {
        for (ServerPlayer serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            for (Job job : jobs) {
                jobsManager.setJobLevel(job.getId(), 0);
                jobsManager.setJobXP(job.getId(), 0);
            }
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendSuccess(() -> Component.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeCooldown(CommandSourceStack source, Collection<ServerPlayer> targets, int ticks) {
        for (ServerPlayer serverPlayerEntity : targets) {
            JobsManager jobsManager = ((JobsManagerAccess) serverPlayerEntity).getJobsManager();
            jobsManager.setEmployedJobTime(ticks);
            JobsServerPacket.writeS2CJobPacket(jobsManager, serverPlayerEntity);
            source.sendSuccess(() -> Component.translatable("commands.jobmanager.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

}
