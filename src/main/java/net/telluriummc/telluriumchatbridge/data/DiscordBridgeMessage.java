package net.telluriummc.telluriumchatbridge.data;

public class DiscordBridgeMessage {
    protected String userId;
    protected String channelId;

    public DiscordBridgeMessage(String userId, String channelId) {
        this.userId = userId;
        this.channelId = channelId;
    }

    public DiscordBridgeMessage() {
        this.userId = "";
        this.channelId = "";
    }

    public DiscordBridgeMessage set(String userId, String channelId) {
        this.userId = userId;
        this.channelId = channelId;
        return this;
    }

    public DiscordBridgeMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DiscordBridgeMessage setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public DiscordBridgeMessage get() {
        return this;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getChannelId() {
        return this.channelId;
    }
}
