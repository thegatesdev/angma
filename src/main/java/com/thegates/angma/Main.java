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

    private static AngerRegister angerRegister;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new AngmaCommand().register(dispatcher, dedicated));
    }

    private void serverStarted(MinecraftServer server) {
        ServerWorld world = server.getWorld(World.OVERWORLD);
        assert world != null;
        angerRegister = world.getPersistentStateManager().getOrCreate(AngerRegister::new, AngerRegister::new, MOD_ID);
    }

    public static AngerRegister getAngerRegister() {
        return angerRegister;
    }
}
