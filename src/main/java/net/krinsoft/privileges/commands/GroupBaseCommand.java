package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class GroupBaseCommand extends GroupCommand {

    public GroupBaseCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Group");
        setCommandUsage("/priv group");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "create " + ChatColor.GOLD       + " [group] [rank]");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "list");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "perm   " + ChatColor.RED        + " set      " + ChatColor.GOLD + "[node] [value]");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "perm   " + ChatColor.RED        + " remove   " + ChatColor.GOLD + "[node]");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "remove " + ChatColor.GOLD       + " [group]  ");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "rename " + ChatColor.GOLD       + " [group]  [name]");
        addCommandExample(ChatColor.GREEN + "/pg " + ChatColor.AQUA + "set    " + ChatColor.GOLD       + " [user]   [group]");
        setArgRange(0, 1);
        addKey("privileges group");
        addKey("priv group");
        addKey("pgroup");
        addKey("pg");
        setPermission("privileges.group", "The following commands all edit or change group settings. Try a command to see usage examples.", PermissionDefault.TRUE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        showHelp(sender);
    }

}
