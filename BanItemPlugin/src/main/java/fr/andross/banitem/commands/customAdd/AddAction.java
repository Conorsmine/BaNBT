package fr.andross.banitem.commands.customAdd;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTType;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.utils.mojangsonutils.MojangsonUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class AddAction {

    private static final Map<UUID, AddAction> actionMap = new HashMap<>();
    private static final String finishActionCmdFormat = "/bi hidden_cmd %s FINISH";

    private final BanItem pl;
    private final Player p;
    private final UUID actionID = UUID.randomUUID();
    private final String header;

    private final AddActionParser actionParser;
    private final CustomItemBanner banCustomItemBuilder;
    private final Set<String> nbtDataStrings = new HashSet<>();
    private final NBTContainer itemNBT;

    private final MojangsonUtils mojangson = new MojangsonUtils()
            .setClickTypes(MojangsonUtils.SIMPLE_TYPES.toArray(new NBTType[0]))
            .setCmdFormat("/bi hidden_cmd " + actionID + " %s")
            .setInvalidClickTargetFormat("/bi hidden_cmd " + actionID + " INVALID %s ");

    public AddAction(BanItem pl, Player p, String cmdHeader, String[] args) {
        this.pl = pl;
        this.p = p;
        this.header = cmdHeader;
        this.itemNBT = NBTItem.convertItemtoNBT(p.getInventory().getItemInMainHand());
        this.actionParser = new AddActionParser(this, args);
        this.banCustomItemBuilder = new CustomItemBanner(this);

        actionMap.put(actionID, this);

        if (!actionParser.isBanCurrent())
            initAction();
        else completeAction();
    }

    private void initAction() {
        sendUsageMsg();
        p.spigot().sendMessage(mojangson.getInteractiveMojangson(itemNBT, ""));
    }

    private void deleteAction() {
        actionMap.remove(actionID);
    }

    private void clearChat() {
        p.sendMessage(new String[20]);
    }

    private void sendHeader() {
        pl.getUtils().sendMessage(p, header);
    }

    private void sendUsageMsg() {
        sendHeader();
        pl.getUtils().sendMessage(p, "&7Usage:");
        pl.getUtils().sendMessage(p, "&7 >> Select in the following NBT");
        pl.getUtils().sendMessage(p, "&7 >> all data that should be considered.");
        pl.getUtils().sendMessage(p, "&7 >> After doing so specify what the");
        pl.getUtils().sendMessage(p, "&7 >> it should have.");
        pl.getUtils().sendMessage(p, "&7 >> When all the data has been selected");
        pl.getUtils().sendMessage(p, "&7 >> and configured, press on the &a\"FINISH\"&7 button.");
        pl.getUtils().sendMessage(p, "&7 >> Note: You can undo a selection by clicking on the path again.");
        pl.getUtils().sendMessage(p, "&7 >> Note: You can only select data which has &aGREEN&7 hovertext.");
        pl.getUtils().sendMessage(p, "&7 >> Note: This process will take up your chat.");
        pl.getUtils().sendMessage(p, "&7 >> Note: Use &b/bi info &3debug &7to get basic info.");
    }

    private void sendNewDataMsg() {
        clearChat();
        sendHeader();
        p.spigot().sendMessage(mojangson
                .setSpecialColorPaths(nbtDataStrings.toArray(new String[0]))
                .getInteractiveMojangson(itemNBT, "")
        );

        p.sendMessage("");
        final TextComponent finishButton = new TextComponent("FINISH");
        finishButton.setColor(ChatColor.GREEN);
        finishButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(finishActionCmdFormat, actionID)));
        finishButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Finished configuring?").create()));
        p.spigot().sendMessage(finishButton);
    }

    private void sendInvalidTargetErr(String[] args) {
        pl.getUtils().sendMessage(p, String.format(" &7\"&b%s&7\" is not a valid path!", args[3]));
        pl.getUtils().sendMessage(p, " &7All valid data options have &aGREEN&7 hover text.");
    }

    private void toggleDataString(String data) {
        if (nbtDataStrings.contains(data))
            nbtDataStrings.remove(data);
        else
            nbtDataStrings.add(data);
    }

    private void completeAction() {
        banCustomItemBuilder.addCustomBannedItem();

        pl.getUtils().sendMessage(p, String.format(" %s", header));
        // todo:
        p.sendMessage("Send some completion text here!");
        deleteAction();
    }



    public static void processActionCmd(CommandSender sender, String[] args) {
        final AddAction action = getActionMap().getOrDefault(UUID.fromString(args[1]), null);
        if (action == null) return;
        if (args[2].equals("INVALID")) { action.sendInvalidTargetErr(args); return; }
        if (args[2].equals("FINISH")) { action.completeAction(); return; }

        String path = args[2];
        action.toggleDataString(path);
        action.sendNewDataMsg();
    }

    public static Map<UUID, AddAction> getActionMap() {
        return actionMap;
    }

    public Player getPlayer() {
        return p;
    }

    public BanItem getPlugin() {
        return pl;
    }

    public AddActionParser getActionParser() {
        return actionParser;
    }

    public NBTContainer getItemNBT() {
        return itemNBT;
    }

    public Set<String> getNbtDataStrings() {
        return nbtDataStrings;
    }

    public Set<String> addNbtDataStrings(final Set<String> data) {
        nbtDataStrings.addAll(data);
        return nbtDataStrings;
    }
}
