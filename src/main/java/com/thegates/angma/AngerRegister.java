package com.thegates.angma;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AngerRegister extends PersistentState {

    // Information to be saved into nbt and back.
    private Map<UUID, Set<Identifier>> entityDisabled = new HashMap<>();
    private Map<Identifier, Set<Identifier>> globalDisabled = new HashMap<>();
    private Map<UUID, Set<UUID>> specificDisabled = new HashMap<>();


    // Default constructor, used for getting the persistentState.
    public AngerRegister() {
    }


    // Constructor to immediately create the data from nbt.
    public AngerRegister(@NotNull NbtCompound nbt) {
        createFromNbt(nbt);
    }


    // Create the information from the loaded nbt.
    private void createFromNbt(@NotNull NbtCompound nbt) {

        NbtCompound entityDisabledInput = nbt.getCompound("playerDisabled");
        NbtCompound globalDisabledInput = nbt.getCompound("typeDisabled");
        NbtCompound specificDisabledInput = nbt.getCompound("specificDisabled");

        Map<UUID, Set<Identifier>> entityDisabledOutput = new HashMap<>();
        Map<Identifier, Set<Identifier>> globalDisabledOutput = new HashMap<>();
        Map<UUID, Set<UUID>> specificDisabledOutput = new HashMap<>();

        entityDisabledInput.getKeys().forEach(uuid -> {
            NbtList nbtList = (NbtList) entityDisabledInput.get(uuid);
            if (nbtList == null) {
                return;
            }
            Set<Identifier> identifiers = new HashSet<>();
            nbtList.forEach(nbtElement -> identifiers.add(Identifier.tryParse(nbtElement.asString())));
            entityDisabledOutput.put(UUID.fromString(uuid), identifiers);
        });

        globalDisabledInput.getKeys().forEach(identifier -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(identifier);
            if (nbtList == null) {
                return;
            }
            Set<Identifier> identifiers = new HashSet<>();
            nbtList.forEach(nbtElement -> identifiers.add(Identifier.tryParse(nbtElement.asString())));
            globalDisabledOutput.put(Identifier.tryParse(identifier), identifiers);
        });

        specificDisabledInput.getKeys().forEach(key -> {
            NbtList nbtList = (NbtList) globalDisabledInput.get(key);
            if (nbtList == null) {
                return;
            }
            Set<UUID> uuids = new HashSet<>();
            nbtList.forEach(nbtElement -> uuids.add(UUID.fromString(nbtElement.asString())));
            specificDisabledOutput.put(UUID.fromString(key), uuids);
        });

        entityDisabled = entityDisabledOutput;
        globalDisabled = globalDisabledOutput;
        specificDisabled = specificDisabledOutput;
    }


    // WriteNbt is called when a PersistentState is marked dirty, and the game saves.
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        // Create playerDisabled nbt data.
        NbtCompound playerDisabledNbt = new NbtCompound();
        entityDisabled.keySet().forEach(uuid -> {
            NbtList identifiersNbt = new NbtList();
            entityDisabled.get(uuid).forEach(identifier -> identifiersNbt.add(NbtString.of(identifier.toString())));
            playerDisabledNbt.put(uuid.toString(), identifiersNbt);
        });

        // Create globalDisabled nbt data
        NbtCompound globalDisabledNbt = new NbtCompound();
        globalDisabled.keySet().forEach(identifier -> {
            NbtList identifiersNbt = new NbtList();
            globalDisabled.get(identifier).forEach(identifier1 -> identifiersNbt.add(NbtString.of(identifier1.toString())));
            globalDisabledNbt.put(identifier.toString(), identifiersNbt);
        });

        NbtCompound specificDisabledNbt = new NbtCompound();
        specificDisabled.keySet().forEach(uuid -> {
            NbtList uuidList = new NbtList();
            specificDisabled.get(uuid).forEach(uuid1 -> uuidList.add(NbtString.of(uuid1.toString())));
            specificDisabledNbt.put(uuid.toString(), uuidList);
        });


        nbt.put("playerDisabled", playerDisabledNbt);
        nbt.put("typeDisabled", globalDisabledNbt);
        nbt.put("specificDisabled", specificDisabledNbt);

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
        return new HashMap<>(globalDisabled);   // TODO is this necessary?
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


    public void addTag(UUID entityUUID, Tag<EntityType<?>> tag) {
        Identifier tagId = EntityTypeTags.getTagGroup().getUncheckedTagId(tag);
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


    public void removeTag(UUID entityUUID, Tag<EntityType<?>> tag) {
        Identifier tagId = EntityTypeTags.getTagGroup().getUncheckedTagId(tag);

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
        return EntityTypeTags.getTagGroup().getTagsFor(type).stream().anyMatch(new HashSet<>(entityDisabled.get(target.getUuid()))::contains);
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
        return EntityTypeTags.getTagGroup().getTagsFor(targetterType).stream().anyMatch(new HashSet<>(globalDisabled.get(targetTag))::contains);
    }
}