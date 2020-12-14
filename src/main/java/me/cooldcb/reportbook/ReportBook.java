package me.cooldcb.reportbook;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ReportBook extends JavaPlugin implements Listener {
    static DataManager dataManager;

    public void writeFile() {
        File reports = new File(this.getDataFolder(),"reports.yml");
        try {
            reports.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {

        writeFile();

        saveDefaultConfig();
        reloadConfig();

        ReportCmd reportCmd = new ReportCmd(this);
        ReportsNotify reportsNotify = new ReportsNotify(this);
        ReportStatus reportStatus = new ReportStatus(this);
        GetUUID getUUID = new GetUUID(this);
        dataManager = new DataManager(this);

        this.getCommand("reports").setExecutor(reportsNotify);
        this.getCommand("report").setExecutor(reportCmd);
        this.getCommand("uuid").setExecutor(getUUID);
        this.getCommand("reportstatus").setExecutor(reportStatus);
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(reportCmd, this);
        this.getServer().getPluginManager().registerEvents(reportsNotify, this);
        this.getServer().getPluginManager().registerEvents(reportStatus, this);
        this.getServer().getPluginManager().registerEvents(dataManager, this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private ConfigurationSection getSectionOrCreate(ConfigurationSection configurationSection, String path) {
        if (configurationSection.contains(path)) {
            return configurationSection.getConfigurationSection(path);
        } else {
            return configurationSection.createSection(path);
        }
    }
}