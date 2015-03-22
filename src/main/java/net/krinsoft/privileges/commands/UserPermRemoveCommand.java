package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.players.Player;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.UUID;

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
        UUID test;
        if (player != null) {
            test = player.getUniqueId();
        } else {
            test = null;
        }
        UUID user = (plugin.getUserNode(test) != null ? test : null);
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
        StringBuilder msg = new StringBuilder("Node ").append(colorize(ChatColor.GREEN, param[0])).append(" has been ").append(colorize(ChatColor.RED, "removed")).append(" from the user ");
        msg.append(colorize(ChatColor.GOLD, String.valueOf(user)));
        if (param[1] != null) {
            msg.append(" on ").append(colorize(ChatColor.AQUA, param[1]));
        }
        msg.append(".");
        sender.sendMessage(msg.toString());
        plugin.log(">> " + sender.getName() + ": " + user + "'s node '" + param[0] + "' has been removed" + (param[1] != null ? " on '" + param[1] + "'" : ""));
        reload(sender);
    }

}