package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.FancyPage;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
public class ListCommand extends PrivilegesCommand {

    public ListCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: List");
        setCommandUsage("/privileges list ([player] [page])");
        setArgRange(0, 2);
        addKey("privileges list");
        addKey("priv list");
        addKey("plist");
        setPermission("privileges.list", "Lists the specified user's permissions nodes.", PermissionDefault.TRUE);
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
                CommandSender test = plugin.getServer().getPlayer(args.get(0));
                if (test != null) {
                    target = test;
                    name = test.getName();
                }
            }
        } else if (args.size() == 2) {
            try {
                pageNum = Integer.parseInt(args.get(1));
                CommandSender test = plugin.getServer().getPlayer(args.get(0));
                if (test != null) {
                    target = test;
                    name = test.getName();
                }
            } catch (NumberFormatException e) {
                pageNum = 0;
            }
        }
        if (!target.equals(sender) && !sender.hasPermission("privileges.list.other")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to view other peoples' nodes.");
            return;
        }
        List<PermissionAttachmentInfo> attInfo = new ArrayList<PermissionAttachmentInfo>(target.getEffectivePermissions());
        Collections.sort(attInfo, new Comparator<PermissionAttachmentInfo>() {
            public int compare(PermissionAttachmentInfo a, PermissionAttachmentInfo b) {
                return a.getPermission().compareTo(b.getPermission());
            }
        }); // thanks SpaceManiac!
        for (PermissionAttachmentInfo att : attInfo) {
            String node = att.getPermission();
            StringBuilder msg = new StringBuilder();
            msg.append("&B").append(node).append("&A - &B").append(att.getValue()).append("&A ");
            msg.append("&A(").append(att.getAttachment() != null ? "set: &6" + att.getAttachment().getPlugin().getDescription().getName() + "&A" : "&3default&A").append(")");
            list.add(ChatColor.translateAlternateColorCodes('&', msg.toString()));
        }
        FancyPage page = new FancyPage(list);
        String header;
        if (sender instanceof ConsoleCommandSender) {
            header = ChatColor.GREEN + "=== " + ChatColor.WHITE + "Permissions list for " + ChatColor.AQUA + name + ChatColor.GREEN + " ===";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
            for (String line : list) {
                sender.sendMessage(ChatColor.stripColor(line));
            }
        } else {
            header = ChatColor.GREEN + "=== " + ChatColor.WHITE + " [Page " + pageNum + "/" + page.getPages() + "] " + ChatColor.GREEN + "===";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
            for (String line : page.getPage(pageNum)) {
                sender.sendMessage(line);
            }
        }
    }

}
