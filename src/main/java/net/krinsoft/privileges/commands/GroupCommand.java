package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.GroupManager;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupCommand extends PrivilegesCommand {

    protected GroupManager groupManager;

    public GroupCommand(Privileges plugin) {
        super(plugin);
        this.plugin = (Privileges) plugin;
        this.groupManager = plugin.getGroupManager();
        this.setName("privileges group");
        this.setCommandUsage("/privileges group");
        this.setArgRange(0, 0);
        this.addKey("privileges group");
        this.addKey("priv group");
        this.addKey("pg");
        this.setPermission("privileges.group", "Allows the user to use '/perm group' commands.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        
    }

}
