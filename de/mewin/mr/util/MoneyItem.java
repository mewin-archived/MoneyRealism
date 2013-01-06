/*
 * Copyright (C) 2012 mewin <mewin001@hotmail.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package de.mewin.mr.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public final class MoneyItem
{
    public static int moneyItemId = Material.GOLD_NUGGET.getId();
    public static short moneyItemData = 0;
    public static String moneyItemName = "Money";
    public static ChatColor moneyItemColor = ChatColor.GOLD;
    public static Economy economy = null;
    
    private static Random rand = new Random();
    
    public static ItemStack create(double amount, String ... comments)
    {
        ItemStack stack = new ItemStack(moneyItemId, 1, moneyItemData);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(moneyItemColor + moneyItemName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + economy.format(amount) + randomColorCode(20));
        if (comments != null)
        {
            lore.addAll(Arrays.asList(comments));
        }
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }
    
    public static boolean is(ItemStack stack)
    {
        ItemMeta meta = stack.getItemMeta();
        
        return stack.getTypeId() == moneyItemId && stack.getDurability() == moneyItemData && meta.hasDisplayName() && meta.getDisplayName().equals(moneyItemColor + moneyItemName);
    }
    
    public static double getMoney(ItemStack stack)
    {
        if (!is(stack))
        {
            return 0d;
        }
        else
        {
            List<String> lore = stack.getItemMeta().getLore();
            
            if (lore.size() < 1)
            {
                return 0d;
            }
            return getMoney(lore.get(0));
        }
    }
    
    public static String[] getComments(ItemStack stack)
    {
        if (!is(stack))
        {
            return null;
        }
        else
        {
            List<String> lore = stack.getItemMeta().getLore();
            
            if (lore.size() < 2)
            {
                return new String[0];
            }
            else
            {
                String str[] = new String[lore.size() - 1];
                
                for (int i = 1; i < lore.size(); i++)
                {
                    str[i - 1] = lore.get(i);
                }
                
                return str;
            }
        }
    }
    
    private static Double getMoney(String moneyString)
    {
        String onlyNumbers = ChatColor.stripColor(moneyString).replaceAll("[^0-9\\.]", "");
        
        return Double.parseDouble(onlyNumbers);
    }
    
    public static ItemStack combine(ItemStack ... stacks)
    {
        double amount = 0d;
        int numStacks = 0;
        List<String> comments = new ArrayList<>();
        
        for (ItemStack stack : stacks)
        {
            if (stack != null && !is(stack) && stack.getTypeId() != 0)
            {
                return null;
            }
            else if (stack != null && is(stack))
            {
                numStacks++;
                amount += getMoney(stack);
                for (String comment : getComments(stack))
                {
                    if (!comments.contains(comment))
                    {
                        comments.add(comment);
                    }
                }
            }
        }
        
        if (amount > 0d && numStacks > 1)
        {
            return create(amount, comments.toArray(new String[0]));
        }
        else
        {
            return null;
        }
    }
    
    private static String randomColorCode(int length)
    {
        String str = "";
        
        for (int i = 0; i < length; i++)
        {
            str += ChatColor.getByChar(String.valueOf(rand.nextInt(9)));
        }
        
        return str;
    }
}