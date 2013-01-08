package net.krinsoft.privileges.commands;

import com.pneumaticraft.commandhandler.Command;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public abstract class PrivilegesCommand extends Command {

    protected Privileges plugin;

    public PrivilegesCommand(Privileges instance) {
        super(instance);
        plugin = instance;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

    public String colorize(ChatColor color, String value) {
        return color + value + ChatColor.RESET;
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
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            thing[0] = param;
            thing[1] = null;
        }
        return thing;
    }

}
