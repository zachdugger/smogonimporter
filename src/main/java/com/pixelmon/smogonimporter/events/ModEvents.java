package com.pixelmon.smogonimporter.events;

import com.pixelmon.smogonimporter.SmogonImporter;
import com.pixelmon.smogonimporter.commands.SmogonCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = SmogonImporter.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SmogonCommands.register(event.getDispatcher());
        SmogonImporter.LOGGER.debug("Registered Smogon commands");
    }
}