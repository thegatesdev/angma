package com.thegates.angma.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;

public class EntityIdArgumentType implements ArgumentType<Identifier> {

    public static final SuggestionProvider<ServerCommandSource> SUMMONABLE_ENTITIES_AND_PLAYER = SuggestionProviders.register(new Identifier("summonable_entities_and_player"), (commandContext, suggestionsBuilder) -> CommandSource.suggestFromIdentifier(Registry.ENTITY_TYPE.stream().filter(entityType -> entityType.isSummonable() || entityType.getUntranslatedName().equalsIgnoreCase("player")), suggestionsBuilder, EntityType::getId, entityType -> new TranslatableText(Util.createTranslationKey("entity", EntityType.getId(entityType)))));


    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:pig", "player");
    public static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((id) -> new TranslatableText("entity.notFound", id));

    public EntityIdArgumentType() {
    }

    public static Identifier getEntityId(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return EntityIdArgumentType.validate(context.getArgument(name, Identifier.class));
    }

    public static EntityIdArgumentType entityId() {
        return new EntityIdArgumentType();
    }

    private static Identifier validate(Identifier id) throws CommandSyntaxException {
        if (id.getPath().equalsIgnoreCase("player")){
            return id;
        }
        Registry.ENTITY_TYPE.getOrEmpty(id).filter(EntityType::isSummonable).orElseThrow(() -> NOT_FOUND_EXCEPTION.create(id));
        return id;
    }

    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return validate(Identifier.fromCommandInput(stringReader));
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
