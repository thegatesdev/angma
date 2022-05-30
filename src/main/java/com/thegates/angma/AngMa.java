package com.thegates.angma;

import com.thegates.angma.command.AngmaCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public class AngMa implements ModInitializer {

    public static String MOD_ID = "tg_angma";

    public static String MOD_PREFIX = "[Angma]: ";
    public static boolean commandFailed = false;

    private static AngerRegister angerRegister;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        try {
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new AngmaCommand().register(dispatcher, dedicated));
        } catch (Exception e) {
            commandFailed = true;
        }
    }

    private void serverStarted(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        angerRegister = world.getPersistentStateManager().getOrCreate(AngerRegister::new, AngerRegister::new, MOD_ID);
        if (commandFailed) System.out.println(MOD_PREFIX + "Failed to register command!");
    }

    public static AngerRegister getAngerRegister() {
        return angerRegister;
    }
}
