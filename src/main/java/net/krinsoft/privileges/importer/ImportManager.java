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


    //////////////////////////////

    /**
     * @deprecated since 1.4
     */
    @Deprecated
    private void locatePermissionsBukkit() {
        File file = new File("plugins/PermissionsBukkit/config.yml");
        if (file.exists()) {
            long imp = System.currentTimeMillis();
            plugin.log("Beginning PermissionsBukkit configuration import...");
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection groups = config.getConfigurationSection("groups");
            for (String group : groups.getKeys(false)) {
                long time = System.currentTimeMillis();
                // create the group if it doesn't exist
                plugin.buildGroup(group);
                ConfigurationSection node = plugin.getGroupNode(group);
                node.set("rank", node.getInt("rank", 0));
                List<String> globals = node.getStringList("permissions");
                Set<String> tmp = new LinkedHashSet<String>();
                if (globals == null) { globals = new ArrayList<String>(); }
                tmp.addAll(globals);
                String permKey = group + ".permissions";
                // iterate through global 'permissions'
                for (Map.Entry<String, Object> key : groups.getConfigurationSection(permKey).getValues(true).entrySet()) {
                    if (key.getValue() instanceof Boolean) {
                        tmp.add(((Boolean) key.getValue() ? key.getKey() : "-" + key.getKey()));
                        plugin.debug(key.getKey() + ": " + key.getValue());
                    }
                }
                node.set("permissions", tmp);
                // iterate through worlds
                if (groups.getConfigurationSection(group + ".worlds") != null) {
                    for (String world : groups.getConfigurationSection(group + ".worlds").getKeys(false)) {
                        String worldKey = group + ".worlds." + world;
                        List<String> worldNodes = node.getStringList("worlds." + world);
                        tmp = new LinkedHashSet<String>();
                        if (worldNodes == null) { worldNodes = new ArrayList<String>(); }
                        tmp.addAll(worldNodes);
                        // iterate through world keys for individual nodes
                        for (Map.Entry<String, Object> key : groups.getConfigurationSection(worldKey).getValues(true).entrySet()) {
                            if (key.getValue() instanceof Boolean) {
                                tmp.add(((Boolean) key.getValue() ? key.getKey() : "-" + key.getKey()));
                                plugin.debug(key.getKey() + ": " + key.getValue());
                            }
                        }
                        node.set("worlds." + world, tmp);
                    }
                }
                plugin.log("Imported " + group + " in " + (System.currentTimeMillis() - time) + "ms");
                plugin.saveGroups();
            }
            file.getParentFile().renameTo(new File("plugins/PermissionsBukkit_OLD"));
            //new File("plugins/PermissionsBukkit.jar").renameTo(new File("plugins/PermissionsBukkit.jar.old"));
            plugin.log("PermissionsBukkit import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.log("You can now delete PermissionsBukkit.");
            plugin.log("Please view plugins/Privileges/groups.yml, and edit the ranks of the imported groups.");
        }
    }

    private void locatePermissions3() {
        File file = new File("plugins/Permissions");
        if (file.exists()) {
            File[] files = file.listFiles();
            long imp = System.currentTimeMillis();
            plugin.log("Beginning Permissions 3 configuration import...");
            File gGroups = new File("plugins/Permissions/globalGroups.yml");
            if (gGroups.exists()) {
                FileConfiguration conf = YamlConfiguration.loadConfiguration(gGroups);
                if (conf.getKeys(false) != null) {
                    for (String group : conf.getKeys(false)) {
                        plugin.buildGroup(group);
                        ConfigurationSection node = conf.getConfigurationSection("groups." + group);
                        List<String> globals = node.getStringList("permissions");
                        List<String> nodes = node.getStringList("permissions");
                        if (globals == null) { globals = new ArrayList<String>(); }
                        if (node.getBoolean("default", false)) {
                            plugin.log("Setting default group to '" + group + "'");
                            plugin.getConfig().set("default_group", group);
                        }
                        for (String n : nodes) {
                            globals.remove(n);
                            globals.remove("-" + n);
                            globals.add(n);
                        }
                        if (node.getBoolean("info.build", false)) {
                            globals.remove("privileges.build");
                            globals.remove("-privileges.build");
                            globals.add("privileges.build");
                        } else {
                            globals.remove("privileges.build");
                            globals.remove("-privileges.build");
                            globals.add("-privileges.build");
                        }
                        List<String> parents = node.getStringList("inheritance");
                        List<String> inherits = node.getStringList("inheritance");
                        for (String parent : inherits) {
                            parents.remove(parent);
                            parents.add(parent);
                        }
                        node.set("permissions", globals);
                        node.set("inheritance", parents);
                        node.set("rank", node.getInt("info.rank", 0));
                    }
                }
            }
            assert files != null;
            for (File folder : files) {
                if (folder.isDirectory()) {
                    File gConfig = new File("plugins/Permissions/" + folder.getName() + "/groups.yml");
                    if (gConfig.exists()) {
                        FileConfiguration conf = YamlConfiguration.loadConfiguration(gConfig);
                        ConfigurationSection groups = conf.getConfigurationSection("groups");
                        if (groups == null) { continue; }
                        for (String group : groups.getKeys(false)) {
                            long groupTIME = System.currentTimeMillis();
                            // create the group if it doesn't exist, and get the nodes for this world
                            plugin.buildGroup(group);
                            ConfigurationSection node = groups.getConfigurationSection(group);
                            List<String> worldNodes = node.getStringList("worlds." + folder.getName());
                            // get the list of permissions
                            List<String> nodes = node.getStringList("permissions");
                            for (String n : nodes) {
                                worldNodes.remove(n);
                                worldNodes.remove("-" + n);
                                worldNodes.add(n);
                            }
                            if (node.getBoolean("info.build", false)) {
                                worldNodes.remove("privileges.build");
                                worldNodes.remove("-privileges.build");
                                worldNodes.add("privileges.build");
                            } else {
                                worldNodes.remove("privileges.build");
                                worldNodes.remove("-privileges.build");
                                worldNodes.add("-privileges.build");
                            }
                            // get parent groups for this group
                            List<String> parents = node.getStringList("inheritance");
                            List<String> inherits = node.getStringList("inheritance");
                            for (String parent : inherits) {
                                parents.remove(parent);
                                parents.add(parent);
                            }
                            node.set("rank", node.getInt("info.rank", 0));
                            node.set("worlds." + folder.getName(), worldNodes);
                            node.set("inheritance", parents);
                            plugin.log("Imported '" + group + "' from Permissions 3 in " + (System.currentTimeMillis() - groupTIME) + "ms");
                        }
                    }
                }
            }
            file.renameTo(new File("plugins/Permissions_OLD"));
            plugin.log("Permissions 3 import finished in " + (System.currentTimeMillis() - imp) + "ms");
            plugin.log("Permissions 3 can now be deleted.");
            plugin.log("Please check plugin/Privileges/groups.yml. Some groups may have unset 'rank' keys.");
            plugin.saveGroups();
            plugin.saveConfig();
        }
    }
}
