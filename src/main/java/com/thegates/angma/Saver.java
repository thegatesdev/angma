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

public class Saver extends PersistentState {

    // Information to be saved into nbt and back.
    private static Map<UUID, Set<Identifier>> entityDisabled = new HashMap<>();
    private static Map<Identifier, Set<Identifier>> globalDisabled = new HashMap<>();

    // Default constructor, used for getting the persistentState.
    public Saver() {}

    // Constructor to immediately create the data from nbt.
    public Saver(@NotNull NbtCompound nbt){
        createFromNbt(nbt);
    }

    // Create the information from the loaded nbt.
    private void createFromNbt(@NotNull NbtCompound nbt){

        NbtCompound playerDisabledInput = nbt.getCompound("playerDisabled");
        NbtCompound typeDisabledInput = nbt.getCompound("typeDisabled");

        Map<UUID, Set<Identifier>> playerDisabledOutput = new HashMap<>();
        Map<Identifier, Set<Identifier>> typeDisabledOutput = new HashMap<>();

        playerDisabledInput.getKeys().forEach(uuid -> {
            NbtList nbtList = (NbtList) playerDisabledInput.get(uuid);
            if (nbtList == null){return;}
            Set<Identifier> identifiers = new HashSet<>();
            nbtList.forEach(nbtElement -> identifiers.add(Identifier.tryParse(nbtElement.asString())));
            playerDisabledOutput.put(UUID.fromString(uuid), identifiers);
        });

        typeDisabledInput.getKeys().forEach(identifier -> {
            NbtList nbtList = (NbtList) typeDisabledInput.get(identifier);
            if (nbtList == null){return;}
            Set<Identifier> identifiers = new HashSet<>();
            nbtList.forEach(nbtElement -> identifiers.add(Identifier.tryParse(nbtElement.asString())));
            typeDisabledOutput.put(Identifier.tryParse(identifier), identifiers);
        });

        entityDisabled = playerDisabledOutput;
        globalDisabled = typeDisabledOutput;
    }

    // WriteNbt is called when a PersistentState is marked dirty, and the game saves.
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        // Create playerDisabled nbt data.
        NbtCompound playerDisabledNbt = new NbtCompound();
        entityDisabled.keySet().forEach(uuid -> {
            Set<Identifier> identifiers = entityDisabled.get(uuid);
            NbtList identifiersNbt = new NbtList();
            identifiers.forEach(identifier -> identifiersNbt.add(NbtString.of(identifier.toString())));
            playerDisabledNbt.put(uuid.toString(), identifiersNbt);
        });

        // Create typeDisabled nbt data
        NbtCompound typeDisabledNbt = new NbtCompound();
        globalDisabled.keySet().forEach(identifier -> {
            Set<Identifier> identifiers = globalDisabled.get(identifier);
            NbtList identifiersNbt = new NbtList();
            identifiers.forEach(identifier1 -> identifiersNbt.add(NbtString.of(identifier1.toString())));
            typeDisabledNbt.put(identifier.toString(), identifiersNbt);
        });

        nbt.put("playerDisabled", playerDisabledNbt);
        nbt.put("typeDisabled", typeDisabledNbt);

        return nbt;
    }


    public Set<Identifier> getDisabledTypes(UUID uuid){
        Set<Identifier> dTypes = new HashSet<>();
        for (Identifier typeOrTag : entityDisabled.get(uuid)){
            if (EntityType.get(typeOrTag.toString()).isPresent()){
                dTypes.add(typeOrTag);
            }
        }
        if (dTypes.size() == 0){
            return null;
        }
        return dTypes;
    }

    public Set<Identifier> getDisabledTags(UUID uuid){
        Set<Identifier> dTags = new HashSet<>(entityDisabled.get(uuid));
        dTags.removeAll(getDisabledTypes(uuid));
        if (dTags.size() == 0){
            return null;
        }
        return dTags;
    }


    // Adds a mob to the player's list, creating the player if necessary.
    public boolean addMobType(UUID entityUUID, Identifier mobId){
        // If the player does not exist in the map, create it.
        if (!entityDisabled.containsKey(entityUUID)){
            entityDisabled.put(entityUUID, new HashSet<>());
        }else if (entityDisabled.get(entityUUID).contains(mobId)){
            return false;
            // Already exists
        }

        // Add mob to list.
        entityDisabled.get(entityUUID).add(mobId);
        // Make sure it gets saved.
        markDirty();

        return true;
    }

    public void removeMobType(UUID entityUUID, Identifier mobId){
        // If the player does not exist in the map, create it.
        if (!entityDisabled.containsKey(entityUUID)){return;}

        if (!entityDisabled.get(entityUUID).contains(mobId)){return;}
        // Remove mob from list.
        entityDisabled.get(entityUUID).remove(mobId);

        if (entityDisabled.get(entityUUID).size() == 0){
            entityDisabled.remove(entityUUID);
        }
        markDirty();
    }

    public void addTag(UUID entityUUID, Tag<EntityType<?>> tag){
        Identifier tagId = EntityTypeTags.getTagGroup().getUncheckedTagId(tag);
        if (!entityDisabled.containsKey(entityUUID)){
            entityDisabled.put(entityUUID, new HashSet<>());
        }

        // Already exists
        if (entityDisabled.get(entityUUID).contains(tagId)){
            return;
        }

        entityDisabled.get(entityUUID).add(tagId);

        markDirty();
    }

    public void removeTag(UUID entityUUID, Tag<EntityType<?>> tag){
        Identifier tagId = EntityTypeTags.getTagGroup().getUncheckedTagId(tag);

        if (!entityDisabled.containsKey(entityUUID)){return;}
        if (!entityDisabled.get(entityUUID).contains(tagId)){return;}

        entityDisabled.get(entityUUID).remove(tagId);

        if (entityDisabled.get(entityUUID).size() == 0){
            entityDisabled.remove(entityUUID);
        }

        markDirty();
    }

    public void addGlobalMobType(Identifier key, Identifier toDisable){
        if (!globalDisabled.containsKey(key)){
            globalDisabled.put(key, new HashSet<>());
        }else if (globalDisabled.get(key).contains(toDisable)){
            return;
        }

        globalDisabled.get(key).add(toDisable);

        markDirty();
    }

    public void removeGlobalMobType(Identifier key, Identifier toEnable){
        if (!globalDisabled.containsKey(key)){return;}

        if (!globalDisabled.get(key).contains(toEnable)){return;}

        globalDisabled.get(key).remove(toEnable);

        if (globalDisabled.get(key).size() == 0){
            globalDisabled.remove(key);
        }
        markDirty();
    }

    public static boolean angerDisabled(Entity target, Entity targetter){
        return hasAngerDisabled(target, targetter) || isAngerDisabled(EntityType.getId(target.getType()), targetter.getType());
    }


    private static boolean hasAngerDisabled(Entity target, @NotNull Entity targeter){
        return hasAngerTypeDisabled(target, EntityType.getId(targeter.getType())) || hasAngerTagDisabled(target, targeter.getType());
    }

    private static boolean hasAngerTypeDisabled(@NotNull Entity target, Identifier identifier){
        if (entityDisabled.get(target.getUuid()) == null) {return false;}
        return entityDisabled.get(target.getUuid()).contains(identifier);
    }

    private static boolean hasAngerTagDisabled(Entity target, EntityType<?> type){
        if (entityDisabled.get(target.getUuid()) == null) {return false;}
        Collection<Identifier> tags = EntityTypeTags.getTagGroup().getTagsFor(type);
        for (Identifier tagId : tags){
            if (entityDisabled.get(target.getUuid()).contains(tagId)){
                return true;
            }
        }
        return false;
    }


    private static boolean isAngerDisabled(Identifier targetType, EntityType<?> targetterType){
        return isAngerTypeDisabled(targetType, EntityType.getId(targetterType)) || isAngerTagDisabled(targetType, targetterType);
    }

    private static boolean isAngerTypeDisabled(Identifier targetType, Identifier type){
        if (globalDisabled.get(targetType) == null) {return false;}
        return globalDisabled.get(targetType).contains(type);
    }

    private static boolean isAngerTagDisabled(Identifier targetType, EntityType<?> targetterType){
        if (globalDisabled.get(targetType) == null) {return false;}
        for (Identifier tagId : EntityTypeTags.getTagGroup().getTagsFor(targetterType)){
            if (globalDisabled.get(targetType).contains(tagId)){
                return true;
            }
        }
        return false;
    }

}