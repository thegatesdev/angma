package com.thegates.angma;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Util {

    public static EntityType<?> typeFromId(Identifier identifier) {
        return Registry.ENTITY_TYPE.getOrEmpty(identifier).orElse(null);
    }

    public static TagKey<EntityType<?>> tagKeyFromId(Identifier identifier) {
        return TagKey.of(Registry.ENTITY_TYPE.getKey(), identifier);
    }

    public static Object tagKeyOrType(Identifier identifier) {
        EntityType<?> entityType = typeFromId(identifier);
        return entityType == null ? tagKeyFromId(identifier) : entityType;
    }

    public static Object tagKeyOrType(String identifier) {
        return tagKeyOrType(Identifier.tryParse(identifier));
    }

    public static boolean isTagKey(Object o) {
        return o instanceof TagKey<?>;
    }

    public static boolean isType(Object o) {
        return o instanceof EntityType<?>;
    }

    public static <T> T tryCast(Class<T> clazz, Object o) {
        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public static String string(Object o) {
        TagKey<?> tagKey = tryCast(TagKey.class, o);
        if (tagKey != null) return tagKey.id().toString();
        EntityType<?> entityType = tryCast(EntityType.class, o);
        if (entityType != null) return entityType.getUntranslatedName();
        return "";
    }
}
