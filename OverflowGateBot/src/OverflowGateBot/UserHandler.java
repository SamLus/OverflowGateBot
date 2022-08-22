package OverflowGateBot;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import arc.util.Log;

public class UserHandler {
    public String fileName = "users.txt";

    public class DiscordUser {
        String id;
        String name;
        Integer lastMessageTime;
        Integer point;
        Integer level;
        Boolean muted;
        Boolean banned;

        public DiscordUser(String id, String name, Integer lastMessageTime, Integer point, Integer level, Boolean muted,
                Boolean banned) {
            this.id = id;
            this.name = name;
            this.lastMessageTime = lastMessageTime;
            this.point = point;
            this.level = level;
            this.muted = muted;
            this.banned = banned;
        }

        public String toString() {
            return id + " " + name + " " + lastMessageTime + " " + point + " " + level + " " + muted + " " + banned
                    + "\n";
        }
    }

    HashMap<String, DiscordUser> users = new HashMap<>();

    public UserHandler() {
    }

    public void loadUsers() {
        try {
            FileReader reader = new FileReader(fileName);

        } catch (Exception e) {
            Log.info("No users file found, creating new one");
            File file = new File(fileName);
            try {
                file.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
