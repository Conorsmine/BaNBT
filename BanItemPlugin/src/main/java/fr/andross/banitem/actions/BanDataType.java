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
package fr.andross.banitem.actions;

import fr.andross.banitem.utils.enchantments.EnchantmentWrapper;
import org.jetbrains.annotations.NotNull;

/**
 * A simple enum indicating what kind of data is used
 *
 * @author Andross
 * @version 3.3
 */
public enum BanDataType {

    /**
     * Type: Long (millis)
     * Used to check if the banned item has a cooldown
     */
    COOLDOWN("cooldown"),

    /**
     * Type: String
     * Used to get the custom/meta item name
     */
    CUSTOMNAME("customname"),

    /**
     * Type: Set of {@link EnchantmentWrapper}
     * Used to check if the enchantments are banned on an item
     */
    ENCHANTMENT("enchantment"),

    /**
     * Type: Set of {@link org.bukkit.entity.EntityType}
     * Used to check if the ban will applies on this entity
     */
    ENTITY("entity"),

    /**
     * Type: Set of {@link org.bukkit.GameMode}
     * Used to check if the ban applies on current player gamemode
     */
    GAMEMODE("gamemode"),

    /**
     * Type: Set of {@link org.bukkit.event.inventory.InventoryType}
     * Used to check if the ban will applies if the source inventory is included into this set
     */
    INVENTORY_FROM("inventory-from"),

    /**
     * Type: Set of {@link org.bukkit.event.inventory.InventoryType}
     * Used to check if the ban will applies if the destination inventory is included into this set
     */
    INVENTORY_TO("inventory-to"),

    /**
     * Type: boolean
     * Used to check if a log message will be sent to players with log activated
     */
    LOG("log"),

    /**
     * Type: Set of {@link org.bukkit.Material}
     * Used to check if the ban will applies if a material is in the set
     */
    MATERIAL("material"),

    /**
     * Type: List of <i>(already colored)</i> String
     * Used to get the ban message(s)
     */
    MESSAGE("message"),

    /**
     * Type: String
     * Used to get a custom permission for an action
     */
    PERMISSION("permission"),

    /**
     * Type: Set of {@link com.sk89q.worldguard.protection.regions.ProtectedRegion}
     * Used to check if the ban applies into the region
     */
    REGION("region"),


    /**
     * Type: Boolean
     * Used to determine if the user should be banned if the action occurs
     */
    BANNABLE("bannable"),

    /**
     * Type: List of String
     * Used to run commands when the banned action occurs
     */
    RUN("run");

    private final String name;

    BanDataType(@NotNull final String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
