package net.telluriummc.telluriumchatbridge;

import com.oroarmor.util.config.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TelluriumChatBridge implements ModInitializer {
    public static TelluriumDiscordBot discordBot;

    public static String botToken;
    public static String serverName;
    public static String[] channelIds;


    public static final String channelId = "871685696895737887";

    // todo: [Tellurium Chat Bridge] const
    @Override
    public void onInitialize() {
        // handle all config in a separate method
        InitializeConfig();

        // initialize discord bot after server initialization
        ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> {
            discordBot = new TelluriumDiscordBot(botToken, mcServer, channelIds);
        });

        System.out.println("[Tellurium Chat Bridge] Initialized.");
    }

    private void InitializeConfig() {
        //  Initialize configs
        File configFile = new File(FabricLoader.getInstance().getConfigDir() + "/tellurium-chat-bridge.json");
        List<ConfigItem<?>> configItems = new ArrayList<>();

        // add config items (token, server name, channel ids)
        configItems.add(new StringConfigItem("discord_bot_token", "Put discord bot token here", "bot token"));
        configItems.add(new StringConfigItem("server_name", "SMP", "The minecraft server's unique name (ie. SMP, CMP, SMP Copy)"));
        configItems.add(new ArrayConfigItem<>("discord_channel_ids", new String[] {"000000000000000000"}, "chat bridge channel id"));

        // add all items under a config group
        List<ConfigItemGroup> configGroups = new ArrayList<>();
        configGroups.add(new ConfigItemGroup(configItems, "tellurium_chat_bridge_config"));

        Config config = new Config(configGroups, configFile, "tellurium-chat-bridge");

        // check if config file already exists
        if (!configFile.exists()) {
            System.out.println("[Tellurium Chat Bridge] Config file not found. Generating a new config... Please reload the server after editing the config.");
            // generate config
            config.saveConfigToFile();
        }

        // read config and load variables from the changed config
        config.readConfigFromFile();
        configGroups = config.getConfigs();
        configItems = configGroups.get(0).getConfigs();

        try {
            botToken = (String)configItems.get(0).getValue();
            serverName = (String)configItems.get(1).getValue();
            channelIds = (String[])configItems.get(2).getValue();
        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("[Tellurium Chat Bridge] Error while reading the config file.");
        }
    }
}