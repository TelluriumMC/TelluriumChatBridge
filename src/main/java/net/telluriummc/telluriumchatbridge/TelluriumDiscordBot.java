package net.telluriummc.telluriumchatbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class TelluriumDiscordBot
{
    private JDA bot;
    private List<TextChannel> bridgeChannels;
    private final MinecraftServer mcServer;

    public TelluriumDiscordBot(String token, MinecraftServer server, String[] channelIds) {
        try {
            bot = JDABuilder.createDefault(token).build();
        } catch (LoginException error) {
            error.printStackTrace();
        }

        bot.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                HandleDiscordMessage(event);
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
                // add the channel and print this info to console
                bridgeChannels.add(channel);
                System.out.println("[Tellurium Chat Bridge] Initialized Discord channel with the name of " + channel.getName() + " and the id of " + channel.getId());
            }
        }).start();
    }

    public void HandleDiscordMessage(MessageReceivedEvent event) {
        // Handles all messages sent in any channel that the bot has permissions for, in any server.
        String messageContent = event.getMessage().getContentRaw();
        String botPrefix = "!"; // todo: config

        // !ping command
        if (messageContent.equalsIgnoreCase(botPrefix + "ping")) {
            event.getChannel().sendMessage("Pong!").queue();
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
                    mcServer.getCommandManager().execute(mcServer.getCommandSource(), FormatAsTellraw(event.getAuthor().getName() + nameSuffix + filler + messageContent));
                }
            }
        }
    }

    private static String FormatAsTellraw(String message) {
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
}