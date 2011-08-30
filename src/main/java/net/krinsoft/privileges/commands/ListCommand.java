package net.krinsoft.privileges.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.krinsoft.privileges.FancyPage;
import net.krinsoft.privileges.Privileges;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;

/**
 *
 * @author krinsdeath
 */
public class ListCommand extends PrivilegesCommand {

    public ListCommand(Privileges plugin) {
        super(plugin);
        this.plugin = (Privileges) plugin;
        this.setName("privileges list");
        this.setCommandUsage("/privileges list ([player] [page])");
        this.setArgRange(0, 2);
        this.addKey("privileges list");
        this.addKey("priv list");
        this.setPermission("privileges.list", "Allows this user to use '/perm list'", PermissionDefault.FALSE);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        CommandSender target = sender;
        String name = "Console";
        int pageNum = 0;
        List<String> list = new ArrayList<String>();
        if (args.size() == 1) {
            try {
                pageNum = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                pageNum = 0;
                if (plugin.getServer().getPlayer(args.get(0)) != null) {
                    target = plugin.getServer().getPlayer(args.get(0));
                    name = ((Player)target).getName();
                }
            }
        } else if (args.size() == 2) {
            try {
                pageNum = Integer.parseInt(args.get(1));
                if (plugin.getServer().getPlayer(args.get(0)) != null) {
                    target = plugin.getServer().getPlayer(args.get(0));
                    name = ((Player)target).getName();
                }
            } catch (NumberFormatException e) {
                pageNum = 0;
            }
        }
        List<PermissionAttachmentInfo> attInfo = new ArrayList<PermissionAttachmentInfo>(target.getEffectivePermissions());
        Collections.sort(attInfo, new Comparator<PermissionAttachmentInfo>() {
            public int compare(PermissionAttachmentInfo a, PermissionAttachmentInfo b) {
                return a.getPermission().compareTo(b.getPermission());
            }
        }); // thanks SpaceManiac!
        for (PermissionAttachmentInfo att : attInfo) {
            String node = att.getPermission();
            String msg = "";
            msg = "&B" + node + "&A - &B" + att.getValue() + "&A ";
            msg = msg + "&A(" + (att.getAttachment() != null ? "set: &6" + att.getAttachment().getPlugin().getDescription().getName() + "&A" : "&3default&A") + ")";
            list.add(msg.replaceAll("(?i)&([0-F])", "\u00A7$1"));
        }
        FancyPage page = new FancyPage(list);
        String header = "";
        if (sender instanceof ConsoleCommandSender) {
            header = "\u00A7A=== \u00A7BPermissions list for  " + name + " \u00A7A===";
            sender.sendMessage(header);
            for (String line : list) {
                sender.sendMessage(line);
            }
        } else {
            header = "\u00A7A=== \u00A7BPage " + pageNum + "\u00A7A/\u00A7B" + page.getPages() + " \u00A7A===";
            sender.sendMessage(header);
            for (String line : page.getPage(pageNum)) {
                sender.sendMessage(line);
            }
        }
    }

}
