package de.mewin.mr.util.banking;

import de.mewin.util.Saveable;
import de.mewin.util.SavingValue;
import de.mewin.util.ValueSaver;
import java.util.ArrayList;
import java.util.Arrays;
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
