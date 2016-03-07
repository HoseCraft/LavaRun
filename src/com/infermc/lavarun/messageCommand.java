package com.infermc.lavarun;

import com.infermc.hosecraft.command.CommandInterface;
import com.infermc.hosecraft.command.CommandSource;
import com.infermc.hosecraft.server.Player;
import com.infermc.hosecraft.server.Server;

public class messageCommand implements CommandInterface {
    private Server server;
    private LavaRun parent;

    public messageCommand(Server server,LavaRun parent) {
        this.server = server;
        this.parent = parent;
    }

    @Override
    public boolean run(CommandSource commandSource, String[] strings) {
        if (strings.length >= 2) {
            String user = strings[0];
            if (parent.isOnline(user)) {
                Player p = parent.getOnlinePlayer(user);
                String scentence = "";
                for (int i=1;i<strings.length; i++) {
                    scentence += " "+strings[i];
                }
                String remote = "&6[&ePM:"+commandSource.getName()+"&6] &e"+scentence.trim();
                String local = "&6[&ePM:@"+p.getName()+"&6] &e"+scentence.trim();
                for (String m : parent.wordWrap(remote, 64)) p.sendMessage(m.trim());
                for (String m : parent.wordWrap(local, 64)) commandSource.sendMessage(m.trim());
            } else {
                commandSource.sendMessage("&cPlayer offline.");
            }
        } else {
            commandSource.sendMessage("Syntax: /msg username message");
        }
        return true;
    }
}
