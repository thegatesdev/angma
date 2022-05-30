package com.thegates.angma;

import net.minecraft.entity.EntityType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
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

    public boolean isTagKey() {
        return tagKey != null;
    }

    public boolean isType() {
        return entityType != null;
    }

    public String string() {
        if (tagKey != null) return tagKey.id().toString();
        else if (entityType != null) return entityType.getUntranslatedName();
        else throw new IllegalStateException("Both tagId and entityType are null!");
    }

    public static TagOrTypeEntry parse(Identifier identifier) {
        if (identifier == null) return null;
        Optional<EntityType<?>> entityType1 = Registry.ENTITY_TYPE.getOrEmpty(identifier);
        return entityType1.map(TagOrTypeEntry::new).orElseGet(() -> TagOrTypeEntry.tag(identifier));
    }

    public static TagOrTypeEntry parse(String string) {
        return parse(Identifier.tryParse(string));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagOrTypeEntry that = (TagOrTypeEntry) o;
        return Objects.equals(entityType, that.entityType) && Objects.equals(tagKey, that.tagKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, tagKey);
    }
}
