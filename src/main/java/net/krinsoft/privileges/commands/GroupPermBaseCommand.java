package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */
public class GroupPermBaseCommand extends GroupPermCommand {

    public GroupPermBaseCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Group Perm");
        setCommandUsage("/priv group perm");
        addCommandExample(ChatColor.GREEN + "/pgp" + ChatColor.AQUA + "set    " + ChatColor.GOLD + "[group] [world:]node [bool]");
        addCommandExample(ChatColor.GREEN + "/pgp" + ChatColor.AQUA + "remove " + ChatColor.GOLD + "[group] [world:]node");
        addKey("privileges group perm");
        addKey("priv group perm");
        addKey("pgroup perm");
        addKey("pg perm");
        addKey("pgp");
        setPermission("privileges.group.perm", "The following commands all edit group permission nodes. Try a command to see usage examples.", PermissionDefault.TRUE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        showHelp(sender);
    }
}
