package fr.andross.banitem.utils.scanners;

import fr.andross.banitem.BanConfig;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.BanUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class BaublesScanner {

    private final BanItem pl;
    private final BanUtils utils;
    private boolean enabled;
    private int taskId = -1;

    public BaublesScanner(BanItem pl, BanUtils utils) {
        this.pl = pl;
        this.utils = utils;
    }

    public void load(@NotNull final CommandSender sender, @NotNull final BanConfig config) {
        final ConfigurationSection section = config.getConfig().getConfigurationSection("illegal-stacks");
        if (section == null) return;
        boolean enabledInConfig = section.getBoolean("bauble");

        setEnabled(enabledInConfig && !enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            if (taskId < 0)
                taskId = pl.getServer().getScheduler().runTaskTimer(pl, () -> Bukkit.getOnlinePlayers().forEach(utils::checkPlayerBaubleStacks), 40L, 40L).getTaskId();
        } else {
            if (taskId > -1) {
                pl.getServer().getScheduler().cancelTask(taskId);
                taskId = -1;
            }
        }
    }
}
