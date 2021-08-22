# Overview
[![wakatime](https://wakatime.com/badge/github/TelluriumMC/TelluriumChatBridge.svg)](https://wakatime.com/badge/github/TelluriumMC/TelluriumChatBridge)
[![CodeFactor](https://www.codefactor.io/repository/github/telluriummc/telluriumchatbridge/badge)](https://www.codefactor.io/repository/github/telluriummc/telluriumchatbridge)
![GitHub repo size](https://img.shields.io/github/repo-size/telluriummc/telluriumchatbridge)
![GitHub](https://img.shields.io/github/license/telluriummc/telluriumchatbridge)
![GitHub last commit](https://img.shields.io/github/last-commit/telluriummc/telluriumchatbridge)
![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/telluriummc/telluriumchatbridge?include_prereleases&sort=semver)

**Server-side [FabricMC](https://www.fabricmc.net/) mod featuring a [Discord](https://discord.com/) &lt;-> Minecraft chat bridge for the 
[Tellurium](https://telluriummc.github.io) network, with multiple features not found in other chat bridge mods.**

**Built with [JDA](https://github.com/DV8FromTheWorld/JDA), [Mineskin](https://github.com/InventivetalentDev/MineskinClient), and [Oro-Config](https://github.com/OroArmor/Oro-Config).**

\- This is my first time using Java, so this code might not have the best practices in it.

## Contents
- [Features](#features)
    - [Multiple Discord Channel Support](#multiple-discord-channel-support)
    - [3d Head Rendering](#3d-head-rendering)
    - [Death Messages, Player Connections and Server Starting Announcements](#death-messages-player-connections-and-server-starting-announcements)
    - [Discord Bot Commands](#discord-bot-commands)
    - [Json Configuration](#json-configuration)
    - [Console Logging](#console-logging)
- [Setup](#setup)
- [Configuration](#configuration)
- [Commands](#commands)

## Features
### Multiple Discord Channel Support
Broadcast messages from multiple Discord channels into Minecraft, from multiple different servers! 
This also features Discord to Discord chat bridging, so you wont miss a message.
This also supports attachments! Upload images and files and they will be sent through, too, and if a message had an attachment, this will show in Minecraft chat too!

![image](https://user-images.githubusercontent.com/45357714/130361637-5b31f570-a011-4959-898c-300b544bc869.png)

<details><summary><strong>Channel One</strong></summary>
 
![channel-1](https://user-images.githubusercontent.com/45357714/130361056-ce889159-9815-4648-8259-1316671ae818.png)
</details>

<details><summary><strong>Channel Two</strong></summary>
  
![channel-2](https://user-images.githubusercontent.com/45357714/130361102-f9fce4bb-4d1e-4328-b8a3-5c37922f4664.png)
</details>

### 3d Head Rendering
Discord webhooks allow us to set our own avatar for each message, which means we can set them based on the player who talks.
Thanks to [Mineskin](https://github.com/InventivetalentDev/MineskinClient), we are able to generate a rendered image of the player's head.

![image](https://user-images.githubusercontent.com/45357714/130361272-9e52003b-c183-4dd9-911c-4de8806de6f1.png)

### Death Messages, Player Connections and Server Starting Announcements
Broadcast player death messages, join and leave messages, and server start/stop messages straight to Discord.

![image](https://user-images.githubusercontent.com/45357714/130361408-48873c7c-9ea9-46b7-af48-9012359988c1.png)

![image](https://user-images.githubusercontent.com/45357714/130361426-034b5045-bf87-4b5e-ae35-1334f0920b3a.png)*

![image](https://user-images.githubusercontent.com/45357714/130361435-9e2b9e4c-307e-4e81-a734-5966188ceb8b.png)

*Discord formatting was broken in this screenshot. It has since been updated to \_Skypie\_ was shot by Skeleton

### Discord Bot Commands
Commands to check the server's health (TPS, and MSPT), the current players online, and the bot's latency, are all prebuilt into the Discord bot.

![image](https://user-images.githubusercontent.com/45357714/130361570-75e8c95f-6dc2-4178-9a80-0a8da84c9d66.png)

![image](https://user-images.githubusercontent.com/45357714/130361533-ef52dce8-48c9-4f14-8fe0-790798d4db90.png)

![image](https://user-images.githubusercontent.com/45357714/130361567-77bce539-dbf2-4234-954d-2ec3935130fa.png)

### Json Configuration
Automatically generating config file, with the ability to customize everything!
See [configuration](#configuration) below.

### Console Logging
Logs all messages in the server console, as well as errors and initialization.

![image](https://user-images.githubusercontent.com/45357714/130362248-4b45561f-4bd6-4ff3-b8e4-56a13b5f2859.png)

## Setup
- Download the `.jar` file at either the GitHub [releases](https://github.com/TelluriumMC/TelluriumChatBridge/releases) page, 
or download the artifact from the [latest build](https://github.com/TelluriumMC/TelluriumChatBridge/actions).
- Move the `.jar` file into `server/mods` and run the server using your startup script.
- After the server crashes, edit the config file at `server/config/tellurium-chat-bridge.json` (See [configuration](#configuration) for more)
- You're good to go!

## Configuration
Example configuration file:
```json
{
  "tellurium_chat_bridge_config": {
    "discord_bot_token": "Bot token here",
    "server_name": "TMP",
    "discord_channel_ids": [
      "866299513476343962",
      "836222513389183949",
      "821686696895737887"
    ],
    "webhook_urls": [
      "https://discord.com/api/webhooks/821686696895737887/FQO9otfxTI6Ml7mMY6dt-5IpkS81KhYnvz-EgmcwfFm7G1_PNg8cvwAFOjD4QaVC_cdY",
      "https://discord.com/api/webhooks/866299513476343962/35kSpD_NjepvJ6WOXY9FAHZ1l7bWOUkxelyQieLPKUPtjjzXm4dwf7AdsNv45HIpPA2A"
    ],
    "discord_to_minecraft_formatting": "{player_name} §9§lD §r§8»§r {attachment} {text}",
    "bot_prefix": "!"
  }
}
```
(Yes, these webhook URLs are fake)

----------------------------------

`discord_bot_token` - Your Discord bot token.
<details><summary><strong>How do I get a Discord bot token?</strong></summary>
Go to https://discordapp.com/developers.

Click `My apps` in the top left:

![img](https://i.imgur.com/msNDtLt.png)
Click `New App`:

![img](https://i.imgur.com/zSTbluP.png)
Give your bot a name and optionally a description and avatar:  

![img](https://i.imgur.com/mwmIn1y.png)
Click `Create App`:

![img](https://i.imgur.com/MbH7tX2.png)
Scroll down and click `Create a Bot User`:

![img](https://i.imgur.com/G4L7X0l.png)
Click `Yes, do it!`:

![img](https://i.imgur.com/Mdfar29.png)
Click `click to reveal` nex to `Token:`:

![img](https://i.imgur.com/sOIvcXU.png)
</details>

`server_name` - The name of the server (for example SMP, CMP, etc).

`discord_channel_ids` - An array of Discord channel ids. Make sure to add a comma after each item.

`webhook_urls` - An array of Discord webhook URLs. Make sure to add a comma after each item.

`discord_to_minecraft_formatting` - Formatting for what Minecraft chat sees for Discord messages.

| property | use |
| --- | --- |
| **`{player_name}`** | the author's name of the Discord message |
| **`{attachment}`** | where `<Attachment>` shows when an attachment is sent through Discord |
| **`{text}`** | the raw message that was sent to Discord |

`bot_prefix` - The prefix that the bot uses when typing commands.

## Commands
| command | alias | use |
| --- | --- | --- |
| **`ping`** | `ping` | Returns bot latency |
| **`online`** | `online`, `players`, `playing` | Returns current online players |
| **`health`** | `health`, `stats`, `tps`, `mspt` | Returns server health (tps & mspt) |
