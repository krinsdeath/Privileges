package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class GroupCheckCommand extends GroupCommand {

    public GroupCheckCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Group Check");
        setCommandUsage("/pg check [group] [world] [node]");
        addCommandExample(ChatColor.GREEN + "/pg" + ChatColor.AQUA + " owner" + ChatColor.GOLD + " privileges.group.create");
        addCommandExample(ChatColor.GREEN + "/pg" + ChatColor.AQUA + " owner" + ChatColor.GOLD + " world" + ChatColor.YELLOW + " privileges.group.list");
        setArgRange(2, 3);
        addKey("privileges group check");
        addKey("priv group check");
        addKey("pgroup check");
        addKey("pg check");
        setPermission("privileges.group.check", "Checks whether the specified group has explicit access to the given permission.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group g = plugin.getGroupManager().getGroup(args.get(0));
        if (g == null) {
            sender.sendMessage(ChatColor.RED + "Unknown group.");
            return;
        }
        if ((sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) && args.size() == 2) {
            sender.sendMessage(ChatColor.RED + "Must supply a world from the console.");
            return;
        }
        String world = args.size() == 2 ? ((Player) sender).getWorld().getName() : args.get(1);
        String node = args.size() == 3 ? args.get(2) : args.get(1);
        boolean val = g.hasPermission(node, world);
        sender.sendMessage(ChatColor.GREEN + g.getName() + ChatColor.RESET + "'s node '" + ChatColor.AQUA + node + ChatColor.RESET + "' has a value of '" + ChatColor.AQUA + val + ChatColor.RESET + "' on " + ChatColor.GOLD + world + ChatColor.RESET + ".");
    }
}
