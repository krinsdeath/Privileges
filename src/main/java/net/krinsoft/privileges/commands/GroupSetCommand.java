package net.krinsoft.privileges.commands;

import java.util.List;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class GroupSetCommand extends GroupCommand {

    public GroupSetCommand(Privileges plugin) {
        super(plugin);
        this.setName("privileges group set");
        this.setCommandUsage("/privileges group set [player] [group]");
        this.setArgRange(2, 2);
        this.addKey("privileges group set");
        this.addKey("priv group set");
        this.addKey("pg set");
        this.addKey("pgs");
        this.setPermission("privileges.group.set", "Allows this user to change other users' groups.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        CommandSender target = plugin.getServer().getPlayer(args.get(0));
        if (target == null) { return; }
        int tRank = groupManager.getRank(target);
        int sRank = groupManager.getRank(sender);
        int gRank = 0;
        try {
            gRank = groupManager.getGroup(args.get(1)).getRank();
        } catch (NullPointerException e) {
            sender.sendMessage("That group does not exist.");
            return;
        }
        if (sRank <= tRank) {
            sender.sendMessage("You can't change that user's group.");
            return;
        }
        if (sRank <= gRank) {
            sender.sendMessage("That group's rank is too high for you to set.");
            return;
        }
        groupManager.setGroup(((Player) target).getName(), args.get(1));
    }

}
