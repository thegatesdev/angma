package com.thegates.angma.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.thegates.angma.AngerRegister;
import com.thegates.angma.Main;
import com.thegates.angma.TagOrTypeEntry;
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
import net.minecraft.tag.TagKey;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class AngmaCommand {

    public static final SuggestionProvider<ServerCommandSource> ALL_ENTITIES = SuggestionProviders.register(new Identifier("all_entities"), (commandContext, suggestionsBuilder) -> CommandSource.suggestFromIdentifier(Registry.ENTITY_TYPE.stream(), suggestionsBuilder, EntityType::getId, entityType -> new TranslatableText(Util.createTranslationKey("entity", EntityType.getId(entityType)))));
    public static final SuggestionProvider<ServerCommandSource> ALL_TAGS = SuggestionProviders.register(new Identifier("all_tags"), (commandContext, suggestionsBuilder) -> CommandSource.suggestFromIdentifier(Registry.ENTITY_TYPE.streamTags(), suggestionsBuilder, TagKey::id, identifier -> new TranslatableText(Util.createTranslationKey("entity_tag", identifier.id()))));


    public void register(CommandDispatcher<ServerCommandSource> dispatcher, @SuppressWarnings("unused") boolean dedicated) {
        LiteralArgumentBuilder<ServerCommandSource> baseCommand = CommandManager.literal("anger").requires(source -> source.hasPermissionLevel(2));

        baseCommand.then(CommandManager.literal("disable")
                .then(CommandManager.literal("entity")
                        .then(CommandManager.argument("for entities", EntityArgumentType.entities())
                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("from tag", IdentifierArgumentType.identifier()).suggests(ALL_TAGS)
                                                .executes(context -> addAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "for entities"), IdentifierArgumentType.getIdentifier(context, "from tag")))
                                        ))
                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("from type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                                .executes(context -> addAngerType(context.getSource(), EntityArgumentType.getEntities(context, "for entities"), IdentifierArgumentType.getIdentifier(context, "from type")))
                                        ))
                        )
                )
                .then(CommandManager.literal("global")
                        .then(CommandManager.argument("for type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                .then(CommandManager.argument("from type or tag", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES).suggests(ALL_TAGS)
                                        .executes(context -> globalAngerType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "for type"), IdentifierArgumentType.getIdentifier(context, "from type or tag    "), false))
                                ))
                )
                .then(CommandManager.literal("specific")
                        .then(CommandManager.argument("for entity", EntityArgumentType.entity())
                                .then(CommandManager.argument("from entity", EntityArgumentType.entity())
                                        .executes(context -> {
                                            Main.getAngerRegister().addSpecific(EntityArgumentType.getEntity(context, "for entity").getUuid(), EntityArgumentType.getEntity(context, "from entity").getUuid());
                                            context.getSource().sendFeedback(Text.of("Added specific entity to list."), false);
                                            return 1;
                                        })
                                ))
                )
        );

        baseCommand.then(CommandManager.literal("enable")
                .then(CommandManager.literal("entity")
                        .then(CommandManager.argument("for entities", EntityArgumentType.entities())
                                .then(CommandManager.literal("tag")
                                        .then(CommandManager.argument("from tag", IdentifierArgumentType.identifier()).suggests(ALL_TAGS)
                                                .executes(context -> removeAngerTag(context.getSource(), EntityArgumentType.getEntities(context, "for entities"), IdentifierArgumentType.getIdentifier(context, "from tag")))
                                        ))
                                .then(CommandManager.literal("type")
                                        .then(CommandManager.argument("from type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                                .executes(context -> removeAngerType(context.getSource(), EntityArgumentType.getEntities(context, "for entities"), IdentifierArgumentType.getIdentifier(context, "from type")))
                                        ))
                        )
                )
                .then(CommandManager.literal("specific")
                        .then(CommandManager.argument("for entity", EntityArgumentType.entity())
                                .then(CommandManager.argument("from entity", EntityArgumentType.entity())
                                        .executes(context -> {
                                            Main.getAngerRegister().removeSpecific(EntityArgumentType.getEntity(context, "for entity").getUuid(), EntityArgumentType.getEntity(context, "from entity").getUuid());
                                            context.getSource().sendFeedback(Text.of("Added specific entity to list."), false);
                                            return 1;
                                        })
                                ))
                )
                .then(CommandManager.literal("global")
                        .then(CommandManager.argument("for type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                .then(CommandManager.argument("from type", IdentifierArgumentType.identifier()).suggests(ALL_ENTITIES)
                                        .executes(context -> globalAngerType(context.getSource(), IdentifierArgumentType.getIdentifier(context, "for type"), IdentifierArgumentType.getIdentifier(context, "from type"), true))
                                ))
                )
        );

        baseCommand.then(CommandManager.literal("list").executes((context) -> listAngerFor(context.getSource())));

        baseCommand.then(CommandManager.literal("listGlobal").executes(context -> listGlobalAngerTo(context.getSource())));

        baseCommand.then(CommandManager.literal("target")
                .then(CommandManager.argument("entities", EntityArgumentType.entities())
                        .then(CommandManager.argument("target", EntityArgumentType.entity())
                                .executes((context -> setAngry(EntityArgumentType.getEntities(context, "entities"), EntityArgumentType.getEntity(context, "target")))))));

        dispatcher.register(baseCommand);
    }


    private int globalAngerType(ServerCommandSource source, Identifier forType, Identifier fromType, boolean remove) {
        String response;
        if (!remove) {
            Main.getAngerRegister().addGlobalMobType(forType, fromType);
            response = "Added " + forType.getPath() + ".";
        } else {
            Main.getAngerRegister().removeGlobalMobType(forType, fromType);
            response = "Removed " + forType.getPath() + ".";
        }

        source.sendFeedback(Text.of(response), false);

        return 1;
    }


    private int addAngerType(ServerCommandSource source, Collection<? extends Entity> targets, Identifier typeId) {
        Optional<EntityType<?>> type = EntityType.get(typeId.toString());
        if (type.isEmpty()) {
            return 0;
        }// TODO

        AngerRegister saver = Main.getAngerRegister();

        int success = 0;

        for (Entity target : targets) {
            if (saver.addMobType(target.getUuid(), EntityType.getId(type.get()))) {
                success++;
            }
        }

        if (success < targets.size()) {
            source.sendError(new LiteralText(targets.size() - success + " out of " + targets.size() + " failed!"));
        } else {
            source.sendFeedback(Text.of("Added " + typeId.getPath() + " for selected entities!"), false);
        }

        return 1;
    }


    private int removeAngerType(ServerCommandSource source, Collection<? extends Entity> targets, Identifier typeId) {

        AngerRegister saver = Main.getAngerRegister();
        for (Entity target : targets) {
            saver.removeMobType(target.getUuid(), typeId);
        }

        source.sendFeedback(Text.of("Removed " + typeId.getPath() + " from all entities!"), false);

        return 1;
    }


    private int addAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier) {
        AngerRegister saver = Main.getAngerRegister();
        targets.forEach(target -> saver.addTag(target.getUuid(), identifier));

        source.sendFeedback(Text.of("Added tag #" + identifier + " to selected entities!"), false);
        return 1;
    }


    private int removeAngerTag(ServerCommandSource source, Collection<? extends Entity> targets, Identifier identifier) {
        AngerRegister saver = Main.getAngerRegister();

        targets.forEach(entity -> saver.removeTag(entity.getUuid(), identifier));

        source.sendFeedback(Text.of("Removed tag #" + identifier + " From selected entities!"), false);

        return 1;
    }


    private int listAngerFor(ServerCommandSource source) {
        if (source.getEntity() == null) {
            source.sendError(Text.of(Main.MOD_PREFIX + "Console cannot list mobs!"));
            return 0;
        }
        AngerRegister angerRegister = source.getWorld().getPersistentStateManager().getOrCreate(AngerRegister::new, AngerRegister::new, Main.MOD_ID);

        List<Identifier> disabledTypes = angerRegister.getDisabledTypes(source.getEntity().getUuid());
        if (disabledTypes == null || disabledTypes.isEmpty()) {
            source.sendFeedback(Text.of("No types disabled."), false);
        } else {
            source.sendFeedback(Text.of("Disabled Types:"), false);
            disabledTypes.forEach(type -> source.sendFeedback(Text.of("-" + type.getPath()), false));
        }

        List<Identifier> disabledTags = angerRegister.getDisabledTags(source.getEntity().getUuid());
        if (disabledTags == null || disabledTags.isEmpty()) {
            source.sendFeedback(Text.of("No tags disabled."), false);
            return 0;
        } else {
            source.sendFeedback(Text.of("Disabled Tags:"), false);
            disabledTags.forEach(tag -> source.sendFeedback(Text.of("-" + tag.getPath()), false));
        }
        return 1;
    }


    private int listGlobalAngerTo(ServerCommandSource source) {
        Map<TagOrTypeEntry, Set<TagOrTypeEntry>> map = Main.getAngerRegister().getGlobalDisabled();
        if (map == null || map.isEmpty()) {
            source.sendFeedback(Text.of("No global anger disabled!"), false);
            return 1;
        }

        source.sendFeedback(Text.of("Global anger:"), false);

        map.forEach((identifier, identifiers) -> {
            source.sendFeedback(Text.of("-Anger disabled for " + identifier.string() + ":"), false);
            identifiers.forEach(identifier1 -> source.sendFeedback(Text.of("   -" + identifier1.string()), false));
        });

        return 1;
    }


    private int setAngry(Collection<? extends Entity> entities, Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            entities.forEach(entity -> {
                if (entity instanceof MobEntity mobEntity) {
                    mobEntity.setTarget(livingTarget);
                }
            });
        }
        return 1;
    }

}
