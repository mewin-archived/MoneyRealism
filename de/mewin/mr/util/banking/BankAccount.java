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
package de.mewin.mr.util.banking;

import de.mewin.util.Saveable;
import de.mewin.util.SavingValue;
import de.mewin.util.ValueSaver;
import java.util.List;
import org.bukkit.craftbukkit.v1_4_6.inventory.CraftInventoryCustom;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class BankAccount implements Saveable, InventoryHolder
{
    @SavingValue
    private BankAccountOwner owner;
    @SavingValue
    private double money;
    @SavingValue
    private ItemStack[] storedItems;
    private ValueSaver saver;
    private BankInventory inventory;
    
    private BankAccount()
    {
        this.saver = new ValueSaver(this);
    }
    
    public BankAccount(String owner)
    {
        this(new PlayerOwner(owner));
    }
    
    public BankAccount(String owner, String name)
    {
        this(new Organisation(name, owner));
    }
    
    private BankAccount(BankAccountOwner owner)
    {
        this.saver = new ValueSaver(this);
        this.owner = owner;
        this.money = 0d;
        this.storedItems = new ItemStack[54];
        this.inventory = new BankInventory(this);
    }
    
    public BankAccountOwner getOwner()
    {
        return this.owner;
    }
    
    public void addMoney(double amount)
    {
        if (amount > 0)
        {
            this.money += amount;
        }
    }
    
    public void takeMoney(double amount)
    {
        if (amount > 0)
        {
            this.money -= amount;
        }
    }
    
    public double getMoney()
    {
        return this.money;
    }
    
    @Override
    public Object serialize()
    {
        this.storedItems = inventory.getContents();
        return saver.getYaml();
    }
    
    private void load(List yaml)
    {
        saver.load(yaml);
        this.inventory = new BankInventory(this);
        if (((Double) this.money) == null)
        {
            this.money = 0d;
        }
        if (this.storedItems == null)
        {
            this.storedItems = new ItemStack[54];
        }
    }
    
    public static BankAccount deserialize(Object obj)
    {
        BankAccount acc = new BankAccount();
        acc.load((List) obj);
        return acc;
    }

    @Override
    public Inventory getInventory()
    {
        return this.inventory;
    }
    
    private class BankInventory extends CraftInventoryCustom
    {
        public BankInventory(BankAccount account)
        {
            super(account, account.storedItems.length, "Stored Items");
            this.setContents(account.storedItems);
        }
    }
}
