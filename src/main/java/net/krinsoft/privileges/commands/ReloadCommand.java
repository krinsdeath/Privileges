package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class ReloadCommand extends PrivilegesCommand {

    public ReloadCommand(Privileges plugin) {
        // super constructor stuff
        super(plugin);
        this.plugin = (Privileges) plugin;
        // command stuff
        this.setName("privileges reload");
        this.setCommandUsage("/perm reload");
        this.setArgRange(0, 0);
        this.addKey("privileges reload");
        this.addKey("permissions reload");
        this.addKey("perms reload");
        this.addKey("perm reload");
        this.setPermission("privileges.reload", "Allows this user to access '/perm reload'", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        plugin.registerPermissions();
    }

}
