package OverflowGateBot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import static OverflowGateBot.OverflowGateBot.*;

public class UserHandler {

    public String filePath = "users.txt";
    private HashMap<String, DiscordUser> users = new HashMap<>();
    private Long spamTime = 1000l;
    private Long warnTime = 5000l;

    public class DiscordUser {
        String id;
        String name;
        Message lastMessage;
        Long lastMessageTime;
        Integer point;
        Integer level;
        Integer warn = 0;
        Long lastWarnTime = 0l;
        Long muteTime = 0l;
        Long muteFrom = 0l;

        public DiscordUser(String id, String name, Integer point, Integer level) {
            this.id = id;
            this.name = name;
            this.point = point;
            this.level = level;

        }

        public String toString() {
            return id + " " + name + " " + lastMessageTime + " " + point + " " + level
                    + " " + warn
                    + "\n";
        }

        public void warning(int times) {
            this.warn += times;
            this.lastWarnTime = System.currentTimeMillis();
            this.checkMute();
        }

        public void checkWarn() {
            int times = (int) ((System.currentTimeMillis() - this.lastWarnTime) / warnTime);
            if (times > this.warn) {
                this.warn = 0;
                return;
            }
            this.warn -= times;
        }

        public int getWarn() {
            checkWarn();
            return this.warn;
        }

        public void checkMute() {
            if (System.currentTimeMillis() - this.muteFrom > this.muteTime)
                this.muteTime = 0l;

            if (this.warn > 7) {
                mute(1 * 60l);
                messages.replyTempMessage(this.lastMessage, "You have been muted for 1 minute", 20);
            } else if (this.warn > 6) {
                messages.replyTempMessage(this.lastMessage, "Stop or get muted", 20);
                return;
            } else if (this.warn > 5) {
                messages.replyTempMessage(this.lastMessage, "Slow down", 20);
                return;
            }
        }

        public void mute(Long time) {
            this.muteFrom = System.currentTimeMillis();
            this.muteTime = time * 1000;
        }

        public boolean isMuted() {
            return this.muteTime > 0;
        }
    }

    public UserHandler() {
    }

    public void loadUsers() {
        File file;
        try {
            file = new File(filePath);
        } catch (Exception e) {
            file = new File(filePath);
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try {
            DataInputStream data = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<Member> members = messages.guild.getMembers();
        for (Member m : members) {
            if (users.containsKey(m.getId()))
                continue;
            users.put(m.getId(), new DiscordUser(m.getId(), m.getEffectiveName(), 0, 0));
        }

    }

    public boolean checkSpam(Message message) {
        Member member = message.getMember();
        String userId = member.getId();

        if (!users.containsKey(userId)) {
            users.put(member.getId(), new DiscordUser(member.getId(), member.getEffectiveName(), 0, 0));
        }
        DiscordUser dUser = users.get(userId);

        dUser.lastMessage = message;
        dUser.lastMessageTime = System.currentTimeMillis();

        if (dUser.isMuted())
            message.delete().queue();

        dUser.checkWarn();

        if (System.currentTimeMillis() - dUser.lastMessageTime > spamTime)
            return false;
        dUser.warning(1);
        return true;
    }

    public void muteUser(Member member, Long time) {
        String userId = member.getId();
        if (!users.containsKey(userId)) {
            users.put(member.getId(), new DiscordUser(member.getId(), member.getEffectiveName(), 0, 0));
        }
        users.get(userId).mute(time);
        ;
    }
}