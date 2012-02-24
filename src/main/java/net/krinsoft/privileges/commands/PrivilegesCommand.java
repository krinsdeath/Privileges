package net.krinsoft.privileges.commands;

import com.pneumaticraft.commandhandler.Command;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public abstract class PrivilegesCommand extends Command {
    private final static int DEFAULT_PAGE_SIZE = 7;

    class Page {

        private List<String> examples;
        public Page(List<String> examples) {
            this.examples = examples;
        }

        public List<String> getPage(int page) {
            List<String> lines = new ArrayList<String>();
            if ((page_size * page) > examples.size()) {
                page = 0;
            }
            for (int i = (page_size * page); lines.size() <= page_size && i < examples.size(); i++) {
                lines.add(examples.get(i));
            }
            return lines;
        }

        public int getPages() {
            return Math.round(examples.size() / page_size);
        }
    }

    protected Privileges plugin;
    private int page_size = DEFAULT_PAGE_SIZE;

    public PrivilegesCommand(Privileges instance) {
        super(instance);
        plugin = instance;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

    public String colorize(ChatColor color, String value) {
        return color + value + ChatColor.WHITE;
    }

    public void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== " + ChatColor.AQUA + getCommandName() + ChatColor.GREEN + " ===");
        sender.sendMessage(ChatColor.GREEN + "Usage: " + ChatColor.AQUA + getCommandUsage());
        sender.sendMessage(ChatColor.GOLD + getCommandDesc());
        sender.sendMessage(ChatColor.GREEN + "Permission: " + ChatColor.GOLD + this.getPermissionString());
        String keys = "";
        for (String key : this.getKeyStrings()) {
            keys += key + ", ";
        }
        keys = keys.substring(0, keys.length() - 2);
        sender.sendMessage(ChatColor.GREEN + "Aliases: " + ChatColor.RED + keys);
        if (this.getCommandExamples().size() > 0) {
            sender.sendMessage(ChatColor.GREEN + "Examples: ");
            if (sender instanceof Player) {
                for (int i = 0; i < 4 && i < this.getCommandExamples().size(); i++) {
                    sender.sendMessage(this.getCommandExamples().get(i));
                }
            } else {
                for (String c : this.getCommandExamples()) {
                    sender.sendMessage(c);
                }
            }
        }
    }

    public String[] validateNode(String param) {
        String[] thing = new String[2];
        if (param.contains(":")) {
            try {
                thing[0] = param.split(":")[1];
                thing[1] = param.split(":")[0];
                if (plugin.getServer().getWorld(thing[0]) == null) {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        } else {
            thing[0] = param;
            thing[1] = null;
        }
        return thing;
    }

}
