package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class GroupPermRemoveCommand extends GroupPermCommand {

    public GroupPermRemoveCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group Perm Remove");
        setCommandUsage("/pg perm remove [group] [world:]node");
        addCommandExample("/pgpr user privileges.build");
        addCommandExample("/pgpr default world_nether:privileges.interact");
        setArgRange(2, 2);
        addKey("privileges group perm remove");
        addKey("priv group perm remove");
        addKey("pgroup perm remove");
        addKey("pg perm remove");
        addKey("pgp remove");
        addKey("pgpr");
        setPermission("privileges.group.perm.remove", "Allows this user to remove permissions nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group group = plugin.getGroupManager().getGroup(args.get(0));
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "That group does not exist.");
            return;
        }
        if (!plugin.getGroupManager().checkRank(sender, group.getRank())) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        String[] param = validateNode(args.get(1));
        if (param == null) {
            sender.sendMessage(ChatColor.RED + "Invalid node string.");
            return;
        }
        String node = param[0];
        String world = param[1];
        if (node.startsWith("-")) { node = node.substring(1); }
        group.removePermission(world, node);
        group.removePermission(world, "-" + node);
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, node) + "' has been removed from the group " + group.getName());
        plugin.log(">> " + sender.getName() + ": " + group.getName() + "'s node '" + node + "' has been removed.");
        reload(sender);
    }

}
