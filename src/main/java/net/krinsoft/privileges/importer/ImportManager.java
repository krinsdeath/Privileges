package net.krinsoft.privileges.importer;

import net.krinsoft.privileges.Privileges;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author krinsdeath
 */
public class ImportManager {
    private Privileges plugin;

    public ImportManager(Privileges plugin) {
        this.plugin = plugin;
        importPermissionsBukkit();
        //locatePermissions3();
    }

    private void importPermissionsBukkit() {
        File configFile = new File("plugins/PermissionsBukkit/config.yml");
        if (configFile.exists()) {
            long importer = System.currentTimeMillis();
            plugin.log("Beginning PermissionsBukkit configuration import!");
            FileConfiguration config = new YamlConfiguration();
            config.options().pathSeparator('/');
            try {
                config.load(configFile);
            } catch (IOException e) {
                return;
            } catch (InvalidConfigurationException e) {
                return;
            }
            /////////////////////////
            // group import start
            // get the whole group section
            List<String> permSet = new ArrayList<String>();
            int importedGroups = 0;
            ConfigurationSection groupSection = config.getConfigurationSection("groups");
            String header;
            if (groupSection != null) {
                for (String group : groupSection.getKeys(false)) {
                    header = "[" + group + "] Importing (permissions): ";
                    // permissions section
                    ConfigurationSection groupPerms = groupSection.getConfigurationSection(group + "/permissions");
                    permSet = new ArrayList<String>();
                    ConfigurationSection privGroup = plugin.getGroupNode(group);
                    if (privGroup != null) {
                        permSet.addAll(privGroup.getStringList("permissions"));
                    }
                    if (groupPerms != null) {
                        for (String node : groupPerms.getKeys(false)) {
                            boolean val = groupPerms.getBoolean(node);
                            if (node.equalsIgnoreCase("permissions.build")) {
                                node = "privileges.build";
                            } else if (node.equalsIgnoreCase("permissions.*")) {
                                node = "privileges.*";
                            }
                            if (!val) {
                                node = "-"+node;
                            }
                            permSet.add(node);
                            plugin.debug(header + node);
                        }
                        plugin.debug(permSet.toString());
                        plugin.getGroups().set("groups." + group + ".permissions", permSet);
                    }
                    // worlds section
                    ConfigurationSection groupWorlds = groupSection.getConfigurationSection(group + "/worlds");
                    if (groupWorlds != null) {
                        for (String world : groupWorlds.getKeys(false)) {
                            permSet = new ArrayList<String>();
                            ConfigurationSection privGroupWorld = plugin.getGroups().getConfigurationSection("groups." + group + ".worlds");
                            if (privGroupWorld != null) {
                                permSet.addAll(privGroupWorld.getStringList(world));
                            }
                            header = "[" + group + "] Importing (worlds." + world + "): ";
                            ConfigurationSection groupWorldKey = groupWorlds.getConfigurationSection(world);
                            if (groupWorldKey != null) {
                                for (String node : groupWorldKey.getKeys(false)) {
                                    boolean val = groupWorlds.getBoolean(world + "/" + node);
                                    if (node.equalsIgnoreCase("permissions.build")) {
                                        node = "privileges.build";
                                    } else if (node.equalsIgnoreCase("permissions.*")) {
                                        node = "privileges.*";
                                    }
                                    if (!val) {
                                        node = "-"+node;
                                    }
                                    permSet.add(node);
                                    plugin.debug(header + node);
                                }
                            }
                            plugin.getGroups().set("groups." + group + ".worlds." + world, permSet);
                        }
                    }
                    importedGroups++;
                }
            }
            plugin.saveGroups();
            plugin.log("Imported " + importedGroups + " groups!");
            // groups importing done!
            /////////////////////////
            // user import start
            // get the whole users section
            List<String> groups = new ArrayList<String>();
            List<String> permList = new ArrayList<String>();
            int importedUsers = 0;
            ConfigurationSection userSection = config.getConfigurationSection("users");
            if (userSection != null) {
                // user section not null, iterate through users
                for (String username : userSection.getKeys(false)) {
                    username = username.toLowerCase();
                    groups.clear();
                    groups.addAll(userSection.getStringList(username + "/groups"));
                    String group;
                    try {
                        group = plugin.getGroupManager().getGroup(groups.get(0)).getName();
                    } catch (Exception e) {
                        group = plugin.getConfig().getString("default_group", "default");
                    }
                    plugin.getUsers().set("users." + username + ".group", group);
                    plugin.debug("[" + username + "] Set group to '" + group + "'");
                    ConfigurationSection userPerms = userSection.getConfigurationSection(username + "/permissions");
                    permList = new ArrayList<String>();
                    /////////////////////////
                    // permissions section
                    permList.addAll(plugin.getUsers().getStringList("users." + username + ".permissions"));
                    if (userPerms != null) {
                        header = "[" + username + "] Importing (permissions): ";
                        // user has custom permissions set
                        for (String node : userPerms.getKeys(false)) {
                            boolean val = userPerms.getBoolean(node);
                            if (node.equalsIgnoreCase("permissions.*")) {
                                node = "privileges.*";
                            } else if (node.equalsIgnoreCase("permissions.build")) {
                                node = "privileges.build";
                            }
                            if (!val) {
                                node = "-" + node;
                            }
                            permList.add(node);
                            plugin.debug(header + node);
                        }
                    }
                    plugin.getUsers().set("users." + username + ".permissions", permList);
                    // end permissions section
                    /////////////////////////
                    // worlds section
                    ConfigurationSection userWorlds = userSection.getConfigurationSection(username + "/worlds");
                    if (userWorlds != null) {
                        // user has custom world permissions set
                        for (String world : userWorlds.getKeys(false)) {
                            header = "[" + username + "] Importing (worlds." + world + "): ";
                            permList = new ArrayList<String>();
                            permList.addAll(plugin.getUsers().getStringList("users." + username + ".worlds." + world));
                            for (String node : userWorlds.getConfigurationSection(world).getKeys(false)) {
                                boolean val = userWorlds.getBoolean(world +"/" + node);
                                if (node.equalsIgnoreCase("permissions.*")) {
                                    node = "privileges.*";
                                } else if (node.equalsIgnoreCase("permissions.build")) {
                                    node = "privileges.build";
                                }
                                if (!val) {
                                    node = "-" + node;
                                }
                                permList.add(node);
                                plugin.debug(header + node);
                            }
                            plugin.getUsers().set("users." + username + ".worlds." + world, permList);
                        }
                    }
                    importedUsers++;
                    // end worlds section
                    /////////////////////////
                }
            }
            plugin.saveUsers();
            plugin.log("Imported " + importedUsers + " users!");
            // users importing done!
            /////////////////////////
            // rename the config file to prevent multiple imports
            configFile.renameTo(new File("plugins/PermissionsBukkit/config-imported.yml"));
            plugin.log("PermissionsBukkit configuration import complete. (" + (System.currentTimeMillis() - importer) + "ms)");
            plugin.log("Please check plugins/Privileges/groups.yml to configure your group ranks.");
        }
    }
}
