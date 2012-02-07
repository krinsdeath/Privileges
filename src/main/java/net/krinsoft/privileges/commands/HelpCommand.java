package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.FancyPage;
import net.krinsoft.privileges.Privileges;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author krinsdeath
 */
public class HelpCommand extends PrivilegesCommand {
    private boolean hide;
    
    public HelpCommand(Privileges plugin) {
        super(plugin);
        setName("Privileges: Help");
        setCommandUsage("/help [plugin] [page]");
        addCommandExample("/help Privileges 2 -- Show help page 2 for all Privileges commands.");
        addCommandExample("/help -- Show a list of plugins that you can get help for.");
        setArgRange(0, 2);
        addKey("privileges help");
        addKey("priv help");
        addKey("help");
        addKey("h");
        setPermission("privileges.help", "Allows this user to view their available commands.", PermissionDefault.TRUE);
        hide = plugin.getConfig().getBoolean("help.hide_noperms", true);
    }
    
    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<String> lines = new ArrayList<String>();
        List<Plugin> plugins = Arrays.asList(plugin.getServer().getPluginManager().getPlugins());
        for (Plugin p : plugins) {
            lines.add("/help " + p.getDescription().getName());
        }
        String target = "Privileges";
        int pageNum;
        FancyPage pList = new FancyPage(lines);
        try {
            pageNum = Integer.parseInt(args.get(0));
        } catch (Exception e) {
            pageNum = 0;
        }
        if (args.size() == 0 || (args.size() == 1 && pageNum != 0)) {
            pageNum = (pageNum > 0 ? pageNum - 1 : pageNum);
            sender.sendMessage("=== Help -- Page " + (pageNum+1) + "/" + (pList.getPages()+1) + " ===");
            for (String line : pList.getPage(pageNum)) {
                sender.sendMessage(line);
            }
            return;
        }
        if (args.size() == 1) {
            try {
                pageNum = Integer.parseInt(args.get(0))-1;
            } catch (Exception e) {
                pageNum = 0;
                target = args.get(0);
            }
        } else if (args.size() == 2) {
            try {
                target = args.get(0);
                pageNum = Integer.parseInt(args.get(1))-1;
            } catch (Exception e) {
                pageNum = 0;
            }
        }
        Plugin p = plugin.getServer().getPluginManager().getPlugin(target);
        if (p == null) {
            p = plugin;
        }
        lines.clear();
        for (Command com : PluginCommandYamlParser.parse(p)) {
            String perm = (com.getPermission() != null ?
                    " (" + ChatColor.GREEN + com.getPermission() + ChatColor.BLUE + ")" :
                    "");
            if (!perm.isEmpty() && !sender.hasPermission(com.getPermission())) {
                continue;
            }
            if (perm.isEmpty() && hide) {
                continue;
            }
            lines.add(ChatColor.BLUE + com.getName() + perm + ChatColor.WHITE + ": " + com.getDescription());
            if (!com.getAliases().isEmpty()) {
                lines.add(" -- " + ChatColor.GREEN + "Aliases" + ChatColor.WHITE + ": " + ChatColor.AQUA + com.getAliases().toString());
            }
        }
        FancyPage cList = new FancyPage(lines);
        sender.sendMessage("=== " + p.getDescription().getName() + " -- Page " + (pageNum+1) + "/" + (cList.getPages()+1) + " ===");
        for (String line : cList.getPage(pageNum)) {
            sender.sendMessage(line);
        }
    }
}
