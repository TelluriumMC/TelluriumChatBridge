package net.telluriummc.telluriumchatbridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.telluriummc.telluriumchatbridge.data.DiscordBridgeMessage;
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
import java.util.concurrent.ExecutionException;

public class TelluriumDiscordBot
{
    private JDA bot;
    private List<TextChannel> bridgeChannels;
    private final MinecraftServer mcServer;
    private final DiscordBridgeMessage previousSender;

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
        previousSender = new DiscordBridgeMessage();
        
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

                if (channel == null) {
                    System.out.println("[Tellurium Chat Bridge] Discord channel initialization failed due to channel not existing. Channel id provided: " + channelId);
                    continue;
                }

                // add the channel and print this info to console
                bridgeChannels.add(channel);
                System.out.println("[Tellurium Chat Bridge] Initialized Discord channel with the name of " + channel.getName()  + " and the id of " + channelId);
            }
        }).start();

        handleMinecraftMessage("Server started!", "");
    }

    public void handleDiscordMessage(@NotNull MessageReceivedEvent event) {
        // temp cache variables for less method calls
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        User author = event.getAuthor();
        String messageContent = message.getContentRaw();
        String eventChannelId = channel.getId();
        String authorName = author.getName();
        String authorId = author.getId();

        // commands
        if (messageContent.startsWith(TelluriumChatBridge.botPrefix)) {
            switch (messageContent.substring(TelluriumChatBridge.botPrefix.length())) {
                case "ping" -> channel.sendMessage("Pong! Latency: `" + bot.getGatewayPing() + "ms`").queue();
                case "online", "players", "playing" -> {
                    StringBuilder playerList = new StringBuilder();
                    for (String player : mcServer.getPlayerNames()) {
                        playerList.append(player.replace("_", "\\_")).append(", ");
                    }

                    // remove the ", " from the final string
                    String formattedPlayerList = playerList.toString().equals("") ? "No players online" : playerList.substring(0, playerList.length() - 2);

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("**Online Players (" + mcServer.getCurrentPlayerCount() + ")**");
                    embed.addField("Players", formattedPlayerList, false);
                    channel.sendMessageEmbeds(embed.build()).queue();
                }
                case "health", "stats", "tps", "mspt" -> {
                    float mspt = mcServer.getTickTime() + 50; // millisecond sleep
                    float tps = 1000 / (mspt); // 1000 because millisecond to second

                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("**Server Health**");
                    embed.addField("TPS", "Server TPS - " + tps + "/20", true);
                    embed.addField("MSPT", "Server MSPT - " + mspt, true);
                    channel.sendMessageEmbeds(embed.build()).queue();
                }
            }
        }

        // make sure we don't send anything if the content is sent by our bot and starts with ```[
        if ((authorId.equals(bot.getSelfUser().getId()) && messageContent.startsWith("**[")) || message.isWebhookMessage()) {
            return;
        }

        // Sends a message that looks something like GoodPro712 D » This is a message from Discord!
        if (mcServer.getCurrentPlayerCount() > 0) {
            CommandManager commandManager = mcServer.getCommandManager();
            String attachment = !message.getAttachments().isEmpty() ? "<Attachment>" : "";

            String text = TelluriumChatBridge.discordToMinecraftFormatting
                    .replace("{player_name}", authorName)
                    .replace("attachment", attachment)
                    .replace("{text}", messageContent);

            commandManager.execute(mcServer.getCommandSource(), formatAsTellraw(text));
        }

        if (!message.getAuthor().isBot()) {
            // send messages to every other channel
            handleDiscordToDiscord(message, eventChannelId, authorName, authorId, event.getGuild().getName());
        }
    }

    private void handleDiscordToDiscord(Message message, String eventChannelId, String authorName, String authorId, String guildName) {
        List<Message.Attachment> attachments = message.getAttachments();

        for (TextChannel bridgeChannel : bridgeChannels) {
            if (bridgeChannel.getId().equals(eventChannelId)) {
                continue;
            }

            StringBuilder text = new StringBuilder();

            if (!(previousSender.getUserId().equals(authorId) && previousSender.getChannelId().equals(eventChannelId))) {
                text.append("**[").append(authorName).append(" from ").append(guildName).append("]**\n");
            }

            text.append(message.getContentRaw());

            MessageAction newMessage = bridgeChannel.sendMessage(text.toString());

            if (!attachments.isEmpty()) {
                for (Message.Attachment attachment : attachments) {
                    try {
                        newMessage.addFile(attachment.downloadToFile().get());
                    } catch (InterruptedException | ExecutionException error) {
                        error.printStackTrace();
                    }
                }
            }

            newMessage.queue();
        }

        previousSender.set(authorId, eventChannelId);
    }

    public void handleMinecraftMessage(String rawMessage, @NotNull String uuid) {
        for (String webhookUrl : TelluriumChatBridge.webhookUrls) {
            // create webhook builder
            WebhookClient client = WebhookClient.withUrl(webhookUrl);

            // we don't want blank messages nor private commands
            if (rawMessage.isBlank() || rawMessage.startsWith("/")) {
                return;
            }

            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.append(rawMessage);

            // server host
            if (uuid.equals("00000000000000000000000000000000") || uuid.isEmpty()) {
                builder.setUsername("Server Host");
                builder.setAvatarUrl("https://eu.mc-api.net/v3/server/favicon/" + mcServer.getServerIp());
                client.send(builder.build());
                return;
            }

            // set the webhook username to minecraft IGN
            try {
                // set the username with our helper method
                builder.setUsername(usernameFromUUID(uuid));
            } catch (IOException error) {
                // notify something broke through username, message content and logs respectively
                builder.setUsername("USERNAME_FAILED");
                builder.append("\nERROR: Failed to set webhook username. Please check logs for details.");
                System.out.println(TelluriumChatBridge.ConsolePrefix + "Failed to set webhook username with uuid: " + uuid);
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
                System.out.println(TelluriumChatBridge.ConsolePrefix + "Failed to set webhook avatar with uuid: " + uuid);
            }
        }
    }

    private static @NotNull String formatAsTellraw(String message) {
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