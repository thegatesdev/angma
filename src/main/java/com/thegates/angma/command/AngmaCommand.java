package com.thegates.angma.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.thegates.angma.Main;
import com.thegates.angma.Saver;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AngmaCommand {



    public static final SuggestionProvider<ServerCommandSource> ALL_ENTITIES = SuggestionProviders.register(new Identifier("all_entities"), (commandContext, suggestionsBuilder) -> CommandSource.suggestFromIdentifier(Registry.ENTITY_TYPE.stream(), suggestionsBuilder, EntityType::getId, entityType -> new TranslatableText(Util.createTranslationKey("entity", EntityType.getId(entityType)))));
    public static final SuggestionProvider<ServerCommandSource> ALL_TAGS = SuggestionProviders.register(new Identifier("all_tags"), (commandContext, suggestionsBuilder) -> CommandSource.suggestFromIdentifier(EntityTypeTags.getTagGroup().getTags().keySet(), suggestionsBuilder, i -> i, identifier -> new TranslatableText(Util.createTranslationKey("entity", identifier))));




    public void register(CommandDispatcher<ServerCommandSource> dispatcher, @SuppressWarnings("unused") boolean dedicated){
        LiteralArgumentBuilder<ServerCommandSource> baseCommand = CommandManager.literal("anger").requires(source -> source.hasPermissionLevel(2));


        baseCommand.then(CommandManager.literal("disable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())

                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("entity type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                                .executes((context) -> addAngerMob(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity type")))))

                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("entity tag", IdentifierArgumentType.identifier()).suggests(ALL_TAGS)
                                                .executes((context -> addAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity tag"))))))
                                ));

        baseCommand.then(CommandManager.literal("disableGlobal")

                .then(CommandManager.argument("for type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                        .then(CommandManager.literal("type")
                                .then(CommandManager.argument("type to disable", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                        .executes(context -> addGlobalAngerType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "for type"), IdentifierArgumentType.getIdentifier(context, "type to disable")))
                                ))));

        baseCommand.then(CommandManager.literal("enable")
                        .then(CommandManager.argument("targets", EntityArgumentType.entities())

                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("entity type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                                .executes((context) -> removeAngerMob(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity type")))))

                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("entity tag", IdentifierArgumentType.identifier()).suggests(ALL_TAGS)
                                                .executes((context -> removeAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "targets"), IdentifierArgumentType.getIdentifier(context, "entity tag"))))))
                                ));

        baseCommand.then(CommandManager.literal("enableGlobal")

                .then(CommandManager.argument("for type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                        .then(CommandManager.literal("type")
                                .then(CommandManager.argument("type to enable", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                        .executes(context -> removeGlobalAngerType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "for type"), IdentifierArgumentType.getIdentifier(context, "type to enable")))
                                ))));

        baseCommand.then(CommandManager.literal("list").executes((context) -> listAngerFor(context.getSource())));

        baseCommand.then(CommandManager.literal("listGlobal").executes(context -> listGlobalAngerTo(context.getSource())));

        baseCommand.then(CommandManager.literal("target")
                        .then(CommandManager.argument("entities", EntityArgumentType.entities())
                                .then(CommandManager.argument("target", EntityArgumentType.entity())
                                        .executes((context -> setAngry(EntityArgumentType.getEntities(context, "entities"), EntityArgumentType.getEntity(context, "target")))))));

        dispatcher.register(baseCommand);
    }


    private int addGlobalAngerType(ServerCommandSource source, Identifier type, Identifier typeToDisable){

        Main.getSaver().addGlobalMobType(type, typeToDisable);

        source.sendFeedback(Text.of("Added "+type.getPath()+"!"), false);

        return 1;
    }

    private int removeGlobalAngerType(ServerCommandSource source, Identifier type, Identifier typeToEnable){
        Main.getSaver().removeGlobalMobType(type, typeToEnable);

        source.sendFeedback(Text.of("Removed "+type.getPath()+"!"), false);

        return 1;
    }

    private int addAngerMob(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Optional<EntityType<?>> type = EntityType.get(identifier.toString());
        if (type.isEmpty()){
            return 0;
        }

        Saver saver = Main.getSaver();

        int success = 0;

        for (Entity target : targets) {
            if (saver.addMobType(target.getUuid(), EntityType.getId(type.get()))){
                success++;
            }
        }

        if (success < targets.size()) {
            source.sendError(new LiteralText(targets.size() - success + " out of "+ targets.size() + " failed!"));
        }else{
            source.sendFeedback(Text.of("Added "+identifier.getPath()+" for selected entities!"), false);
        }

        return 1;
    }

    private int removeAngerMob(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){

        Saver saver = Main.getSaver();
        for (Entity target : targets) {
            saver.removeMobType(target.getUuid(), identifier);
        }

        source.sendFeedback(Text.of("Removed "+identifier.getPath()+" from all entities!"), false);

        return 1;
    }

    private int addAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Saver saver = Main.getSaver();
        targets.forEach(target -> saver.addTag(target.getUuid(), EntityTypeTags.getTagGroup().getTag(identifier)));

        source.sendFeedback(Text.of("Added tag #"+identifier+" to selected entities!"), false);
        return 1;
    }

    private int removeAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier){
        Saver saver = Main.getSaver();

        targets.forEach(entity -> saver.removeTag(entity.getUuid(), EntityTypeTags.getTagGroup().getTag(identifier)));

        source.sendFeedback(Text.of("Removed tag #"+identifier+" From selected entities!"), false);

        return 1;
    }

    private int listAngerFor(ServerCommandSource source){
        if (source.getEntity() == null){
            source.sendError(Text.of(Main.MOD_PREFIX + "Console cannot list mobs!"));
            return 0;
        }
        Saver saver = source.getWorld().getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID);

        Set<Identifier> disabledTypes = saver.getDisabledTypes(source.getEntity().getUuid());
        if (disabledTypes == null || disabledTypes.isEmpty()){
            source.sendFeedback(Text.of("No types disabled."), false);
        }else{
            source.sendFeedback(Text.of("Disabled Types:"), false);
            disabledTypes.forEach(type -> source.sendFeedback(Text.of("-"+type.getPath()), false));
        }

        Set<Identifier> disabledTags = saver.getDisabledTags(source.getEntity().getUuid());
        if (disabledTags == null || disabledTags.isEmpty()){
            source.sendFeedback(Text.of("No tags disabled."), false);
            return 0;
        }else {
            source.sendFeedback(Text.of("Disabled Tags:"), false);
            disabledTags.forEach(tag -> source.sendFeedback(Text.of("-"+tag.getPath()), false));
        }
        return 1;
    }

    private int listGlobalAngerTo(ServerCommandSource source){
        Map<Identifier, Set<Identifier>> map = Main.getSaver().getGlobalDisabled();
        if (map == null || map.isEmpty()){
            source.sendFeedback(Text.of("No global anger disabled!"), false);
            return 1;
        }

        source.sendFeedback(Text.of("Global anger:"),false);

        map.forEach((identifier, identifiers) -> {
            source.sendFeedback(Text.of("-Anger disabled for "+identifier.getPath()+ ":"), false);
            identifiers.forEach(identifier1 -> source.sendFeedback(Text.of("   -"+identifier1.getPath()), false));
        });

        return 1;
    }

    private int setAngry(Collection<? extends Entity> entities, Entity target){
        entities.forEach(entity -> {
            try {
                ((MobEntity) entity).setTarget((LivingEntity) target);
            }catch (Exception ignored){}
        });
        return 1;
    }

}
