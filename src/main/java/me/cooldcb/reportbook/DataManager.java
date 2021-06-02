package me.cooldcb.reportbook;

import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataManager implements Listener {
    ReportBook plugin;
    HashBiMap<UUID, String> playerList = HashBiMap.create();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPlayedBefore()) {
            playerList.put(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        }
    }

    public UUID nameToUUID(String name) {
        return playerList.inverse().get(name);
    }

    public HashBiMap<UUID, String> getPlayerList() {
        return this.playerList;
    }

    public DataManager(ReportBook instance) {
        plugin = instance;
        OfflinePlayer[] offlinePlayerList = Bukkit.getOfflinePlayers();
        for (OfflinePlayer offlinePlayer : offlinePlayerList) {
            if (playerList.containsValue(offlinePlayer.getName())) continue;
            playerList.put(offlinePlayer.getUniqueId(), offlinePlayer.getName());
        }
    }

    public void getReport(String uuid, CommandSender sender, String reportedUser) {
        File reports = new File(plugin.getDataFolder(),"reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);

        if (!reportYAML.contains(uuid)) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7This user has not got any reports.");
            return;
    }

        Map<String, Object> yamlValues = reportYAML.getConfigurationSection(uuid).getValues(false);
        Set<Map.Entry<String, Object>> userReports = yamlValues.entrySet();

        sender.sendMessage("§8§l[§d§lES§8§l] §7Reports against §d" + reportedUser + "§7:");
        for (Map.Entry<String, Object> report : userReports) {
            ConfigurationSection currReport = (ConfigurationSection) report.getValue();
            String index = report.getKey();
            String reporter = currReport.getString("reporter");
            String category = currReport.getString("category");
            String date = currReport.getString("date");
            String status = currReport.getString("status");
            if (status.toLowerCase().equals("open")) {
                status = "§a" + status;
            } else if (status.toLowerCase().equals("closed")) {
                status = "§c" + status;
            } else {
                status = "§6" + status;
            }
            sender.sendMessage("§8§l[§d§l" + index + "§8§l] \n§8- §7Reporter: " + reporter + "\n§8- §7Category: " + category + "\n§8- §7Date/Time: " + date + "\n§8- §7Status: " + status);
        }
    }

    private ConfigurationSection getSectionOrCreate(ConfigurationSection configurationSection, String path) {
        if (configurationSection.contains(path)) {
            return configurationSection.getConfigurationSection(path);
        } else {
            return configurationSection.createSection(path);
        }
    }

    public void writeToYML(String reportedUser, String reportedFor, String sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                File reports = new File(plugin.getDataFolder(),"reports.yml");
                YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);

                ConfigurationSection section = getSectionOrCreate(reportYAML, reportedUser);

                Map<String, Object> sectionValues = section.getValues(false);
                int reportInt = sectionValues.size();
                reportInt += 1;

                String reportNum = Integer.toString(reportInt);

                ConfigurationSection currentReportSection = section.createSection(reportNum);

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                String date = dtf.format(now);

                currentReportSection.set("reporter", sender);
                currentReportSection.set("category", reportedFor);
                currentReportSection.set("date", date);
                currentReportSection.set("status", "open");
                try {
                    reportYAML.save(reports);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
