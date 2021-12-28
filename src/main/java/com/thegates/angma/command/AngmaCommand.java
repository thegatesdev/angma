package com.thegates.angma.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.thegates.angma.Main;
import com.thegates.angma.Saver;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EntitySummonArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class AngmaCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, @SuppressWarnings("unused") boolean dedicated){
        LiteralArgumentBuilder<ServerCommandSource> baseCommand = CommandManager.literal("anger").requires(source -> source.hasPermissionLevel(2));


        baseCommand.then(CommandManager.literal("disable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())

                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("entity type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                                .executes((context) -> addAngerMob(context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntitySummonArgumentType.getEntitySummon(context, "entity type")))))

                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("entity tag", IdentifierArgumentType.identifier())
                                                .executes((context -> addAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity tag"))))))
                                ));

        baseCommand.then(CommandManager.literal("disableGlobal")

                .then(CommandManager.argument("for type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .then(CommandManager.literal("type")
                                .then(CommandManager.argument("type to disable", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes(context -> addGlobalAngerType(context.getSource(), EntitySummonArgumentType.getEntitySummon(context, "for type"), EntitySummonArgumentType.getEntitySummon(context, "type to disable")))
                                ))));

        baseCommand.then(CommandManager.literal("enable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())

                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("entity type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                                .executes((context) -> removeAngerMob(context.getSource(), EntityArgumentType.getEntities(context, "targets"), EntitySummonArgumentType.getEntitySummon(context, "entity type")))))

                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("entity tag", IdentifierArgumentType.identifier())
                                                .executes((context -> removeAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity tag"))))))
                                ));

        baseCommand.then(CommandManager.literal("enableGlobal")

                .then(CommandManager.argument("for type", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                        .then(CommandManager.literal("type")
                                .then(CommandManager.argument("type to enable", EntitySummonArgumentType.entitySummon()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                                        .executes(context -> removeGlobalAngerType(context.getSource(), EntitySummonArgumentType.getEntitySummon(context, "for type"), EntitySummonArgumentType.getEntitySummon(context, "type to enable")))
                                ))));

        baseCommand.then(CommandManager.literal("list").executes((context) -> listAnger(context.getSource())));

        baseCommand.then(CommandManager.literal("target")
                        .then(CommandManager.argument("entities", EntityArgumentType.entities())
                                .then(CommandManager.argument("target", EntityArgumentType.entity())
                                        .executes((context -> setAngry(context.getSource(), EntityArgumentType.getEntities(context, "entities"), EntityArgumentType.getEntity(context, "target")))))));

        dispatcher.register(baseCommand);
    }


    private static int addGlobalAngerType(ServerCommandSource source, Identifier type, Identifier typeToDisable){

        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        saver.addGlobalMobType(type, typeToDisable);

        return 1;
    }

    private static int removeGlobalAngerType(ServerCommandSource source, Identifier type, Identifier typeToEnable){
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        saver.removeGlobalMobType(type, typeToEnable);

        return 1;
    }

    private static int addAngerMob(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Optional<EntityType<?>> type = EntityType.get(identifier.toString());
        if (type.isEmpty()){
            return 0;
        }

        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        int success = 0;

        for (Entity target : targets) {
            if (saver.addMobType(target.getUuid(), EntityType.getId(type.get()))){
                success++;
            }
        }

        if (success < targets.size()) {
            source.sendError(new LiteralText(targets.size() - success + "out of "+ targets.size() + "failed."));
            return 0;
        }
        return 1;
    }

    private static int removeAngerMob(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){

        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        for (Entity target : targets) {
            saver.removeMobType(target.getUuid(), identifier);
        }

        return 1;
    }

    private static int addAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);
        targets.forEach(target -> saver.addTag(target.getUuid(), EntityTypeTags.getTagGroup().getTag(identifier)));

        return 1;
    }

    private static int removeAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        targets.forEach(entity -> saver.removeTag(entity.getUuid(), EntityTypeTags.getTagGroup().getTag(identifier)));



        return 1;
    }

    private static int listAnger(ServerCommandSource source){
        if (source.getEntity() == null){
            source.sendError(new LiteralText("[Angma]: Console cannot list mobs."));
            return 0;
        }
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        Set<Identifier> disabledTypes = saver.getDisabledTypes(source.getEntity().getUuid());
        if (disabledTypes == null){
            source.sendError(new LiteralText("No types disabled."));
        }else{
            source.sendFeedback(new LiteralText("Disabled Types:"), false);
            disabledTypes.forEach(type -> source.sendFeedback(new LiteralText(type.toString()), false));
        }

        Set<Identifier> disabledTags = saver.getDisabledTags(source.getEntity().getUuid());
        if (disabledTags == null){
            source.sendError(new LiteralText("No tags disabled."));
            return 0;
        }else {
            source.sendFeedback(new LiteralText("Disabled Tags:"), false);
            disabledTags.forEach(tag -> source.sendFeedback(new LiteralText(tag.toString()), false));
        }
        return 1;
    }

    private static int setAngry(ServerCommandSource source, Collection<? extends Entity> entities, Entity target){
        entities.forEach(entity -> {
            try {
                ((MobEntity) entity).setTarget((LivingEntity) target);
            }catch (Exception e){
                source.sendError(new LiteralText("Could not set anger."));
            }
        });
        return 1;
    }

}
