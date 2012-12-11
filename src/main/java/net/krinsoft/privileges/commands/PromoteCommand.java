package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class PromoteCommand extends GroupCommand {
    
    public PromoteCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: Promote");
        this.setCommandUsage("/promote [user]");
        this.setArgRange(1, 1);
        this.addKey("privileges promote");
        this.addKey("priv promote");
        this.addKey("promote");
        this.setPermission("privileges.promote", "Allows this user to promote other users.", PermissionDefault.OP);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(args.get(0));
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "The target '" + ChatColor.DARK_RED + args.get(0) + ChatColor.RED + "' doesn't exist.");
            return;
        }
        plugin.getGroupManager().promote(sender, target);
    }
}
