package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;
import java.util.Set;

/**
 * @author krinsdeath
 */
public class GroupListCommand extends GroupCommand {
    
    public GroupListCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Group List");
        setCommandUsage("/priv group list");
        setArgRange(0, 0);
        addKey("privileges group list");
        addKey("priv group list");
        addKey("pgroup list");
        addKey("pg list");
        addKey("pgroupl");
        addKey("pgl");
        setPermission("privileges.group.list", "Allows this user to list all groups", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        StringBuilder line = new StringBuilder();
        Set<String> groups = plugin.getGroups().getConfigurationSection("groups").getKeys(false);
        for (String group : groups) {
            if (line.length() > 0) { line.append(", "); }
            Group g = plugin.getGroupManager().getGroup(group);
            line.append(g.getName()).append(" (").append(g.getRank()).append(")");
        }
        sender.sendMessage(line.toString());
    }
}
