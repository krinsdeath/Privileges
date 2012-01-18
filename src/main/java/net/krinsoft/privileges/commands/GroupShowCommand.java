package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupShowCommand extends GroupCommand {

    public GroupShowCommand(Privileges plugin) {
        super(plugin);
        this.setName("privileges group show");
        this.setCommandUsage("/privileges group show [target]");
        this.addCommandExample("/priv group show Player -- Show you Player's group");
        this.addCommandExample("/pg show -- Print out your own group.");
        this.setArgRange(0, 1);
        this.addKey("privileges group show");
        this.addKey("priv group show");
        this.addKey("pg show");
        this.setPermission("privileges.group.show", "Allows you to view other players' groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group group = null;
        String target = null;
        if (args.isEmpty() && !(sender instanceof Player)) {
            sender.sendMessage("You must supply a target as the console.");
            return;
        }
        if (args.isEmpty()) {
            group = groupManager.getGroup((Player) sender);
            target = "Your";
        } else {
            group = groupManager.getGroup(plugin.getServer().getPlayer(args.get(0)));
            target = plugin.getServer().getPlayer(args.get(0)).getName() + "'s";
        }
        sender.sendMessage(ChatColor.GREEN + target + ChatColor.WHITE + "'s group is: " + group.getName());
    }

}
