package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupRemoveCommand extends PrivilegesCommand {

    public GroupRemoveCommand(Privileges plugin) {
        super(plugin);
        this.plugin = (Privileges) plugin;
        this.setName("privileges group remove");
        this.setCommandUsage("/privileges group remove [group] [--safe]");
        this.addCommandExample("/pgr ? -- show command help");
        this.addCommandExample("/pgr users --safe -- removes the 'users' group and deletes all inheritance references");
        this.addCommandExample("/pgr admin -- deletes the admin group, but leaves any inheritance references");
        this.setArgRange(1, 2);
        this.addKey("privileges group remove");
        this.addKey("priv group remove");
        this.addKey("pg remove");
        this.addKey("pgr");
        this.setPermission("privileges.group.remove", "Allows this user to remove groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        
    }

}
