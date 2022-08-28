package OverflowGateBot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import arc.util.Strings;
import mindustry.net.Host;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

import static OverflowGateBot.OverflowGateBot.*;

public class ServerStatus {

    HashMap<String, Message> serverStatus = new HashMap<>();
    List<String> serverList = new ArrayList<String>();

    public ServerStatus() {
        // Delete all message in status channel
        MessageHistory history = MessageHistory.getHistoryFromBeginning(messages.guild.getTextChannelById(
                messages.serverStatusChannel))
                .complete();
        List<Message> msg = history.getRetrievedHistory();
        msg.forEach(_msg -> _msg.delete().queue());

        registerServerCommand("Fourgamingstudio.ddns.net");
        registerServerCommand("america-flux.at.playit.gg:23208");
        registerServerCommand("parts-syracuse.at.playit.gg:56752");
        registerServerCommand("notes-immigration.at.playit.gg:23209");
    }

    public void displayServerStatus(String ip) {
        if (serverStatus.containsKey(ip))
            return;

        net.run(0l, 60000l, () -> {
            net.pingServer(ip, result -> {
                EmbedBuilder builder = serverStatusBuilder(ip, result);

                if (serverStatus.containsKey(ip)) {
                    serverStatus.get(ip).editMessageEmbeds(builder.build()).queue();
                } else {
                    messages.guild.getTextChannelById(
                            messages.serverStatusChannel).sendMessageEmbeds(builder.build()).queue(_message -> {
                                serverStatus.put(ip, _message);
                            });
                }
            });
        });
    }

    public void refreshServerStat() {
        for (String ip : serverStatus.keySet()) {
            net.pingServer(ip, result -> {
                EmbedBuilder builder = serverStatusBuilder(ip, result);

                if (serverStatus.containsKey(ip)) {
                    serverStatus.get(ip).editMessageEmbeds(builder.build()).queue();
                } else {
                    messages.guild.getTextChannelById(
                            messages.serverStatusChannel).sendMessageEmbeds(builder.build()).queue(_message -> {
                                serverStatus.put(ip, _message);
                            });
                }
            });
        }
    }

    public void registerServerCommand(String ip) {
        serverList.add(ip);
        displayServerStatus(ip);
    }

    public EmbedBuilder serverStatusBuilder(String ip, Host result) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();
        if (result.name != null) {
            builder.setTitle("Online");
            field.append("Server name: " + Strings.stripColors(result.name) +
                    "\nServer ip:" + ip +
                    "\nPlayers: " + result.players +
                    "\nMap: " + Strings.stripColors(result.mapname) +
                    "\nWave: " + result.wave +
                    "\nVersion: " + result.version +
                    "\nPing: " + result.ping + "ms\n");

        } else {
            builder.setTitle("Offline");
            field.append("Server ip:" + ip +
                    "\nServer offline or not found\n");

        }
        builder.addField("Server info:", field.toString(), true);
        builder.addBlankField(true);
        builder.addField("Last update", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                .format(Calendar.getInstance().getTime()), true);
        return builder;
    }
}
