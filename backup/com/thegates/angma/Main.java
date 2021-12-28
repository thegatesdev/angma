package com.thegates.angma;

import com.thegates.angma.command.AngmaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class Main implements ModInitializer {

    public static String MOD_ID = "tg_angma";


    @Override
    public void onInitialize() {
        System.out.println("[Angma]: onInit!");
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        CommandRegistrationCallback.EVENT.register(AngmaCommand::register);
    }

    private void serverStarted(MinecraftServer server){
        System.out.println("[Angma]: serverStarted called!");
        Saver saver = server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(Saver::new, Saver::new, MOD_ID);
    }
}
