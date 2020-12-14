package me.cooldcb.reportbook;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportsNotify implements CommandExecutor, Listener, TabCompleter {
    ReportBook plugin;

    public ReportsNotify(ReportBook instance) {
        plugin = instance;
    }

    public String getFriendlyName(String name) {
        for (String nameLoc : ReportBook.dataManager.getPlayerList().values()) {
            if (name.toLowerCase().equals(nameLoc.toLowerCase())) {
                return nameLoc;
            }
        }
        return "false";
    }

    public List<String> listReportedUsersOpen(CommandSender sender) {
        List<String> reportedPlayerList = new ArrayList<>();
        File reports = new File(plugin.getDataFolder(), "reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);
        Map<String, Object> reportedPlayers = reportYAML.getValues(false);


        for (Map.Entry<String, Object> reportedPlayer : reportedPlayers.entrySet()) {
            ConfigurationSection reportedPlayerSection = (ConfigurationSection) reportedPlayer.getValue();
            for (Map.Entry<String, Object> reportID : reportedPlayerSection.getValues(false).entrySet()) {
                ConfigurationSection reportIDSection = (ConfigurationSection) reportID.getValue();
                String reportedPlayerStr = reportedPlayer.getKey();
                UUID reportedPlayerUUID = UUID.fromString(reportedPlayerStr);
                String reportedPlayerName = Bukkit.getOfflinePlayer(reportedPlayerUUID).getName();
                reportedPlayerList.add(reportedPlayerName);
                break;
            }
        }
        return reportedPlayerList;
    }

    public void outputReportedUsersOpen(CommandSender sender) {
        File reports = new File(plugin.getDataFolder(), "reports.yml");
        YamlConfiguration reportYAML = YamlConfiguration.loadConfiguration(reports);
        Map<String, Object> reportedPlayers = reportYAML.getValues(false);
        int reportedNum = 0;
        String nameList = null;


        for (Map.Entry<String, Object> reportedPlayer : reportedPlayers.entrySet()) {
            ConfigurationSection reportedPlayerSection = (ConfigurationSection) reportedPlayer.getValue();
            for (Map.Entry<String, Object> reportID : reportedPlayerSection.getValues(false).entrySet()) {
                ConfigurationSection reportIDSection = (ConfigurationSection) reportID.getValue();
                if (reportIDSection.getString("status").equals("open")) {
                    String reportedPlayerStr = reportedPlayer.getKey();
                    UUID reportedPlayerUUID = UUID.fromString(reportedPlayerStr);
                    String reportedPlayerName = Bukkit.getOfflinePlayer(reportedPlayerUUID).getName();
                    if (reportedNum == 0) {
                        reportedNum = reportedNum + 1;
                        nameList = "§8- §7" + reportedPlayerName;
                        break;
                    }
                    reportedNum = reportedNum + 1;
                    nameList = nameList + "\n§8- §7" + reportedPlayerName;
                    break;
                }
            }
        }

        if (reportedNum == 0) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7There are currently no reported players.");
            return;
        }

        if (reportedNum == 1) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7There is currently §d§l" + reportedNum + " §7reported player: ");
        } else {
            sender.sendMessage("§8§l[§d§lES§8§l] §7There are currently §d§l" + reportedNum + " §7reported players: ");
        }

        sender.sendMessage(nameList);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().hasPermission("reportbook.report.notify")) {return;}
        new BukkitRunnable() {
            @Override
            public void run() {
                outputReportedUsersOpen(e.getPlayer());
            }
        }.runTaskLater(plugin, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("reportbook.report.notify")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7You have insufficient permissions.");
            return true;
        }

        if (args.length == 0) {
            outputReportedUsersOpen(sender);
            return true;
        }

        String reportedFriendly = getFriendlyName(args[0]);

        if (reportedFriendly.equals("false")) {
            sender.sendMessage("§8§l[§d§lES§8§l] §7This user has not connected to the server.");
            return true;
        }

        String uuid = ReportBook.dataManager.nameToUUID(reportedFriendly).toString();

        ReportBook.dataManager.getReport(uuid, sender, reportedFriendly);
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {

        List<String> empty = new ArrayList<>();

        if (!commandSender.hasPermission("reportbook.report.notify")) {
            return empty;
        }

        if (args.length == 1) {
            return listReportedUsersOpen(commandSender);
        }

        return empty;
    }
}