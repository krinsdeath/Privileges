package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.players.Player;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class UserPermRemoveCommand extends UserPermCommand {

    public UserPermRemoveCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: User Perm Remove");
        this.setCommandUsage("/privileges user perm remove [user] [world:]node");
        this.addCommandExample("/priv user perm remove Player example.node");
        this.addCommandExample("/pups Player world:example.node");
        this.setArgRange(2, 2);
        this.addKey("privileges user perm remove");
        this.addKey("priv user perm remove");
        this.addKey("pu perm remove");
        this.addKey("pup remove");
        this.addKey("pupr");
        this.setPermission("privileges.user.perm.remove", "Allows this user to remove permissions nodes.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(args.get(0));
        String test;
        if (player != null) {
            test = player.getName();
        } else {
            test = args.get(0);
        }
        String user = (plugin.getUsers().getConfigurationSection("users." + test) != null ? test : null);
        if (user == null) {
            sender.sendMessage("I don't know about that user.");
            return;
        }
        String[] param = validateNode(args.get(1));
        if (param == null) {
            showHelp(sender);
            return;
        }
        Player priv = plugin.getPlayerManager().getPlayer(test);
        priv.removePermission(param[1], param[0]);
        priv.removePermission(param[1], "-" + param[0]);
        sender.sendMessage("Node '" + colorize(ChatColor.GREEN, param[0]) + "' has been removed from " + user + (param[1] == null ? "" : " on " + ChatColor.GREEN + param[1]));
        plugin.log(">> " + sender.getName() + ": " + user + "'s node '" + param[0] + "' has been removed.");
        reload(sender);
    }

}