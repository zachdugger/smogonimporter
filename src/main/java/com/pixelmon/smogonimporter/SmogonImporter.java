package com.pixelmon.smogonimporter;

import com.pixelmon.smogonimporter.api.SmogonAPI;
import com.pixelmon.smogonimporter.config.SmogonConfig;
import com.pixelmon.smogonimporter.data.DataManager;
import com.pixelmon.smogonimporter.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SmogonImporter.MOD_ID)
public class SmogonImporter {
    public static final String MOD_ID = "smogonimporter";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static SmogonImporter instance;
    private DataManager dataManager;
    private SmogonAPI api;

    public SmogonImporter(IEventBus modBus, ModContainer container) {
        instance = this;

        // Register config
        container.registerConfig(ModConfig.Type.COMMON, SmogonConfig.SPEC);

        // Register mod event listeners
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::loadComplete);

        // Register forge event listeners
        NeoForge.EVENT_BUS.addListener(this::serverStarting);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);

        LOGGER.info("Smogon Importer initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Setting up Smogon Importer...");

        // Initialize network handler
        NetworkHandler.init();

        // Initialize data manager
        dataManager = new DataManager();

        // Initialize API
        api = new SmogonAPI(dataManager);
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Smogon Importer load complete!");

            // Schedule initial data fetch
            if (SmogonConfig.AUTO_UPDATE.get()) {
                dataManager.scheduleDataUpdate();
            }
        });
    }

    private void serverStarting(final ServerStartingEvent event) {
        LOGGER.info("Server starting, initializing Smogon data...");

        // Load cached data or fetch fresh data
        dataManager.initialize();
    }

    private void serverStopped(final ServerStoppedEvent event) {
        LOGGER.info("Server stopping, saving Smogon data...");

        // Save current data to cache
        dataManager.saveCache();
    }

    public static SmogonImporter getInstance() {
        return instance;
    }

    public static SmogonAPI getAPI() {
        if (instance == null || instance.api == null) {
            throw new IllegalStateException("Smogon Importer API not initialized!");
        }
        return instance.api;
    }

    public static DataManager getDataManager() {
        if (instance == null || instance.dataManager == null) {
            throw new IllegalStateException("Smogon Importer DataManager not initialized!");
        }
        return instance.dataManager;
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
