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

public class AngerRegister extends PersistentState {

    // Information to be saved into nbt and back.
    private final DisabledContainer<UUID, Identifier> entityDisabled = new DisabledContainer<>();
    private final DisabledContainer<Identifier, Identifier> globalDisabled = new DisabledContainer<>();
    private final DisabledContainer<UUID, UUID> specificDisabled = new DisabledContainer<>();


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
        specificDisabled.clear();
        NbtCompound entityDisabledInput = nbt.getCompound("playerDisabled");
        NbtCompound globalDisabledInput = nbt.getCompound("typeDisabled");
        NbtCompound specificDisabledInput = nbt.getCompound("specificDisabled");

        entityDisabledInput.getKeys().forEach(uuidString -> {
            NbtList nbtList = (NbtList) entityDisabledInput.get(uuidString);
            if (nbtList == null) return;
            UUID uuid = UUID.fromString(uuidString);
            nbtList.forEach(nbtElement -> entityDisabled.putOverHead(uuid, Identifier.tryParse(nbtElement.asString()), nbtList.size()));
        });

        globalDisabledInput.getKeys().forEach(identifierString -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(identifierString);
            if (nbtList == null) return;
            Identifier identifier = Identifier.tryParse(identifierString);
            nbtList.forEach(nbtElement -> globalDisabled.putOverHead(identifier, Identifier.tryParse(nbtElement.asString()), nbtList.size()));
        });

        specificDisabledInput.getKeys().forEach(key -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(key);
            if (nbtList == null) return;
            UUID uuid = UUID.fromString(key);
            nbtList.forEach(nbtElement -> specificDisabled.putOverHead(uuid, UUID.fromString(nbtElement.asString()), nbtList.size()));
        });
    }


    // WriteNbt is called when a PersistentState is marked dirty, and the game saves.
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("playerDisabled", entityDisabled.populateNbt(UUID::toString, Identifier::toString));
        nbt.put("typeDisabled", globalDisabled.populateNbt(Identifier::toString, Identifier::toString));
        nbt.put("specificDisabled", specificDisabled.populateNbt(UUID::toString, UUID::toString));
        return nbt;
    }


    public Set<Identifier> getDisabledTypes(UUID uuid) {
        Set<Identifier> dTypes = new HashSet<>();
        Set<Identifier> all = entityDisabled.get(uuid);
        if (all == null) {
            return dTypes;
        }
        for (Identifier typeOrTag : entityDisabled.get(uuid)) {
            if (EntityType.get(typeOrTag.toString()).isPresent()) {
                dTypes.add(typeOrTag);
            }
        }
        return dTypes;
    }


    public Set<Identifier> getDisabledTags(UUID uuid) {
        Set<Identifier> all = entityDisabled.get(uuid);
        if (all == null) {
            return new HashSet<>();
        }
        Set<Identifier> dTags = new HashSet<>(all);
        dTags.removeAll(getDisabledTypes(uuid));
        return dTags;
    }


    public Map<Identifier, Set<Identifier>> getGlobalDisabled() {
        return Collections.unmodifiableMap(globalDisabled);
    }


    // Adds a mob to the player's list, creating the player if necessary.
    public boolean addMobType(UUID entityUUID, Identifier mobId) {
        // If the player does not exist in the map, create it.
        if (!entityDisabled.containsKey(entityUUID)) {
            entityDisabled.put(entityUUID, new HashSet<>());
        } else if (entityDisabled.get(entityUUID).contains(mobId)) {
            return false;
            // Already exists
        }

        // Add mob to list.
        entityDisabled.get(entityUUID).add(mobId);
        // Make sure it gets saved.
        markDirty();

        return true;
    }


    public void removeMobType(UUID entityUUID, Identifier mobId) {
        // If the player does not exist in the map, create it.
        if (!entityDisabled.containsKey(entityUUID)) {
            return;
        }

        if (!entityDisabled.get(entityUUID).contains(mobId)) {
            return;
        }
        // Remove mob from list.
        entityDisabled.get(entityUUID).remove(mobId);

        if (entityDisabled.get(entityUUID).size() == 0) {
            entityDisabled.remove(entityUUID);
        }
        markDirty();
    }


    public void addTag(UUID entityUUID, Identifier tagId) {
        if (!entityDisabled.containsKey(entityUUID)) {
            entityDisabled.put(entityUUID, new HashSet<>());
        }

        // Already exists
        if (entityDisabled.get(entityUUID).contains(tagId)) {
            return;
        }

        entityDisabled.get(entityUUID).add(tagId);

        markDirty();
    }


    public void removeTag(UUID entityUUID, Identifier tagId) {
        if (!entityDisabled.containsKey(entityUUID)) {
            return;
        }
        if (!entityDisabled.get(entityUUID).contains(tagId)) {
            return;
        }

        entityDisabled.get(entityUUID).remove(tagId);

        if (entityDisabled.get(entityUUID).size() <= 0) {
            entityDisabled.remove(entityUUID);
        }

        markDirty();
    }


    public void addSpecific(UUID entity, UUID targeter) {
        if (!specificDisabled.containsKey(entity)) {
            specificDisabled.put(entity, new HashSet<>());
        }
        specificDisabled.get(entity).add(targeter);
    }


    public void removeSpecific(UUID entity, UUID targeter) {
        if (!specificDisabled.containsKey(entity)) {
            return;
        }
        specificDisabled.get(entity).remove(targeter);
        if (specificDisabled.get(entity).size() <= 0) {
            specificDisabled.remove(entity);
        }
    }


    public void addGlobalMobType(Identifier key, Identifier toDisable) {
        if (!globalDisabled.containsKey(key)) {
            globalDisabled.put(key, new HashSet<>());
        } else if (globalDisabled.get(key).contains(toDisable)) {
            return;
        }

        globalDisabled.get(key).add(toDisable);

        markDirty();
    }


    public void removeGlobalMobType(Identifier key, Identifier toEnable) {
        if (!globalDisabled.containsKey(key)) {
            return;
        }

        if (!globalDisabled.get(key).contains(toEnable)) {
            return;
        }

        globalDisabled.get(key).remove(toEnable);

        if (globalDisabled.get(key).size() == 0) {
            globalDisabled.remove(key);
        }
        markDirty();
    }


    public boolean angerDisabled(Entity target, Entity targetter) {
        return hasAngerDisabled(target, targetter) || isAngerDisabled(EntityType.getId(target.getType()), targetter.getType()) || specificDisabled(target.getUuid(), targetter.getUuid());
    }


    private boolean specificDisabled(UUID target, UUID targeter) {
        if (!specificDisabled.containsKey(target)) {
            return false;
        }
        return specificDisabled.get(target).contains(targeter);
    }


    private boolean hasAngerDisabled(Entity target, @NotNull Entity targeter) {
        return hasAngerTypeDisabled(target, EntityType.getId(targeter.getType())) || hasAngerTagDisabled(target, targeter.getType());
    }


    private boolean hasAngerTypeDisabled(@NotNull Entity target, Identifier identifier) {
        if (!entityDisabled.containsKey(target.getUuid())) {
            return false;
        }
        return entityDisabled.get(target.getUuid()).contains(identifier);
    }


    private boolean hasAngerTagDisabled(Entity target, EntityType<?> type) {
        if (!entityDisabled.containsKey(target.getUuid())) {
            return false;
        }
        return getTagsFor(type).stream().map(TagKey::id).anyMatch(new HashSet<>(entityDisabled.get(target.getUuid()))::contains);
    }


    private boolean isAngerDisabled(Identifier targetType, EntityType<?> targetterType) {
        return isAngerTypeDisabled(targetType, EntityType.getId(targetterType)) || isAngerTagDisabled(targetType, targetterType);
    }


    private boolean isAngerTypeDisabled(Identifier targetType, Identifier type) {
        if (!globalDisabled.containsKey(targetType)) {
            return false;
        }
        return globalDisabled.get(targetType).contains(type);
    }


    private boolean isAngerTagDisabled(Identifier targetTag, EntityType<?> targetterType) {
        if (!globalDisabled.containsKey(targetTag)) {
            return false;
        }
        return getTagsFor(targetterType).stream().map(TagKey::id).anyMatch(new HashSet<>(globalDisabled.get(targetTag))::contains);
    }


    public static List<TagKey<EntityType<?>>> getTagsFor(EntityType<?> entityType) {
        Optional<RegistryKey<EntityType<?>>> key = Registry.ENTITY_TYPE.getKey(entityType);
        if (key.isEmpty() || !Registry.ENTITY_TYPE.contains(key.get())) {
            return Collections.emptyList();
        }
        Optional<RegistryEntry<EntityType<?>>> entry = Registry.ENTITY_TYPE.getEntry(key.get());
        if (entry.isEmpty()) {
            return Collections.emptyList();
        }
        return entry.get().streamTags().toList();
    }
}