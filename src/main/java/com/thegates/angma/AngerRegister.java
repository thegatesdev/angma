package com.thegates.angma;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class AngerRegister extends PersistentState {

    // Information to be saved into nbt and back.
    private final DisabledContainer<UUID, TagOrTypeEntry> entityDisabled = new DisabledContainer<>();
    private final DisabledContainer<UUID, UUID> specificEntityDisabled = new DisabledContainer<>();
    private final DisabledContainer<TagOrTypeEntry, TagOrTypeEntry> globalDisabled = new DisabledContainer<>();


    // Default constructor, used for getting the persistentState.
    public AngerRegister() {
    }


    // Constructor to immediately create the data from nbt.
    public AngerRegister(@NotNull NbtCompound nbt) {
        createFromNbt(nbt);
    }


    // Create the information from the loaded nbt.
    private void createFromNbt(@NotNull NbtCompound nbt) {
        entityDisabled.clear();
        globalDisabled.clear();
        specificEntityDisabled.clear();
        NbtCompound entityDisabledInput = nbt.getCompound("playerDisabled");
        NbtCompound globalDisabledInput = nbt.getCompound("typeDisabled");
        NbtCompound specificDisabledInput = nbt.getCompound("specificDisabled");

        entityDisabledInput.getKeys().forEach(uuidString -> {
            NbtList nbtList = (NbtList) entityDisabledInput.get(uuidString);
            if (nbtList == null) return;
            UUID uuid = UUID.fromString(uuidString);
            nbtList.forEach(nbtElement -> {
                TagOrTypeEntry parsed = TagOrTypeEntry.parse(Identifier.tryParse(nbtElement.asString()));
                if (parsed != null) entityDisabled.putOverHead(uuid, parsed, nbtList.size());
            });
        });

        globalDisabledInput.getKeys().forEach(identifierString -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(identifierString);
            if (nbtList == null) return;
            Identifier identifier = Identifier.tryParse(identifierString);
            nbtList.forEach(nbtElement -> {
                TagOrTypeEntry parse = TagOrTypeEntry.parse(Identifier.tryParse(nbtElement.asString()));
                if (parse != null) globalDisabled.putOverHead(TagOrTypeEntry.parse(identifier), parse, nbtList.size());
            });
        });

        specificDisabledInput.getKeys().forEach(key -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(key);
            if (nbtList == null) return;
            UUID uuid = UUID.fromString(key);
            nbtList.forEach(nbtElement -> specificEntityDisabled.putOverHead(uuid, UUID.fromString(nbtElement.asString()), nbtList.size()));
        });
    }


    // WriteNbt is called when a PersistentState is marked dirty, and the game saves.
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("playerDisabled", entityDisabled.populateNbt(UUID::toString, TagOrTypeEntry::string));
        nbt.put("typeDisabled", globalDisabled.populateNbt(TagOrTypeEntry::string, TagOrTypeEntry::string));
        nbt.put("specificDisabled", specificEntityDisabled.populateNbt(UUID::toString, UUID::toString));
        return nbt;
    }


    public List<Identifier> getDisabledTypes(UUID uuid) {
        return entityDisabled.get(uuid).stream().filter(TagOrTypeEntry::isType).map(TagOrTypeEntry::string).map(Identifier::tryParse).filter(Objects::nonNull).toList();
    }


    public List<Identifier> getDisabledTags(UUID uuid) {
        return entityDisabled.get(uuid).stream().filter(TagOrTypeEntry::getTagKey).map(TagOrTypeEntry::string).map(Identifier::tryParse).filter(Objects::nonNull).toList();
    }


    public Map<TagOrTypeEntry, Set<TagOrTypeEntry>> getGlobalDisabled() {
        return globalDisabled.read();
    }


    public boolean addMobType(UUID entityUUID, Identifier type) {
        boolean ret = entityDisabled.put(entityUUID, TagOrTypeEntry.type(type));
        markDirty();
        return ret;
    }


    public boolean removeMobType(UUID entityUUID, Identifier type) {
        boolean ret = entityDisabled.remove(entityUUID, TagOrTypeEntry.type(type));
        markDirty();
        return ret;
    }


    public boolean addTag(UUID entityUUID, Identifier tag) {
        boolean ret = entityDisabled.put(entityUUID, TagOrTypeEntry.tag(tag));
        markDirty();
        return ret;
    }


    public boolean removeTag(UUID entityUUID, Identifier tagId) {
        boolean ret = entityDisabled.remove(entityUUID, TagOrTypeEntry.tag(tagId));
        markDirty();
        return ret;
    }


    public boolean addSpecific(UUID entity, UUID targeter) {
        boolean ret = specificEntityDisabled.put(entity, targeter);
        markDirty();
        return ret;
    }


    public boolean removeSpecific(UUID entity, UUID targeter) {
        boolean ret = specificEntityDisabled.remove(entity, targeter);
        markDirty();
        return ret;
    }


    public boolean addGlobalMobType(Identifier key, Identifier toDisable) {
        boolean ret = globalDisabled.put(TagOrTypeEntry.parse(key), TagOrTypeEntry.parse(toDisable));
        markDirty();
        return ret;
    }


    public boolean removeGlobalMobType(Identifier key, Identifier toEnable) {
        boolean ret = globalDisabled.remove(TagOrTypeEntry.parse(key), TagOrTypeEntry.parse(toEnable));
        markDirty();
        return ret;
    }

    public boolean isAngerDisabled(Entity entity1, Entity entity2) {
        UUID uuid1 = entity1.getUuid();
        EntityType<?> type2 = entity2.getType();
        {   // Check TYPES (and specific)
            boolean ret = isEntityAngerDisabled(uuid1, type2) || isSpecificEntityAngerDisabled(uuid1, entity2.getUuid()) || isGlobalAngerDisabled(entity1.getType(), type2);
            if (ret) return true;
        }
        {   // Check TAGS
            Optional<RegistryKey<EntityType<?>>> key = Registry.ENTITY_TYPE.getKey(type2);
            if (key.isEmpty()) return false;
            Optional<RegistryEntry<EntityType<?>>> entry = Registry.ENTITY_TYPE.getEntry(key.get());
            if (entry.isEmpty()) return false;
            return entry.get().streamTags().anyMatch(tagKey -> isEntityAngerDisabled(uuid1, tagKey) || isGlobalAngerDisabled(type2, tagKey));
        }
    }

    public boolean isGlobalAngerDisabled(EntityType<?> key1, EntityType<?> key2) {
        return globalDisabled.has(new TagOrTypeEntry(key1), new TagOrTypeEntry(key2));
    }

    public boolean isGlobalAngerDisabled(EntityType<?> key1, TagKey<EntityType<?>> key2) {
        return globalDisabled.has(new TagOrTypeEntry(key1), new TagOrTypeEntry(key2));
    }

    public boolean isEntityAngerDisabled(UUID uuid, EntityType<?> type) {
        return entityDisabled.has(uuid, new TagOrTypeEntry(type));
    }

    public boolean isEntityAngerDisabled(UUID uuid, TagKey<EntityType<?>> tag) {
        return entityDisabled.has(uuid, new TagOrTypeEntry(tag));
    }

    public boolean isSpecificEntityAngerDisabled(UUID uuid1, UUID uuid2) {
        return specificEntityDisabled.has(uuid1, uuid2);
    }
}