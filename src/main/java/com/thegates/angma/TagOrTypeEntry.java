package com.thegates.angma;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Optional;

public class TagOrTypeEntry {

    private final EntityType<?> entityType;
    private final TagKey<EntityType<?>> tagKey;

    public TagOrTypeEntry(EntityType<?> entityType) {
        this.entityType = entityType;
        this.tagKey = null;
    }

    public TagOrTypeEntry(TagKey<EntityType<?>> tagKey) {
        this.tagKey = tagKey;
        this.entityType = null;
    }

    public static TagOrTypeEntry type(Identifier identifier) {
        Optional<EntityType<?>> entityType = Registry.ENTITY_TYPE.getOrEmpty(identifier);
        return entityType.isEmpty() ? null : new TagOrTypeEntry(entityType.get());
    }

    public static TagOrTypeEntry tag(Identifier identifier) {
        return new TagOrTypeEntry(TagKey.of(Registry.ENTITY_TYPE.getKey(), identifier));
    }

    public boolean getTagKey() {
        return tagKey != null;
    }

    public boolean isType() {
        return entityType != null;
    }

    public String string() {
        if (tagKey != null) return "#" + tagKey;
        else if (entityType != null) return entityType.toString();
        else throw new IllegalStateException("Botj tagId and entityType are null!");
    }

    public boolean hasTag(EntityType<?> entityType) {
        Optional<RegistryKey<EntityType<?>>> key = Registry.ENTITY_TYPE.getKey(entityType);
        if (key.isEmpty()) return false;
        Optional<RegistryEntry<EntityType<?>>> entry = Registry.ENTITY_TYPE.getEntry(key.get());
        if (entry.isEmpty()) return false;
        return entry.get().streamTags().anyMatch(tagKey1 -> tagKey == tagKey1);
    }

    public static TagOrTypeEntry parse(Identifier identifier) {
        if (identifier == null) return null;
        Optional<EntityType<?>> entityType1 = EntityType.get(identifier.toString());
        return entityType1.map(TagOrTypeEntry::new).orElseGet(() -> TagOrTypeEntry.tag(identifier));
    }
}
