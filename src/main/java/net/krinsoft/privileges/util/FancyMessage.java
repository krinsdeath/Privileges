package net.krinsoft.privileges.util;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * @author krinsdeath
 */
public class FancyMessage implements Message {
    private String name;
    private int page;
    private List<String> lines = new ArrayList<String>();
    
    public FancyMessage(String name, int page, List<Command> commands, CommandSender sender) {
        this.name = name;
        this.page = page;
        for (Command com : commands) {
            if (com.getPermission() == null || sender.hasPermission(com.getPermission())) {
                lines.add(ChatColor.GREEN + com.getName() + " (" + ChatColor.AQUA + com.getPermission() + ChatColor.GREEN + "): " + ChatColor.WHITE + com.getDescription());
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getLines() {
        if (page > 0) { page--; }
        List<String> messages = new ArrayList<String>();
        if (page*5 > lines.size()) { page = 0; }
        for (int i = (page * 5); i < (page*5)+5; i++) {
            try {
                if (i >= lines.size()) {
                    break;
                }
                messages.add(lines.get(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    @Override
    public String getHeader() {
        int page = this.page;
        if (page == 0) { page++; }
        int pages = lines.size() / 5 + 1;
        if (page > pages) {
            page = 1;
        }
        return ChatColor.GREEN + "=== " + ChatColor.WHITE + name + " [Page " + (page) + "/" + pages + ChatColor.GREEN + "] ===";
    }
}
