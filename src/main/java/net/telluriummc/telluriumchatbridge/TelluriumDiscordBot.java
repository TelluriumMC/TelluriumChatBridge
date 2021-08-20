package net.telluriummc.telluriumchatbridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.util.Formatting;
import net.telluriummc.telluriumchatbridge.utils.JsonReader;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.mineskin.MineskinClient;
import org.mineskin.SkinOptions;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelluriumDiscordBot
{
    private JDA bot;
    private List<TextChannel> bridgeChannels;
    private final MinecraftServer mcServer;
    private static final int DISCORD_MAX_CHARACTER_LIMIT = 2000;

    public TelluriumDiscordBot(String token, MinecraftServer server, String[] channelIds) {
        try {
            bot = JDABuilder.createDefault(token).build();
        } catch (LoginException error) {
            error.printStackTrace();
        }

        bot.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                handleDiscordMessage(event);
            }
        });
        
        mcServer = server;
        
        new Thread(() -> {
            try {
                bot.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            bridgeChannels = new ArrayList<>();
            // loop through every channel id and add the channels to the bridgeChannels list
            for (String channelId : channelIds) {
                TextChannel channel = bot.getTextChannelById(channelId);
                String name = channel != null ? channel.getName() : "channel does not exist";
                // add the channel and print this info to console
                bridgeChannels.add(channel);
                System.out.println("[Tellurium Chat Bridge] Initialized Discord channel with the name of " + name  + " and the id of " + channel.getId());
            }
        }).start();
    }

    public void handleDiscordMessage(MessageReceivedEvent event) {
        // Handles all messages sent in any channel that the bot has permissions for, in any server.
        String messageContent = event.getMessage().getContentRaw();
        String botPrefix = "!"; // todo: config

        // todo: commands
        if (messageContent.startsWith(botPrefix)) {
            switch (messageContent.substring(botPrefix.length())) {
                case "ping":
                    event.getChannel().sendMessage("Pong!").queue();
                    break;
            }
        }

        // loop through every text channel
        for (TextChannel bridgeChannel : bridgeChannels) {

            // check if the channel the message was sent doesn't match our text channel
            if (event.getChannel().getId().equals(bridgeChannel.getId())) {
                String messagePrefix = "[Discord]"; // todo: config

                // Checks if the message isn't sent by the bot, or if it is, it only runs if the message doesn't start with [Discord]
                if (!event.getMessage().getAuthor().isBot() || !(messageContent.startsWith(messagePrefix) && event.getAuthor() == bot.getSelfUser())) {
                    // Handling discord messages to minecraft | todo: config
                    String filler = " " + Formatting.RESET + Formatting.DARK_GRAY + "»" + Formatting.RESET + " ";
                    String nameSuffix = " " + Formatting.BLUE + Formatting.BOLD + "D";

                    // Sends a message that looks something like GoodPro712 D » This is a message from Discord!
                    mcServer.getCommandManager().execute(mcServer.getCommandSource(), formatAsTellraw(event.getAuthor().getName() + nameSuffix + filler + messageContent));
                }
            }
        }
    }

    public void handleMinecraftMessage(String rawMessage, String uuid) {
        // create webhook builder todo: webhook url from config
        WebhookClient client = WebhookClient.withUrl("https://discord.com/api/webhooks/877690625481392179/FQO9otfxTI6Ml7mMY6dt-5IpacF4KhYnvz-EgmcwfFm7G1_PNg8cvwAFOjD4QaVC_cdY");
        WebhookMessageBuilder builder = new WebhookMessageBuilder();

        // add the minecraft message to the webhook builder
        builder.append(rawMessage);

        // set the webhook username to minecraft IGN
        try {
            // set the username with our helper method
            builder.setUsername(usernameFromUUID(uuid));
        } catch (IOException error) {
            // notify something broke through username, message content and logs respectively
            builder.setUsername("USERNAME_FAILED");
            builder.append("\nERROR: Failed to set webhook username. Please check logs for details.");
            System.out.println("[Tellurium Chat Bridge] Failed to set webhook username with uuid: " + uuid); // todo: remove hardcoded prefix
        }

        try {
            String username = usernameFromUUID(uuid);
            String skinUrl = skinUrlFromUUID(uuid);

            // create a mineskin api post request to generate our head
            MineskinClient mineskinClient = new MineskinClient("TelluriumChatBridge/1.0.0");
            mineskinClient.generateUrl(skinUrl, new SkinOptions(username)).thenAccept(skin -> {
                // grab 3d render of head from mineskin id
                String headUrl = "https://api.mineskin.org/render/" + skin.uuid + "/head";
                // set webhook avatar to this 3d head image
                builder.setAvatarUrl(headUrl);
                // send the webhook message
                client.send(builder.build());
            });
        } catch (IOException error) {
            // notify something broke through message content and logs
            builder.append("\nERROR: Failed to set webhook avatar. Please check logs for details.");
            System.out.println("[Tellurium Chat Bridge] Failed to set webhook avatar with uuid: " + uuid); // todo: remove hardcoded prefix
        }
    }

    private static String formatAsTellraw(String message) {
        // remove multiline
        message = message.replaceAll("\n", " ");
        // log message
        System.out.println("[Tellurium Chat Bridge] " + message);
        // returns something like /tellraw @a {"text": "GoodPro712 D » Hello World!"}
        return
                "/tellraw @a " +
                    "{" +
                        "\"text\": \"" + message + "\"" +
                    "}";
    }

    // todo: handle errors
    public static String usernameFromUUID(String uuid) throws IOException {
        JSONObject usernameData = JsonReader.fromUrl("https://api.mojang.com/user/profile/" + uuid);
        return usernameData.getString("name");
    }

    public static String skinUrlFromUUID(String uuid) throws IOException {
        // get profile json from Mojang api
        JSONObject profileJson = JsonReader.fromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
        //  {
        //      "id": "<profile identifier>",
        //      "name": "<player name>",
        //      "properties": [
        //          {
        //              "name": "textures",
        //              "value": "<base64 string>",
        //              "signature": "<base64 string; signed data using Yggdrasil's private key>" // Only provided if ?unsigned=false is appended to url
        //          }
        //      ]
        //  }

        // get texture json
        JSONObject textureJson = (JSONObject)profileJson.getJSONArray("properties").get(0);
        //  {
        //      "name": "textures",
        //      "value": "<base64 string>",
        //      "signature": "<base64 string; signed data using Yggdrasil's private key>" // Only provided if ?unsigned=false is appended to url
        //  }

        // get decoded base64 string of value in texture json
        String texturesValue = textureJson.getString("value");
        //  "value": "<base64 string>",

        // decode base64 into new json object
        JSONObject decodedTextureJson = new JSONObject(new String(Base64.decodeBase64(texturesValue)));
        //  {
        //      "timestamp": <java time in ms>,
        //      "profileId": "<profile uuid>",
        //      "profileName": "<player name>",
        //      "signatureRequired": true, // Only present if ?unsigned=false is appended to url
        //      "textures": {
        //          "SKIN": {
        //              "url": "<player skin URL>"
        //          },
        //          "CAPE": {
        //              "url": "<player cape URL>"
        //          }
        //      }
        //  }

        // get skin json object from textures json object from decoded texture json object (so many json objects)
        JSONObject skinData = decodedTextureJson.getJSONObject("textures").getJSONObject("SKIN");
        //  "SKIN": {
        //      "url": "<player skin URL>"
        //  },

        // return key url in json object SKIN
        return skinData.getString("url");
        //  "url": "<player skin URL>"
    }
}