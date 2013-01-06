package de.mewin.mr;

import de.mewin.mr.util.MoneyItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class MRListener implements Listener
{
    private MRPlugin plugin;
    private Economy economy;
    
    public MRListener(MRPlugin plugin)
    {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
    }
    
    @EventHandler
    public void onPlayerDeath(EntityDeathEvent e)
    {
        if (e.getEntity() instanceof Player)
        {
            Player player = (Player) e.getEntity();
            double money = economy.getBalance(player.getName());
            
            if (money > 0d)
            {
                economy.withdrawPlayer(player.getName(), money);
                e.getDrops().add(MoneyItem.create(money, ChatColor.BLUE + "The money " + ChatColor.stripColor(player.getDisplayName()) + ChatColor.BLUE + " dropped when he died."));
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (e.getAction() == Action.RIGHT_CLICK_AIR && e.hasItem() && MoneyItem.is(e.getItem()))
        {
            double amount = MoneyItem.getMoney(e.getItem());
            
            e.getPlayer().setItemInHand(null);
            economy.depositPlayer(e.getPlayer().getName(), amount);
            e.getPlayer().sendMessage(ChatColor.GREEN + "You received " + economy.format(amount));
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e)
    {
        if (e.getInventory() instanceof CraftingInventory)
        {
            final CraftingInventory inv = (CraftingInventory) e.getInventory();
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable()
            {
                public void run()
                {
                    updateCrafting(inv);
                }
            }, 1L);
        }
    }
    
    private void updateCrafting(final CraftingInventory inv)
    {
        ItemStack combined = MoneyItem.combine(inv.getMatrix());
        
        if (combined != null && !combined.equals(inv.getResult()))
        {
            inv.setResult(combined);
            for (HumanEntity ent : inv.getViewers())
            {
                if (ent instanceof CraftPlayer)
                {
                    ((CraftPlayer) ent).updateInventory();
                }
            }
        }
    }
}
