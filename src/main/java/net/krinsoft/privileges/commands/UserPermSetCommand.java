package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class UserPermSetCommand extends UserPermCommand {

    public UserPermSetCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: User Perm Set");
        this.setCommandUsage("/privileges user perm set [user] [world:]node [val]");
        this.addCommandExample("/priv user perm set Player example.node true");
        this.addCommandExample("/pups Player world:example.node false");
        this.setArgRange(3, 3);
        this.addKey("privileges user perm set");
        this.addKey("priv user perm set");
        this.addKey("pu perm set");
        this.addKey("pup set");
        this.addKey("pups");
        this.setPermission("privileges.user.perm.set", "Allows this user to set permission nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        String user = (plugin.getUsers().getConfigurationSection("users." + args.get(0)) != null ? args.get(0) : null);
        if (user == null) {
            sender.sendMessage("I don't know about that user.");
            return;
        }
        String node = args.get(1);
        String world = null;
        boolean val = Boolean.valueOf(args.get(2));
        if (node.contains(":")) {
            try {
                world = node.split(":")[0];
                node = node.split(":")[1];
                if (plugin.getServer().getWorld(world) == null) {
                    sender.sendMessage("Invalid world.");
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                sender.sendMessage("Invalid node string.");
                return;
            }
        }
        if (node.equalsIgnoreCase("privileges.self.edit") && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "Only the console can set that node.");
            return;
        }
        List<String> nodes;
        if (world == null) {
            nodes = plugin.getUserNode(user).getStringList("permissions");
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getUserNode(user).set("permissions", nodes);
        } else {
            nodes = plugin.getUserNode(user).getStringList("worlds." + world);
            nodes.remove(node);
            nodes.remove("-" + node);
            nodes.add((val ? "" : "-") + node);
            plugin.getUserNode(user).set("worlds." + world, nodes);
        }
        plugin.saveUsers();
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, node) + "' is now " + (val ? ChatColor.GREEN : ChatColor.RED) + val + ChatColor.WHITE + " for " + user + (world == null ? "" : " on " + ChatColor.GREEN + world));
        sender.sendMessage("When you're done editing permissions, run: " + ChatColor.GREEN + "/priv reload");
    }

}
