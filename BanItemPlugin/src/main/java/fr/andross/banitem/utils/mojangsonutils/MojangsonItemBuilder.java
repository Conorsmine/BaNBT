package fr.andross.banitem.utils.mojangsonutils;

import de.tr7zw.nbtapi.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Utils class to parse nbt and create an item
 * @version 1.0
 * @author Conorsmine
 */
public class MojangsonItemBuilder {

    private final NBTCompound itemCompound;

    public MojangsonItemBuilder(@Nonnull ItemStack item) {
        this.itemCompound = NBTItem.convertItemtoNBT(item);
    }

    public MojangsonItemBuilder() {
        this.itemCompound = new NBTContainer();
        setDefault();
    }

    private void setDefault() {
        itemCompound.setString("id", "minecraft:air");
        itemCompound.setByte("Count", (byte) 1);
        itemCompound.setShort("Damage", (short) 0);
    }

    /**
     * This method adds the specified data following the path.
     * @param nbtPath Path to the data
     * @param o Data to be set
     */
    public MojangsonItemBuilder addData(@Nonnull final String nbtPath, @Nonnull final Object o) {
        // This is due to the ambiguity for if the index of the array isn't 0
        // What should the "empty" indexes be? 0, "{}" or ""?!
        if (MojangsonUtils.isArr(MojangsonUtils.getLastKey(nbtPath)))
            throw new UnsupportedOperationException(String.format("Cannot add value to array directly, only to tags: \"%s\"", nbtPath));

        NBTCompound rec = new NBTContainer();
        addCompounds(rec, nbtPath, o);
        mergeCompounds(itemCompound, rec);
        return this;
    }

    private void addCompounds(NBTCompound compound, String nbtPath, Object o) {
        String newPath = MojangsonUtils.removeLastKey(nbtPath);

        if (o instanceof List) { List<?> l = ((List<?>) o); o = (l.isEmpty()) ? o : l.get(0); }
        if (StringUtils.isBlank(newPath)) { addDataToCompound(compound, nbtPath, o); return; }

        String[] compArr = MojangsonUtils.pathToKeys(newPath);
        Iterator<String> it = Arrays.stream(compArr).iterator();
        while (it.hasNext()) {
            final String key = it.next();
            if (MojangsonUtils.isArr(key)) { compound = addList(compound, key); }
            else { compound = compound.addCompound(key); }

            if (!it.hasNext()) addDataToCompound(compound, MojangsonUtils.getLastKey(nbtPath), o);
        }
    }

    private NBTCompound addList(NBTCompound compound, String key) {
        if (MojangsonUtils.isFullArray(key))
            throw new UnsupportedOperationException(String.format("Full arrays are not supported: \"%s\"", key));

        final NBTCompoundList list = addNBTTagList(compound, MojangsonUtils.getArrayKeyValue(key));
        int index = MojangsonUtils.getIndexOfArrayKey(key);
        int missing = Math.max(0, ((index + 1) - list.size())); // How many indexes are missing
        addMissingIndexes(list, missing);
        return list.get(index);
    }

    private void addDataToCompound(NBTCompound compound, String key, Object o) {
        MojangsonUtils.setSimpleDataFromKey(compound, key, o);
    }

    private NBTCompoundList addNBTTagList(NBTCompound compound, String key) {
        NBTCompoundList list = compound.getCompoundList(key);
        if (list.size() > 0) return list;

        // For some reason, the list on "generates" if a value
        // was added. So I add and remove a value to ensure that the list
        // has been created.
        list.addCompound(new NBTContainer());
        list.remove(list.size() - 1);
        return list;
    }

    private void addMissingIndexes(NBTCompoundList list, int missing) {
        for (int i = 0; i < missing; i++)
            list.addCompound(new NBTContainer());
    }

    // The "adding" compound has priority
    private void mergeCompounds(NBTCompound original, NBTCompound adding) {
        for (String key : adding.getKeys()) {
            NBTType addingType = adding.getType(key);

            // Directly overriding original value
            if (MojangsonUtils.SIMPLE_TYPES.contains(addingType)) {
                MojangsonUtils.setSimpleDataFromKey(original, key,
                        MojangsonUtils.getSimpleDataFromCompound(adding, key));
                continue;
            }

            // Recursion
            if (addingType == NBTType.NBTTagCompound) {
                if (original.getType(key) != addingType) original.removeKey(key);
                if (!original.getKeys().contains(key)) original.addCompound(key);
                mergeCompounds(original.getCompound(key), adding.getCompound(key));
                continue;
            }

            // List handling
            if (addingType == NBTType.NBTTagList) {
                if (original.getType(key) != addingType) original.removeKey(key);
                if (!original.getKeys().contains(key)) addNBTTagList(original, key);
                mergeNBTLists(original, adding, key);
            }
        }
    }

    private void mergeNBTLists(NBTCompound original, NBTCompound adding, String key) {
        NBTCompoundList addingCompoundList = adding.getCompoundList(key);
        NBTCompoundList originalCompoundList = original.getCompoundList(key);

        // Convert to List, cause then stuff like #add work
        List<NBTCompound> originalCompounds = new LinkedList<>();
        originalCompoundList.forEach(c -> originalCompounds.add(new NBTContainer(c.toString())));
        originalCompoundList.clear();

        // Modify the list by adding new or overriding the original values
        for (int i = 0; i < addingCompoundList.size(); i++) {
            final NBTCompound add = addingCompoundList.get(i);

            if (i < originalCompounds.size()) {
                // Merge the compounds of the lists
                mergeCompounds(add, originalCompounds.get(i));
                originalCompounds.set(i, add);
            }
            else
                originalCompounds.add(add);
        }

        // Add data back into original NBTCompoundList as to maintain referencing
        originalCompounds.forEach(originalCompoundList::addCompound);
    }

    public final NBTCompound getItemCompound() {
        return itemCompound;
    }

    public final ItemStack getItemFromData() {
        return NBTItem.convertNBTtoItem(itemCompound);
    }
}
