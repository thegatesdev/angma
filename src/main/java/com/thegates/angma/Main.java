package com.thegates.angma;

import com.thegates.angma.command.AngmaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class Main implements ModInitializer {

    public static String MOD_ID = "tg_angma";

    public static String MOD_PREFIX = "[Angma]: ";

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        CommandRegistrationCallback.EVENT.register(AngmaCommand::register);
    }

    private void serverStarted(MinecraftServer server){
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world != null) {
            world.getPersistentStateManager().getOrCreate(Saver::new, Saver::new, MOD_ID);
        }else {
            throw new RuntimeException(MOD_PREFIX+"Could not get the overworld, is it disabled?");
        }
    }
}
