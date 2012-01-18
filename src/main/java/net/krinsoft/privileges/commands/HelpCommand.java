package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.util.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author krinsdeath
 */
public class HelpCommand extends PrivilegesCommand {
    
    public HelpCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges Help");
        this.setCommandUsage("/help [page]");
        this.setArgRange(0, 1);
        this.addKey("privileges help");
        this.addKey("priv help");
        this.addKey("help");
        this.setPermission("privileges.help", "Allows this user to view their available commands.", PermissionDefault.TRUE);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<Command> coms = new ArrayList<Command>();
        for (Plugin p : plugin.getServer().getPluginManager().getPlugins()) {
            for (Command command : PluginCommandYamlParser.parse(p)) {
                coms.add(plugin.getServer().getPluginCommand(command.getName()));
            }
        }
        Collections.sort(coms, new Comparator<Command>() {
            public int compare(Command a, Command b) {
                return a.getName().compareTo(b.getName());
            }
        });
        int page = 0;
        if (args.size() > 0) {
            try {
                page = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid argument.");
                plugin.debug("Invalid argument detected executing command '" + getCommandName() + "'");
            }
        }
        FancyMessage message = new FancyMessage("Help", page, coms, sender);
        sender.sendMessage(message.getHeader());
        for (String line : message.getLines()) {
            sender.sendMessage(line);
        }
    }
}
