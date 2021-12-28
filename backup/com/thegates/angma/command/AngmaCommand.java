package com.thegates.angma.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thegates.angma.Main;
import com.thegates.angma.Saver;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class AngmaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated){
        LiteralArgumentBuilder<ServerCommandSource> baseCommand = CommandManager.literal("anger").requires(source -> source.hasPermissionLevel(2))

                .then(CommandManager.literal("disable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("entity type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes((context) -> disableAnger(context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntitySummonArgumentType.getEntitySummon(context, "entity type"))))))

                .then(CommandManager.literal("enable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())
                                .then(CommandManager.argument("entity type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes((context) -> enableAnger(context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntitySummonArgumentType.getEntitySummon(context, "entity type"))))))

                .then(CommandManager.literal("list").executes((context) -> listAnger(context.getSource())));

                //.then(CommandManager.literal("setAngry")
                        //.then(CommandManager.argument("targets", EntityArgumentType.entities())
                                //.then(CommandManager.argument("angryAt", EntityArgumentType.entities())
                                        //.executes((context -> setAngry(context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntityArgumentType.getEntities(context, "angryAt")))))));

        dispatcher.register(baseCommand);
    }

    private static int setAngry(ServerCommandSource source, Collection<? extends Entity> targets, Collection<? extends Entity> angryAt){
        targets.forEach(angryEntity -> {
            angryAt.forEach(targetEntity -> {
                try {
                    ((MobEntity) angryEntity).setTarget((LivingEntity) targetEntity);
                }catch (Exception e){
                    source.sendError(new LiteralText("Could not set anger."));
                }
            });
        });
        return 1;
    }

    private static int disableAnger(ServerCommandSource source, Collection<? extends Entity> targets, Identifier entityType){

        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);
        for (Entity target : targets) {
            boolean added = saver.addMob(target.getUuid(), entityType);
            if (!added){
                source.sendError(new LiteralText("[Angma]: Not added because it already existed!"));
                return -1;
            }
        }

        source.sendFeedback(new LiteralText("[Angma]: Anger off for \""+ entityType.toString()+"\" for "+targets.size()+" entities."), false);

        return 1;
    }

    private static int enableAnger(ServerCommandSource source, Collection<? extends Entity> targets, Identifier entityType){

        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);
        for (Entity target : targets) {
            saver.removeMob(target.getUuid(), entityType);
        }

        source.sendFeedback(new LiteralText("[Angma]: Anger on for: "+ entityType.toString()+" for "+targets.size()+" entities."), false);


        return 1;
    }

    private static int listAnger(ServerCommandSource source){
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);
        if (source.getEntity() == null){source.sendFeedback(new LiteralText("[Angma]: Console cannot list mobs."), false);}
        Entity executor = source.getEntity();
        if (saver.getList().get(executor.getUuid()) == null || saver.getList().isEmpty()) {
            source.sendError(new LiteralText("[Angma]: Nothing saved in list!"));
            return 0;
        }
        source.sendFeedback(new LiteralText("List of mobs: "), false);
        saver.getList().get(executor.getUuid()).forEach(identifier -> source.sendFeedback(new LiteralText(identifier.toString()), false));

        return 1;
    }

}
