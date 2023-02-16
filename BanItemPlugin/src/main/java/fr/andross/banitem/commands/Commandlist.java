package fr.andross.banitem.commands;


import de.tr7zw.nbtapi.NBTItem;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.items.BannedItem;
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.utils.mojangsonutils.MojangsonItemBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A subcommand used to list all custom
 * items and some info about them.
 * @version  1.0
 * @author Conorsmine
 */
public class Commandlist extends BanCommand{

    private static final Set<Material> MAT = Arrays.stream(Material.values()).collect(Collectors.toSet());
    private static final String CMD_FORMAT = "/bi list hidden_cmd %s";  // "%s" is replaced with the name of the item

    public Commandlist(@NotNull BanItem pl, @NotNull CommandSender sender, @NotNull String[] args) {
        super(pl, sender, args);
    }

    @Override
    public void run() {
        if (args.length >= 2 && args[1].equals("hidden_cmd"))
            sendBannedItemInfo(args[2]);
        else {
            header("&6&lList");
            message("&7All currently banned custom items:");
            message("&7Note: Click on an item to view specifics.");

            sendBannedItemMsg();
        }
    }

    @Nullable
    @Override
    public List<String> runTab() {
        return null;
    }

    // Sends info about a specific item
    private void sendBannedItemInfo(String itemName) {
        final ConfigurationSection itemConfig = pl.getBanDatabase().getCustomItems()
                .getConfig().getConfigurationSection(itemName);
        if (itemConfig == null) return;

        header("&6&lList - Info");
        message(String.format("&3%s:", itemName));
        printConfigData(itemConfig, 1);
    }

    // Sends list of all items
    private void sendBannedItemMsg() {
        boolean isPlayer = (sender instanceof Player);

        FileConfiguration customItemConfig = pl.getBanDatabase().getCustomItems().getConfig();
        final TextComponent itemName = new TextComponent();
        itemName.setColor(ChatColor.AQUA);
        for (CustomBannedItem bannedItem : pl.getBanDatabase().getCustomItems().getReversed().keySet()) {
            final String itemJson = getBannedItemJson(customItemConfig, bannedItem.getName());
            if (itemJson == null) continue;

            if (isPlayer) {
                itemName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(itemJson)}));
                itemName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(CMD_FORMAT, bannedItem.getName())));
            }

            itemName.setText(String.format("%s §7>> §b%s", pl.getBanConfig().getPrefix(), bannedItem.getName()));
            sender.spigot().sendMessage(itemName);
        }
    }

    private void printConfigData(ConfigurationSection data, int indent) {
        for (String key : data.getKeys(false)) {
            if (data.isConfigurationSection(key)) {
                message(String.format("%s&3%s", addIndent(indent), key));
                printConfigData(data.getConfigurationSection(key), indent + 1);
            }
            else if (data.isList(key)) {
                final List<?> dataList = data.getList(key);
                if (dataList == null) return;
                if (dataList.size() <= 1)
                    message(String.format("%s&3%s: &d[&6%s&d]", addIndent(indent), key, ((dataList.size() == 0) ? "" : dataList.get(0))));
                else {
                    message(String.format("%s&3%s:", addIndent(indent), key));
                    dataList.forEach(o -> message(String.format("%s  &3-&6%s", addIndent(indent), o)));
                }
            }
            else {
                message(String.format("%s&3%s: &6%s", addIndent(indent), key, data.get(key)));
            }
        }
    }

    private String addIndent(int indent) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < indent; i++)
            str.append("  ");
        return str.toString();
    }

    private String getBannedItemJson(FileConfiguration customItemConfig, String bannedItemName) {
        ConfigurationSection customItemData = customItemConfig.getConfigurationSection(bannedItemName);
        if (!validItemData(bannedItemName, customItemData)) return null;
        String itemType = customItemData.getString("material");
        if (!materialExists(bannedItemName, itemType)) return null;
        if (!validMaterial(bannedItemName, itemType)) return null;

        ItemStack customItem = new ItemStack(Material.getMaterial(itemType.toUpperCase(Locale.ROOT)), 1, (short) customItemData.getInt("durability", 0));
        if (!customItemData.getKeys(false).contains("nbtapi")) return NBTItem.convertItemtoNBT(customItem).toString();

        ConfigurationSection nbtApi = customItemData.getConfigurationSection("nbtapi");
        MojangsonItemBuilder itemBuilder = new MojangsonItemBuilder(customItem);

        for (String path : nbtApi.getKeys(false)) {
            Object data = nbtApi.get(path);
            if (data == null) {
                message(String.format("\"%s\" for custom item \"%s\" is missing data!", path, bannedItemName));
                continue;
            }

            String dataPath = String.format("tag.%s", path.replaceAll("#", "."));
            itemBuilder.addData(dataPath, data);
        }

        return itemBuilder.getItemCompound().toString();
    }

    private boolean validItemData(String bannedItemName, ConfigurationSection customItemData) {
        if (customItemData == null) {
            message(String.format("Missing data for item: \"%s\"", bannedItemName));
            return false;
        }

        return true;
    }

    private boolean materialExists(String bannedItemName, String itemType) {
        if(StringUtils.isBlank(itemType)) {
            message(String.format("Missing material for item: \"%s\"", bannedItemName));
            return false;
        }

        return true;
    }

    private boolean validMaterial(String bannedItemName, String itemType) {
        if (!MAT.contains(Material.getMaterial(itemType.toUpperCase(Locale.ROOT)))) {
            message(String.format("Invalid material for item: \"%s\"", bannedItemName));
            return false;
        }

        return true;
    }
}
