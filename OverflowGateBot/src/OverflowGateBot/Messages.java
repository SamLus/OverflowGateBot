package OverflowGateBot;

import arc.files.*;
import arc.util.*;
import arc.util.io.Streams;
import mindustry.*;
import mindustry.game.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.imageio.*;

import org.jetbrains.annotations.NotNull;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import mindustry.type.ItemStack;

import static OverflowGateBot.OverflowGateBot.*;

public class Messages extends ListenerAdapter {

    private final JDA jda;

    public String prefix = "/";

    public Guild guild;

    long discussSchematic = 1010379724440207381l;
    long donateSchematic = 1010383122623385631l;
    long librarySchematic = 1010383221256622132l;
    long librarySchematic2 = 1010387602735648919l;

    long discussLogic = 1010405416968138822l;

    long botRatio = 1008912317485940806l;

    long textChannel1 = 1010373926100148356l;
    long textChannel2 = 1010375863977639946l;

    long botSpam = 1010405590910107668l;

    long donateMap = 1010383138435903630l;
    long discussMap = 1010382856557703168l;
    long libraryMap = 1010387795338080287l;

    long serverStatusChannel = 1012362641060155462l;

    long[] noTextChannel = {
            donateSchematic,
            donateMap,
    };

    long[] schematicChannel = {
            discussSchematic,
            donateSchematic,
            librarySchematic,
            librarySchematic2,
            discussLogic,
            textChannel1,
            textChannel2
    };

    long[] mapChannel = {
            donateMap,
            discussMap,
            libraryMap,
    };

    CommandHandler commandHandler = new CommandHandler();

    public class CommandHandler {
        HashMap<String, Command> commands = new HashMap<>();

        public void register(String command, int arg, String argv, String decs, String usage) {
            commands.put(command, new Command(command, arg, argv, decs, usage));
        }

        public Command getCommand(String command) {
            if (commands.containsKey(command)) {
                return commands.get(command);
            }
            return null;
        }
    }

    public class Command {
        String name;
        Integer args;
        String argsv;
        String desc;
        String usage;
        int counter = 0;

        public Command(String name, Integer args, String argsv, String desc, String usage) {
            this.name = name;
            this.args = args;
            this.argsv = argsv;
            this.desc = desc;
            this.usage = usage;
        }
    }

    public Messages() {
        String token = "";

        try {
            commandHandler.register("help", 1, "help", "List all commands", "help");
            commandHandler.register("help", 2, "help command_name", "How to use a command", "Input command name");
            commandHandler.register("postmap", 1, "postmap", "Preview map file",
                    "Attach map file to command to preview it");
            commandHandler.register("postschem", 1, "postschem", "Preview schematic file",
                    "Reply a message to post its file");
            commandHandler.register("refreshserver", 1, "refreshserver",
                    "Refresh server status instantly", "refreshserver");

            jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();

            jda.awaitReady().getPresence()
                    .setActivity(Activity.of(ActivityType.PLAYING, "Type " + prefix + "help to start"));

            guild = jda.getGuildById(1010373870395596830l);
            guild.upsertCommand(
                    Commands.slash("postmap", "Preview map in current channel").addOption(OptionType.ATTACHMENT,
                            "mapfile", "Map file to preview"))
                    .queue();

            guild.upsertCommand(
                    Commands.slash("warn", "Warn someone").addOption(OptionType.USER, "user", "User to be warned"))
                    .queue();
            Log.info("Bot online.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        var message = event.getMessage();

        if (message.getAuthor().isBot()) {
            return;
        }
        userHandler.checkSpam(message);

        // command register
        if (message.getContentRaw().startsWith(prefix)) {
            handleCommand(message);
            return;
        }
        // schematic preview
        if ((isSchematicText(message) && message.getAttachments().isEmpty()) || isSchematicFile(message)) {
            handleSchematic(message, message.getTextChannel());
            return;
        }
        // delete invalid message
        if (inChannels(message, noTextChannel) || message.getContentRaw().startsWith("#")) {
            replyErrorMessage(message, "Please don't send message in this channel", 10);
            return;
        }

        Log.info(message.getContentRaw());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("postmap")) {
            MessageChannel channel = event.getHook().getInteraction().getMessageChannel();
            if (event.getOptions().isEmpty()) {
                sendErrorMessage(channel, "Please attach a map file to use this command", 10);
                return;
            }
            handleMap(event);
            return;
        }
    }

    public void text(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public void text(Message message, String text, Object... args) {
        text(message.getChannel(), text, args);
    }

    public void sendErrorMessage(Message message, String text, long delay, Object... args) {
        sendTempMessage(message, text, delay, args);
    }

    public void sendErrorMessage(MessageChannel channel, String text, long delay, Object... args) {
        sendTempMessage(channel, text, delay, args);
    }

    public void sendTempMessage(Message message, String text, long delay, Object... args) {
        sendTempMessage(message.getChannel(), text, delay, args);
        message.delete().queue();
    }

    public void sendTempMessage(MessageChannel channel, String text, long delay, Object... args) {
        channel.sendMessage(text).queue((_message) -> {
            _message.delete().queueAfter(delay, TimeUnit.SECONDS);
        });
    }

    public void replyTempMessage(Message message, String text, long delay, Object... args) {
        message.reply(text).queue((_message) -> {
            _message.delete().queueAfter(delay, TimeUnit.SECONDS);
            message.delete().queueAfter(delay, TimeUnit.SECONDS);
        });
    }

    public void replyErrorMessage(Message message, String text, long delay, Object... args) {
        replyTempMessage(message, text, delay, args);
    }

    public boolean inChannels(Message message, long[] channels) {
        long channelLongId = message.getChannel().getIdLong();
        for (long id : channels) {
            if (id == channelLongId)
                return true;
        }
        return false;
    }

    public boolean inChannels(MessageChannel channel, long[] channels) {
        long channelLongId = channel.getIdLong();
        for (long id : channels) {
            if (id == channelLongId)
                return true;
        }
        return false;
    }

    public boolean isSchematicText(Message message) {
        return message.getContentRaw().startsWith(ContentHandler.schemHeader);
    }

    public boolean isSchematicFile(Message message) {
        for (int i = 0; i < message.getAttachments().size(); i++) {
            if (message.getAttachments().get(i).getFileExtension() != null
                    && message.getAttachments().get(i).getFileExtension().equals(Vars.schematicExtension))
                return true;
        }
        return false;
    }

    public boolean isSchematicFile(Attachment a) {
        return a.getFileExtension() != null && a.getFileExtension().equals(Vars.schematicExtension);
    }

    public boolean isMapFile(Message message) {
        return message.getAttachments().get(0).getFileName().endsWith(".msav");
    }

    public boolean isMapFile(Attachment a) {
        return a.getFileName().endsWith(".msav");
    }

    public String capitalize(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public String bold(String text) {
        return "**" + text + "**";
    }

    public void handleCommand(Message message) {
        String[] command = message.getContentRaw().replace(prefix, "").split(" ");
        // help command
        if (command[0].equals("help") && (command.length == 1 || command.length == 2)) {
            helpCommand(message, command);
            return;
        }
        // postmap command
        else if (command[0].equals("postmap") && command.length == 1) {
            postMapCommand(message, command);
            return;
        }
        // post schem
        else if (command[0].equals("postschem") && command.length == 1) {
            postSchematicCommand(message, command);
            return;
        }

        // refresh server
        else if (command[0].equals("refreshserver") && command.length == 1) {
            serverStatus.refreshServerStat();
            replyTempMessage(message, "Refreshing", 10);
            return;
        }

    }

    public void handleMap(Message message, TextChannel channel) {
        for (int i = 0; i < message.getAttachments().size(); i++) {
            Attachment a = message.getAttachments().get(i);
            if (isMapFile(a)) {
                try {
                    ContentHandler.Map map = contentHandler.readMap(net.download(a.getUrl()));
                    new File("cache/").mkdir();
                    File mapFile = new File("cache/" + a.getFileName());
                    Fi imageFile = Fi.get("cache/image_" + a.getFileName().replace(".msav", ".png"));
                    Streams.copy(net.download(a.getUrl()), new FileOutputStream(mapFile));
                    ImageIO.write(map.image, "png", imageFile.file());

                    EmbedBuilder builder = new EmbedBuilder()
                            .setImage("attachment://" + imageFile.name())
                            .setAuthor(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl(),
                                    message.getAuthor().getEffectiveAvatarUrl())
                            .setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);

                    if (map.description != null)
                        builder.setFooter(map.description);

                    channel.sendFile(mapFile).addFile(imageFile.file()).setEmbeds(builder.build()).queue();
                    message.delete().queue();

                } catch (Exception e) {
                    replyErrorMessage(message, "Error parsing map.", 10);
                }
            }
        }
    }

    public void handleMap(SlashCommandInteractionEvent event) {
        Attachment a = event.getOption("mapfile").getAsAttachment();
        User user = event.getUser();
        try {
            ContentHandler.Map map = contentHandler.readMap(net.download(a.getUrl()));
            new File("cache/").mkdir();
            File mapFile = new File("cache/" + a.getFileName());
            Fi imageFile = Fi.get("cache/image_" + a.getFileName().replace(".msav", ".png"));
            Streams.copy(net.download(a.getUrl()), new FileOutputStream(mapFile));
            ImageIO.write(map.image, "png", imageFile.file());

            EmbedBuilder builder = new EmbedBuilder()
                    .setImage("attachment://" + imageFile.name())
                    .setAuthor(user.getName(), user.getEffectiveAvatarUrl(),
                            user.getEffectiveAvatarUrl())
                    .setTitle(map.name == null ? a.getFileName().replace(".msav", "") : map.name);

            if (map.description != null)
                builder.setFooter(map.description);

            event.replyFile(mapFile).addFile(imageFile.file()).addEmbeds(builder.build()).queue();

        } catch (Exception e) {
            sendErrorMessage(event.getChannel(), "Error parsing map.", 10);
        }
    }

    public void handleSchematic(Message message, TextChannel channel) {
        // no schematic in others channels
        if (!inChannels(message, schematicChannel)
                && message.getChannel().getType() != ChannelType.GUILD_PUBLIC_THREAD) {
            replyTempMessage(message, "Please only send schematic in #schematic channel", 10);
            return;
        }
        try {
            if (message.getAttachments().isEmpty()) {
                previewSchematic(message, contentHandler.parseSchematic(message.getContentRaw()), channel);
            } else {
                for (int i = 0; i < message.getAttachments().size(); i++) {
                    Attachment a = message.getAttachments().get(i);
                    if (isSchematicFile(a)) {
                        previewSchematic(message, contentHandler.parseSchematic(a.getUrl()), channel);
                    }
                }
            }
            message.delete().queue();
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public void previewSchematic(Message message, Schematic schem, TextChannel channel) {
        try {
            BufferedImage preview = contentHandler.previewSchematic(schem);
            String sname = schem.name().replace("/", "_").replace(" ", "_");
            if (sname.isEmpty())
                sname = "empty";

            new File("cache").mkdir();
            File previewFile = new File("cache/img_" + UUID.randomUUID() + ".png");
            File schemFile = new File("cache/" + sname + "." + Vars.schematicExtension);
            Schematics.write(schem, new Fi(schemFile));
            ImageIO.write(preview, "png", previewFile);

            EmbedBuilder builder = new EmbedBuilder()
                    .setImage("attachment://" + previewFile.getName())
                    .setAuthor(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl(),
                            message.getAuthor().getEffectiveAvatarUrl())
                    .setTitle(schem.name());

            if (!schem.description().isEmpty())
                builder.setFooter(schem.description());
            StringBuilder field = new StringBuilder();
            field.append("Size:" + String.valueOf(schem.width) + "x" + String.valueOf(schem.height) + "\n");
            // item requirements
            for (ItemStack stack : schem.requirements()) {
                List<Emote> emotes = guild.getEmotesByName(stack.item.name.replace("-", ""), true);
                Emote result = emotes.isEmpty() ? guild.getEmotesByName("ohno", true).get(0) : emotes.get(0);

                field.append(result.getAsMention()).append(stack.amount).append("  ");
            }
            // power input/output
            float powerProduction = (int) Math.round(schem.powerProduction() * 10) / 10;
            float powerConsumption = (int) Math.round(schem.powerConsumption() * 10) / 10;
            if (powerProduction != 0)
                field.append("\nPower production:" + String.valueOf(powerProduction));
            if (powerConsumption != 0)
                field.append("\nPower consumption:" + String.valueOf(powerConsumption));

            builder.addField("INFO:", field.toString(), true);
            // send embed
            channel.sendFile(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();

        } catch (Exception e) {
            Log.err(e);
        }
    }

    public void helpCommand(Message message, String[] command) {
        if (command.length == 1) {
            EmbedBuilder builder = new EmbedBuilder();
            StringBuffer field = new StringBuffer();
            builder.setTitle(bold("HELP"));
            String desc = "";
            for (String c : commandHandler.commands.keySet()) {
                if (commandHandler.getCommand(c) == null)
                    continue;
                desc = commandHandler.commands.get(c).desc;
                field.append(capitalize(bold(c)) + ": " + capitalize(desc) + "\n");
            }
            builder.addField("Command list", field.toString(), false);
            message.getChannel().sendMessageEmbeds(builder.build()).queue(_message -> {
                _message.delete().queueAfter(60, TimeUnit.SECONDS);
            });
            message.delete().queue();
        } else {
            Command c = commandHandler.getCommand(command[1]);
            if (c == null) {
                replyErrorMessage(message, "Command " + command[1] + " not found", 10);
                return;
            }
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(capitalize(command[1]));
            StringBuilder field = new StringBuilder();

            field.append(c.usage);
            builder.addField(prefix + c.argsv, "_" + field.toString() + "_", true);

            message.getChannel().sendMessageEmbeds(builder.build()).queue(_message -> {
                _message.delete().queueAfter(30, TimeUnit.SECONDS);
            });
            message.delete().queueAfter(30, TimeUnit.SECONDS);
        }
    }

    public void postMapCommand(Message message, String[] command) {
        Message msg = message.getReferencedMessage();
        if (msg != null) {
            if (msg.getAttachments().size() != 0) {
                handleMap(msg, guild.getTextChannelById(donateMap));
                message.delete().queue();
                return;
            }
        }

        if (inChannels(message, mapChannel) || message.getChannel().getType() == ChannelType.GUILD_PUBLIC_THREAD) {
            if (isMapFile(message)) {
                handleMap(message, message.getTextChannel());
                return;
            } else {
                replyErrorMessage(message, "Please don't send message here", 10);
                return;
            }
        }
        replyTempMessage(message, "Please use this command on #map channel", 10);
    }

    public void postSchematicCommand(Message message, String[] command) {
        Message msg = message.getReferencedMessage();
        if (msg == null) {
            replyErrorMessage(message, "Please reply to a message to use this command", 10);
            return;
        }
        handleSchematic(msg, guild.getTextChannelById(donateSchematic));
        message.delete().queue();
    }
}
