package com.thegates.angma;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Saver extends PersistentState {



    // Default constructor, used for getting the persistentState.
    public Saver() {}



    // Constructor to immediately create the data from nbt.
    public Saver(@NotNull NbtCompound nbt){
        this.angerEntities = createFromNbt(nbt);
    }



    public Map<UUID, Set<Identifier>> getList(){
        return angerEntities;
    }



    // Information to be saved into nbt and back.
    private Map<UUID, Set<Identifier>> angerEntities = new HashMap<>();



    // WriteNbt is called when a PersistentState is marked dirty, and the game saves.
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        System.out.println("[Angma]: writeNbt called!");
        angerEntities.keySet().forEach(uuid -> {
            NbtList mobList = new NbtList();
            angerEntities.get(uuid).forEach(identifier -> {
                if (identifier == null){return;}
                mobList.add(NbtString.of(identifier.toString()));
            });
            nbt.put(uuid.toString(), mobList);
        });

        return nbt;
    }



    // Create the information from the loaded nbt.
    private Map<UUID, Set<Identifier>> createFromNbt(@NotNull NbtCompound nbt){
        System.out.println("[Angma]: createFromNbt called!");
        Map<UUID, Set<Identifier>> players = new HashMap<>();

        // For each uuid, create a set of mobs,
        nbt.getKeys().forEach(uuid -> {
            System.out.println("For uuid: "+ uuid);
            Set<Identifier> mobs = new HashSet<>();

            NbtList mobList = (NbtList) nbt.get(uuid);
            if (mobList == null){
                System.out.println("MobList is null");
                return;
            }

            mobList.forEach(mob -> {
                Identifier id = Identifier.tryParse(mob.asString());
                if (id == null) {return;}
                System.out.println("For Identifier: "+ id);
                mobs.add(id);
            });

            players.put(UUID.fromString(uuid), mobs);
        });
        return players;
    }



    // Adds a mob to the player's list, creating the player if necessary.
    public boolean addMob(UUID entityUUID, Identifier mobId){
        // If the player does not exist in the map, create it.
        if (!angerEntities.containsKey(entityUUID)){
            angerEntities.put(entityUUID, new HashSet<>());
        }
        // Already exists
        if (angerEntities.get(entityUUID).contains(mobId)){
            return false;
        }

        // Add mob to list.
        angerEntities.get(entityUUID).add(mobId);
        markDirty();

        return true;

    }



    public void removeMob(UUID playerUUID, Identifier mobName){
        // If the player does not exist in the map, create it.
        if (!angerEntities.containsKey(playerUUID)){return;}
        if (!angerEntities.get(playerUUID).contains(mobName)){return;}
        // Remove mob from list.
        angerEntities.get(playerUUID).remove(mobName);

        if (angerEntities.get(playerUUID).size() == 0){
            angerEntities.remove(playerUUID);
        }
        markDirty();

    }



    // Deletes all entries, for testing purposes.
    private void clearAll(){
        angerEntities.clear();
        markDirty();
    }




    public static boolean hasAngerDisabled(@NotNull LivingEntity entity, Identifier mobId){
        UUID uuid = entity.getUuid();


        Map<UUID, Set<Identifier>> angerPlayers = ((ServerWorld)entity.getEntityWorld()).getPersistentStateManager().getOrCreate(Saver::new, Saver::new, Main.MOD_ID).getList();
        return uuid != null && angerPlayers.get(uuid) != null && angerPlayers.get(uuid).contains(mobId);
    }

}