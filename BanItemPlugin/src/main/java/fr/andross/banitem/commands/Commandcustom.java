package fr.andross.banitem.commands;

import fr.andross.banitem.BanItem;
import fr.andross.banitem.commands.customAdd.AddAction;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Sub command add custom
 * @version 1.0
 * @author Conorsmine
 */
public class Commandcustom extends BanCommand{

    public Commandcustom(@NotNull BanItem pl, @NotNull CommandSender sender, @NotNull String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        if (!sender.hasPermission("banitem.command.custom")) {
            message(getNoPermMessage());
            return;
        }

        if (!(sender instanceof Player)) { sendNonPlayerErr(); return; }
        Player p = ((Player) sender);
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) { sendNonItemErr(); return; }
        if (args.length < 2) { sendUsageMsg(); return; }

        if (AddAction.getActionMap().containsKey(p.getUniqueId())) sendLastActionCanceledMsg();
        message("&aStarted new action.");
        new AddAction(pl, p, "&7&m     &r &l[&6&lCustom&r&l] &7&m     ", args);
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return null;
    }

    private void sendNonPlayerErr() {
        message("&cOnly players can execute this command!");
    }

    private void sendNonItemErr() {
        message("&cYou need to hold an item for this command!");
    }

    private void sendUsageMsg() {
        header("&6&lCustom");
        message("&7Usage:");
        message("&b/bi custom &3<actions> [-w worlds] [-m message] [-b asIs]");
        message("&7 >> Will ban the currently held item.");
        message("&7 >> If no worlds were entered");
        message("&7 >> it will default to the current.");
        message("&7 >> Set the &b-b&7 flag to &3true&7 to ban");
        message("&7 >> the item as is. (A shortcut per say)");
        message("&2 Example: &b/bi custom &3hold,use -w world&f");
    }

    private void sendLastActionCanceledMsg() {
        message("&cYour last action has been discarded.");
    }
}
