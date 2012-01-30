package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class VersionCommand extends PrivilegesCommand {

    public VersionCommand(Privileges plugin) {
        super(plugin);
        this.setName("Privileges: Version");
        this.setCommandUsage("/priv version");
        this.setArgRange(0, 0);
        this.addKey("privileges version");
        this.addKey("priv version");
        this.addKey("pv");
        this.setPermission("privileges.version", "Allows this user to check Privileges' version and build numbers.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        sender.sendMessage("Privileges version: " + ChatColor.GREEN + plugin.getDescription().getVersion());
        sender.sendMessage("By: " + ChatColor.GREEN + "krinsdeath");
    }

}
