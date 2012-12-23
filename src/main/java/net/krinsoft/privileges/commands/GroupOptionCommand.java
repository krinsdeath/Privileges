package net.krinsoft.privileges.commands;

import net.krinsoft.privileges.Privileges;
import net.krinsoft.privileges.groups.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

/**
 * @author krinsdeath
 */

enum Action {
    ADD,
    RM,
    SET;

    static Action fromName(String name) {
        if (name == null) { return null; }
        for (Action val : values()) {
            if (val.name().equals(name.toUpperCase())) {
                return val;
            }
        }
        return null;
    }
}

enum Option {
    RANK,
    INHERITANCE,
    PROMOTION,
    DEMOTION;

    static Option fromName(String name) {
        if (name == null) { return null; }
        for (Option val : values()) {
            if (val.name().equals(name.toUpperCase())) {
                return val;
            }
        }
        return null;
    }
}
public class GroupOptionCommand extends GroupCommand {

    public GroupOptionCommand(Privileges instance) {
        super(instance);
        setName("Privileges: Group Option");
        setCommandUsage("/pg option [group] [action] [option] [value]");
        addCommandExample(ChatColor.GREEN + "/pg option" + ChatColor.AQUA + " owner" + ChatColor.GOLD + " add inheritance" + ChatColor.YELLOW + " admin");
        addCommandExample(ChatColor.GREEN + "/pg option" + ChatColor.AQUA + " admin" + ChatColor.GOLD + " set rank" + ChatColor.YELLOW + " 10");
        addCommandExample(ChatColor.GREEN + "/pg option" + ChatColor.AQUA + " user" + ChatColor.GOLD + " rm inheritance" + ChatColor.YELLOW + " default");
        setArgRange(4, 4);
        addKey("privileges group option");
        addKey("priv group option");
        addKey("pgroup option");
        addKey("pg option");
        setPermission("privileges.group.option", "Allows the setting of various group options.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Group g = plugin.getGroupManager().getGroup(args.get(0));
        if (g == null) {
            sender.sendMessage(ChatColor.RED + "That group does not exist.");
            return;
        }
        if (!plugin.getGroupManager().checkRank(sender, g.getRank())) {
            sender.sendMessage(ChatColor.RED + "That group's rank is too high.");
            return;
        }
        Action a = Action.fromName(args.get(1));
        if (a == null) {
            sender.sendMessage(ChatColor.RED + "Unknown action: " + args.get(1));
            return;
        }
        Option o = Option.fromName(args.get(2));
        if (o == null) {
            sender.sendMessage(ChatColor.RED + "Unknown option: " + args.get(2));
            return;
        }
        ConfigurationSection group = plugin.getGroupNode(g.getName());
        switch (o) {
            case RANK:
                if (a == Action.RM) {
                    sender.sendMessage(ChatColor.RED + "Invalid action for RANK option.");
                    return;
                }
                try {
                    int rank = Integer.parseInt(args.get(3));
                    group.set("rank", rank);
                    reload(sender);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Rank must be a number.");
                    return;
                }
                break;
            case INHERITANCE:
                List<String> tree = group.getStringList("inheritance");
                Group parent = plugin.getGroupManager().getGroup(args.get(3));
                if (parent == null) {
                    sender.sendMessage(ChatColor.RED + "That group does not exist.");
                    return;
                }
                switch (a) {
                    case ADD:
                        if (!tree.contains(parent.getName())) {
                            tree.add(parent.getName());
                        }
                        break;
                    case RM:
                        tree.remove(parent.getName());
                        break;
                    case SET:
                        tree.clear();
                        tree.add(parent.getName());
                        break;
                    default:
                        break;
                }
                group.set("inheritance", tree);
                break;
            case PROMOTION:
                switch (a) {
                    case ADD:
                        group.set("data.promotion", args.get(3));
                        break;
                    case SET:
                        group.set("data.promotion", args.get(3));
                        break;
                    case RM:
                        group.set("data.promotion", null);
                        break;
                    default:
                        break;
                }
                break;
            case DEMOTION:
                switch (a) {
                    case ADD:
                        group.set("data.demotion", args.get(3));
                        break;
                    case SET:
                        group.set("data.demotion", args.get(3));
                        break;
                    case RM:
                        group.set("data.demotion", null);
                        break;
                    default:
                        break;
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "No such option: " + ChatColor.DARK_RED + args.get(2));
                return;
        }
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GREEN).append("The value ").append(ChatColor.AQUA).append(args.get(3)).append(ChatColor.GREEN).append(" has been ").append(ChatColor.DARK_AQUA);
        switch (a) {
            case ADD:
                message.append("added").append(ChatColor.GREEN).append(" to ");
                break;
            case SET:
                message.append("set").append(ChatColor.GREEN).append(" to ");
                break;
            case RM:
                message.append("removed").append(ChatColor.GREEN).append(" from ");
                break;
            default:
                break;
        }
        message.append(ChatColor.GOLD).append(g.getName()).append(ChatColor.GREEN).append("'s ").append(ChatColor.AQUA);
        switch (o) {
            case RANK:
                message.append("rank").append(ChatColor.GREEN).append(".");
                break;
            case INHERITANCE:
                message.append("inheritance list").append(ChatColor.GREEN).append(".");
                break;
            case PROMOTION:
                message.append("promotion group").append(ChatColor.GREEN).append(".");
                break;
            case DEMOTION:
                message.append("demotion group").append(ChatColor.GREEN).append(".");
                break;
            default:
                break;
        }
        sender.sendMessage(message.toString());
        reload(sender);
    }
}
