package fr.andross.banitem.commands.customAdd;

import de.tr7zw.nbtapi.NBTItem;
import fr.andross.banitem.BanItem;
import fr.andross.banitem.actions.BanAction;
import fr.andross.banitem.items.CustomBannedItem;
import fr.andross.banitem.utils.MojangsonUtils;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

public class CustomItemBanner {

    private final AddAction action;
    private final BanItem pl;

    public CustomItemBanner(AddAction action) {
        this.action = action;
        this.pl = action.getPlugin();
    }

    public void addCustomBannedItem() {
        final CustomBannedItem customBannedItem = createCustomBannedItem();
        addItemToCustomConfig(customBannedItem);
        addItemToConfig(customBannedItem);
    }

    private void addItemToCustomConfig(CustomBannedItem customBannedItem) {
        FileConfiguration customConfig = BanItem.getInstance().getBanDatabase().getCustomItems().getConfig();
        customConfig.set(customBannedItem.getName(), createCustomItemBanData());
        try {
            customConfig.save(BanItem.getInstance().getBanDatabase().getCustomItems().getFile());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void addItemToConfig(CustomBannedItem customBannedItem) {
        final FileConfiguration banConfig = BanItem.getInstance().getConfig();
        final Map<String, List<String>> banData = createBanActionData();
        World[] banWorldsArr = action.getActionParser().getBanWorlds();
        String[] banWorldsStr = (banWorldsArr.length == 0) ? new java.lang.String[] {"*"} : Arrays.stream(banWorldsArr)
                .map(World::getName).toArray(String[]::new);
        ConfigurationSection blacklist = new YamlConfiguration();
        if (banConfig.contains("blacklist")) blacklist = banConfig.getConfigurationSection("blacklist");
        if (blacklist == null) blacklist = new YamlConfiguration();

        addItemToWorlds(blacklist, banWorldsStr, customBannedItem, banData);
        banConfig.set("blacklist", blacklist);

        try {
            banConfig.save(BanItem.getInstance().getBanConfig().getConfigFile());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void addItemToWorlds(ConfigurationSection blacklist, String[] worlds, CustomBannedItem customBannedItem, Map<String, List<String>> banData) {
        for (String world : worlds) {
            ConfigurationSection worldSection = new YamlConfiguration();
            if (blacklist.contains(world)) worldSection = blacklist.getConfigurationSection(world);

            worldSection.set(customBannedItem.getName(), banData);
            blacklist.set(world, worldSection);
        }
    }

    private CustomBannedItem createCustomBannedItem() {
        BanItem banItemPlugin = BanItem.getInstance();
        Debug itemDebug = new Debug(banItemPlugin.getBanConfig(), action.getPlayer());
        return new CustomBannedItem(generateBanItemKey(), createCustomConfigurationSection(), itemDebug);
    }

    private YamlConfiguration createCustomConfigurationSection() {
        YamlConfiguration customItemConf = new YamlConfiguration();
        createCustomItemBanData().forEach(customItemConf::set);
        return customItemConf;
    }

    private Map<String, List<String>> createBanActionData() {
        final Map<String, List<String>> actionData = new HashMap<>();
        if (action.getActionParser().getBanActions().isEmpty()) {
            actionData.put("*", action.getActionParser().getBanMessages());
            return actionData;
        }

        for (BanAction banAction : action.getActionParser().getBanActions()) {
            actionData.put(
                    banAction.getName(),
                    action.getActionParser().getBanMessages());
        }
        return actionData;
    }

    private Map<String, Object> createCustomItemBanData() {
        Map<String, Object> banMap = new HashMap<>();
        final ItemStack item = NBTItem.convertNBTtoItem(action.getItemNBT());
        final Map<String, Object> banData = createNBTBanData();

        if (action.getNbtDataStrings().contains("Damage")) banMap.put("durability", item.getDurability());
        if (!banData.isEmpty()) banMap.put("nbtapi", banData);
        banMap.put("material", item.getType().name());
        return banMap;
    }

    private Map<String, Object> createNBTBanData() {
        Map<String, Object> nbtData = new HashMap<>();
        Set<String> paths = (action.getActionParser().isBanCurrent()) ?
                action.addNbtDataStrings(MojangsonUtils.getAllSimplePaths(action.getItemNBT())) : action.getNbtDataStrings();

        for (String path : paths) {
            if (path.equals("Damage")) continue;
            if (path.equals("id")) continue;
            if (path.equals("Count")) continue;

            MojangsonUtils.NBTResult result = MojangsonUtils.getCompoundFromPath(action.getItemNBT(), path);
            Object data = MojangsonUtils.getSimpleDataFromCompound(result);

            if (MojangsonUtils.getFirstKey(path).equals("tag")) path = path.replaceFirst("tag\\.", "");
            nbtData.put(path.replaceAll("\\.", "#"), data);
        }

        return nbtData;
    }

    private String generateBanItemKey() {
        String key = NBTItem.convertNBTtoItem(action.getItemNBT()).getType().name().toLowerCase(Locale.ROOT);
        FileConfiguration customItemConf = pl.getBanDatabase().getCustomItems().getConfig();
        int highestIndex = 0;
        for (String itemKey : customItemConf.getKeys(false)) {
            if (!itemKey.startsWith(key)) continue;

            int newIndex = Integer.parseInt(itemKey.replace(key + "_", "")) + 1;
            if (newIndex > highestIndex) highestIndex = newIndex;
        }

        return String.format("%s_%d", key, highestIndex);
    }
}
