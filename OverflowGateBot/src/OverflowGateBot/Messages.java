package OverflowGateBot;

import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static OverflowGateBot.OverflowGateBot.*;

public class Messages extends ListenerAdapter {
    String[] schematic_channel = { "schematic", "kênh-tin-nhắn-1", "kênh-tin-nhắn-2", };

    private final JDA jda;

    LongSeq schematicChannels = new LongSeq();

    public Messages() {
        String token = "";

        try {
            jda = JDABuilder.createDefault(token)
                    .addEventListeners(this)
                    .build();
            ;
            jda.awaitReady();

            Log.info("Discord bot up.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        var msg = event.getMessage();

        if (msg.getAuthor().isBot())
            return;

        // schematic preview
        if ((msg.getContentRaw().startsWith(ContentHandler.schemHeader) && msg.getAttachments().isEmpty()) ||
                (msg.getAttachments().size() == 1 && msg.getAttachments().get(0).getFileExtension() != null
                        && msg.getAttachments().get(0).getFileExtension().equals(Vars.schematicExtension))) {
            // no schematic in others channels
            if (!isSchematicChannel(msg.getChannel())) {
                textDeleteAfter(msg.getChannel(), "Please don't send schematic in this channel", 60);
                msg.delete().queue();
                return;
            }

            try {
                Schematic schem = msg.getAttachments().size() == 1
                        ? contentHandler.parseSchematicURL(msg.getAttachments().get(0).getUrl())
                        : contentHandler.parseSchematic(msg.getContentRaw());
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
                        .setAuthor(msg.getAuthor().getName(), msg.getAuthor().getEffectiveAvatarUrl(),
                                msg.getAuthor().getEffectiveAvatarUrl())
                        .setTitle(schem.name());

                if (!schem.description().isEmpty())
                    builder.setFooter(schem.description());

                msg.getChannel().sendFile(schemFile).addFile(previewFile).setEmbeds(builder.build()).queue();
                msg.delete().queue();
            } catch (Exception e) {
                Log.err(e);
            }
        }
    }

    public void text(MessageChannel channel, String text, Object... args) {
        channel.sendMessage(Strings.format(text, args)).queue();
    }

    public void text(Message message, String text, Object... args) {
        text(message.getChannel(), text, args);
    }

    public void textDeleteAfter(MessageChannel channel, String text, long delay, Object... args) {
        channel.sendMessage(text).queue((message) -> {
            message.delete().queueAfter(delay, TimeUnit.SECONDS);
        });
    }

    public boolean isSchematicChannel(MessageChannel channel) {
        for (String ch : schematic_channel) {
            if (channel.getName().equals(ch))
                return true;
        }
        return false;
    }

    public boolean isTextAllowed() {
        return true;
    }
}
