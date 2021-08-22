package net.telluriummc.telluriumchatbridge;

import com.oroarmor.util.config.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TelluriumChatBridge implements ModInitializer {
    public static String ConsolePrefix = "[Tellurium Chat Bridge] ";
    public static TelluriumDiscordBot discordBot;

    public static String botToken;
    public static String serverName;
    public static String[] channelIds;
    public static String[] webhookUrls;
    public static String discordToMinecraftFormatting;
    public static String botPrefix;

    @Override
    public void onInitialize() {
        // handle all config in a separate method
        InitializeConfig();

        // initialize discord bot after server initialization
        ServerLifecycleEvents.SERVER_STARTED.register(mcServer -> discordBot = new TelluriumDiscordBot(botToken, mcServer, channelIds));
        ServerLifecycleEvents.SERVER_STOPPED.register(mcServer -> discordBot.handleMinecraftMessage("Server stopped!", ""));
        ServerPlayConnectionEvents.JOIN.register(TelluriumChatBridge::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(TelluriumChatBridge::onPlayerDisconnect);

        System.out.println(ConsolePrefix + "Initialized.");
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler networkHandler, PacketSender packetSender, MinecraftServer server) {
        ServerPlayerEntity player = networkHandler.getPlayer();
        String formattedName = player.getName().getString().replace("_", "\\_");
        discordBot.handleMinecraftMessage(formattedName + " joined the server!", player.getUuidAsString());
    }

    private static void onPlayerDisconnect(ServerPlayNetworkHandler networkHandler, MinecraftServer server) {
        ServerPlayerEntity player = networkHandler.getPlayer();
        String formattedName = player.getName().getString().replace("_", "\\_");
        discordBot.handleMinecraftMessage(formattedName + " left the server!", player.getUuidAsString());
    }

    // https://discord.com/api/webhooks/877690625481392179/FQO9otfxTI6Ml7mMY6dt-5IpacF4KhYnvz-EgmcwfFm7G1_PNg8cvwAFOjD4QaVC_cdY
    // https://discord.com/api/webhooks/878139901219049512/35VYEU_NjepvJ6WOXY9FAHZ1l7bWOUkxelyQieLPKUPtjjzXm4dwf7AdsNv45HIpPA2A
    private void InitializeConfig() {
        //  Initialize configs
        String configFilename = "/tellurium-chat-bridge.json";
        File configFile = new File(FabricLoader.getInstance().getConfigDir() + configFilename);
        List<ConfigItem<?>> configItems = new ArrayList<>();

        // add config items (token, server name, channel ids)
        configItems.add(new StringConfigItem("discord_bot_token", "Put discord bot token here", "discord_bot_token"));
        configItems.add(new StringConfigItem("server_name", "SMP", "server_name"));
        configItems.add(new ArrayConfigItem<>("discord_channel_ids", new String[] {"000000000000000000"}, "discord_channel_ids"));
        configItems.add(new ArrayConfigItem<>("webhook_urls", new String[] {"https://discord.com/api/webhooks/000000000000000000/R4NdoMg4r8le" }, "webhook_urls"));
        configItems.add(new StringConfigItem("discord_to_minecraft_formatting", "{player_name} §9§lD §r§8»§r {attachment} {text}", "discord_to_minecraft_formatting"));
        configItems.add(new StringConfigItem("bot_prefix", "!", "bot_prefix"));

        // add all items under a config group
        List<ConfigItemGroup> configGroups = new ArrayList<>();
        configGroups.add(new ConfigItemGroup(configItems, "tellurium_chat_bridge_config"));

        Config config = new Config(configGroups, configFile, "tellurium-chat-bridge");

        // check if config file already exists
        if (!configFile.exists()) {
            System.out.println(ConsolePrefix + "Config file not found. Generating a new config... Please reload the server after editing the config.");
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
            webhookUrls = (String[])configItems.get(3).getValue();
            discordToMinecraftFormatting = (String)configItems.get(4).getValue();
            botPrefix = (String)configItems.get(5).getValue();
        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("[Tellurium Chat Bridge] Error while reading the config file.");
        }
    }
}