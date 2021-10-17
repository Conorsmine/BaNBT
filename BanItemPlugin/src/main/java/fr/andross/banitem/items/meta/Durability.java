/*
 * BanItem - Lightweight, powerful & configurable per world ban item plugin
 * Copyright (C) 2021 André Sustac
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your action) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.andross.banitem.items.meta;

import fr.andross.banitem.utils.BanVersion;
import fr.andross.banitem.utils.debug.Debug;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple meta comparator to compare the durability
 * @version 3.3
 * @author Andross
 */
public final class Durability extends MetaTypeComparator {
    private int min = -1, max = -1;

    public Durability(final Object o, final Debug debug) {
        super(o);

        if (o instanceof String) {
            final String[] s = o.toString().split("-");
            if (s.length != 2) {
                debug.clone().add("&cInvalid interval '" + o + "' for meta type &e&ldurability&c.").sendDebug();
                setValid(false);
                return;
            }

            try {
                min = Integer.parseInt(s[0]);
            } catch (final NumberFormatException e) {
                debug.clone().add("&cInvalid inferior interval '" + s[0] + "' for meta type &e&ldurability&c.").sendDebug();
                setValid(false);
                return;
            }

            try {
                max = Integer.parseInt(s[1]);
            } catch (final NumberFormatException e) {
                debug.clone().add("&cInvalid superior interval '" + s[1] + "' for meta type &e&ldurability&c.").sendDebug();
                setValid(false);
                return;
            }

            if (max < min) {
                debug.clone().add("&cThe superior interval must be inferior to the inferior interval for meta type &e&ldurability&c.").sendDebug();
                setValid(false);
            }
        } else {
            try {
                min = max = Integer.parseInt(o.toString());
            } catch (final NumberFormatException e) {
                debug.clone().add("&cInvalid durability '" + o + "' for meta type &e&ldurability&c.").sendDebug();
                setValid(false);
            }
        }
    }

    @Override
    public boolean matches(@NotNull final ItemStack itemStack, @Nullable final ItemMeta itemMeta) {
        final int dura;
        if (BanVersion.v13OrMore) {
            if (itemMeta == null) return false;
            dura = ((Damageable)itemMeta).getDamage();
        } else dura = itemStack.getDurability();
        return dura >= min && dura <= max;
    }
}
