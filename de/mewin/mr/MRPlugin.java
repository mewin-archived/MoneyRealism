package de.mewin.mr;

import de.mewin.mr.util.MoneyItem;
import de.mewin.mr.util.banking.BankAccount;
import de.mewin.mr.util.banking.BankAccountOwner;
import de.mewin.mr.util.banking.BankAccountOwnerType;
import de.mewin.util.SavingValue;
import de.mewin.util.ValueSaver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class MRPlugin extends JavaPlugin
{
    private static final int CMDS_PER_SITE = 7;
    private Economy economy = null;
    private MRListener listener;
    @SavingValue
    private List<BankAccount> accounts;
    private ValueSaver saver;
    
    @Override
    public void onEnable()
    {
        if (!setupEconomy())
        {
            getLogger().log(Level.SEVERE, "Could not establish connection to Vault. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
        else
        {
            accounts = new ArrayList<>();
            saver = new ValueSaver(this, new File(this.getDataFolder(), "fields.yml"));
            getLogger().log(Level.INFO, "Hooked into Vault. Thanks god this worked.");
            listener = new MRListener(this);
            getServer().getPluginManager().registerEvents(listener, this);
            initMoneyItem();
            load();
        }
    }
    
    @Override
    public void onDisable()
    {
        save();
    }
    
    private void load()
    {
        try
        {
            saver.load();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void save()
    {
        try
        {
            saver.save();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void initMoneyItem()
    {
        MoneyItem.economy = economy;
    }
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
        {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
    public Economy getEconomy()
    {
        return economy;
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String label, String[] params)
    {
        if (label.equals("moneytoitem") ||
                label.equals("mti"))
        {
            if (params.length < 1)
            {
                cs.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GOLD + cmd.getUsage().replace("<command>", label));
            }
            else if (cs instanceof Player)
            {
                Player player = (Player) cs;
                double amount;
                String comment = "";
                
                try
                {
                    amount = Double.parseDouble(params[0].replace(",", "."));
                }
                catch(NumberFormatException ex)
                {
                    player.sendMessage(ChatColor.RED + "\"" + params[0] + "\" is not a valid number.");
                    return true;
                }
                
                if (amount < 0.01d)
                {
                    player.sendMessage(ChatColor.RED + "You must specify a minimum amount of " + ChatColor.WHITE + economy.format(0.01));
                    return true;
                }
                
                if (params.length > 1)
                {
                    if (!player.hasPermission("mr.moneyitem.comment"))
                    {
                        player.sendMessage(ChatColor.RED + "You don't have permission to add comments to money items.");
                        return true;
                    }
                    for (int i = 1; i < params.length; i++)
                    {
                        comment += params[i] + " ";
                    }
                }
                
                if (player.hasPermission("mr.moneyitem.comment.color"))
                {
                    comment = ChatColor.translateAlternateColorCodes('&', comment);
                }
                
                if (!economy.withdrawPlayer(player.getName(), amount).transactionSuccess())
                {
                    player.sendMessage(ChatColor.RED + "You don't have that much money.");
                }
                else
                {
                    if (!comment.equals(""))
                    {
                        player.getInventory().addItem(MoneyItem.create(amount, comment));
                    }
                    else
                    {
                        player.getInventory().addItem(MoneyItem.create(amount));
                    }
                }
            }
            else
            {
                cs.sendMessage("Only players can create money items.");
            }
            return true;
        }
        else if (label.equals("bank") || label.equals("rbank") || label.equals("b"))
        {
            if (params.length < 1)
            {
                cs.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GOLD + cmd.getUsage().replace("<command>", label));
            }
            else
            {
                switch(params[0])
                {
                    case "account":
                    case "acc":
                    case "a":
                        if (params.length < 2)
                        {
                            cs.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GOLD + "/" + label + " " + params[0] + " [subcommand]");
                            cs.sendMessage(ChatColor.WHITE + "Type " + ChatColor.GOLD + "/" + label + " " + params[0] + " help for a list of subcommands.");
                        }
                        else
                        {
                            switch(params[1])
                            {
                                case "create":
                                case "c":
                                    if (!(cs instanceof Player))
                                    {
                                        cs.sendMessage(ChatColor.RED + "This action requires a player.");
                                    }
                                    else if (!cs.hasPermission("mr.bank.own"))
                                    {
                                        cs.sendMessage(ChatColor.RED + "You do not have access to that command.");
                                    }
                                    else
                                    {
                                        String name = cs.getName();
                                        BankAccountOwnerType type = BankAccountOwnerType.PLAYER;
                                        if (params.length > 2)
                                        {
                                            type = BankAccountOwnerType.ORGANISATION;
                                            name = params[2];
                                        }

                                        if (hasBankAccount(name, type))
                                        {
                                            cs.sendMessage(ChatColor.RED + "This bank account allready exists.");
                                        }
                                        else
                                        {
                                            createBankAccount((Player) cs, name, type);
                                            cs.sendMessage(ChatColor.GREEN + "Bank account created.");
                                        }
                                    }
                                    break;
                                case "remove":
                                case "rem":
                                case "del":
                                case "r":
                                case "d":
                                    if (!(cs instanceof Player))
                                    {
                                        cs.sendMessage(ChatColor.RED + "This action requires a player.");
                                    }
                                    else if (!cs.hasPermission("mr.bank.own"))
                                    {
                                        cs.sendMessage(ChatColor.RED + "You do not have access to that command.");
                                    }
                                    else
                                    {
                                        String name = cs.getName();
                                        BankAccountOwnerType type = BankAccountOwnerType.PLAYER;
                                        if (params.length > 2)
                                        {
                                            type = BankAccountOwnerType.ORGANISATION;
                                            name = params[2];
                                        }

                                        if (!hasBankAccount(name, type))
                                        {
                                            cs.sendMessage(ChatColor.RED + "This bank does not exists.");
                                        }
                                        else
                                        {
                                            removeBankAccount(name, type);
                                            cs.sendMessage(ChatColor.GREEN + "The account has been removed.");
                                        }
                                    }
                                    break;
                                case "list":
                                case "l":
                                    int site = 1;
                                    String search = null;
                                    BankAccountOwnerType type = null;
                                    if (params.length > 4)
                                    {
                                        search = params[4];
                                        try
                                        {
                                            site = Integer.parseInt(params[3]);
                                        }
                                        catch(NumberFormatException ex)
                                        {
                                            cs.sendMessage(ChatColor.RED + "Invalid site.");
                                            return true;
                                        }
                                        
                                        if ("organisation".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.ORGANISATION;
                                        }
                                        else if ("player".startsWith(params[2].toLowerCase())
                                                || "private".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.PLAYER;
                                        }
                                        else
                                        {
                                            cs.sendMessage(ChatColor.RED + "Unknown account type: \"" + params[2] + "\"");
                                            return true;
                                        }
                                    }
                                    else if (params.length > 3)
                                    {
                                        try
                                        {
                                            site = Integer.parseInt(params[3]);
                                        }
                                        catch(NumberFormatException ex)
                                        {
                                            search = params[3];
                                        }
                                        
                                        if ("organisation".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.ORGANISATION;
                                        }
                                        else if ("player".startsWith(params[2].toLowerCase())
                                                || "private".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.PLAYER;
                                        }
                                        else if (search == null)
                                        {
                                            search = params[2];
                                        }
                                        else
                                        {
                                            cs.sendMessage(ChatColor.RED + "Unknown account type: \"" + params[2] + "\"");
                                            return true;
                                        }
                                    }
                                    else if (params.length > 2)
                                    {
                                        if ("organisation".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.ORGANISATION;
                                        }
                                        else if ("player".startsWith(params[2].toLowerCase())
                                                || "private".startsWith(params[2].toLowerCase()))
                                        {
                                            type = BankAccountOwnerType.PLAYER;
                                        }
                                        else
                                        {
                                            try
                                            {
                                                site = Integer.valueOf(params[2]);
                                            }
                                            catch(NumberFormatException ex)
                                            {
                                                search = params[2];
                                            }
                                        }
                                    }
                                    
                                    if (site < 1)
                                    {
                                        cs.sendMessage(ChatColor.RED + "Site must be at least 1.");
                                        return true;
                                    }
                                    
                                    List<BankAccount> find = findAccounts(type, search, site, cs);
                                    if (find != null)
                                    {
                                        for (BankAccount acc : find)
                                        {
                                            cs.sendMessage(ChatColor.BLUE + acc.getOwner().getName() + ChatColor.WHITE + " (" + 
                                                    ChatColor.BLUE + acc.getOwner().getType().name() + ChatColor.WHITE + "): " +
                                                    economy.format(acc.getMoney()));
                                        }
                                    }
                                    
                                    break;
                                case "inventory":
                                case "inv":
                                case "i":
                                    if (cs instanceof Player)
                                    {
                                        if (params.length < 3)
                                        {
                                            cs.sendMessage(ChatColor.WHITE + "Usage: " + ChatColor.GOLD + "/" + label + " " + params[0] + " " + params[1] + " [Account Name] (Account Type)");
                                        }
                                        else
                                        {
                                            BankAccount acc;
                                            if (params.length > 3)
                                            {
                                                if ("organisation".startsWith(params[3].toLowerCase()))
                                                {
                                                    acc = bankByOwner(params[2], BankAccountOwnerType.ORGANISATION);
                                                }
                                                else if ("player".startsWith(params[3].toLowerCase())
                                                        || "private".startsWith(params[3].toLowerCase()))
                                                {
                                                    acc = bankByOwner(params[2], BankAccountOwnerType.PLAYER);
                                                }
                                                else
                                                {
                                                    cs.sendMessage(ChatColor.RED + "Unknown account type.");
                                                    return true;
                                                }
                                            }
                                            else
                                            {
                                                acc = bankByOwner(params[2], BankAccountOwnerType.PLAYER);
                                                if (acc == null)
                                                {
                                                    acc = bankByOwner(params[2], BankAccountOwnerType.ORGANISATION);
                                                }
                                            }
                                            if (acc == null)
                                            {
                                                cs.sendMessage(ChatColor.RED + "Bank account not found.");
                                            }
                                            else
                                            {
                                                ((Player) cs).openInventory(acc.getInventory());
                                            }
                                        }
                                    }
                                    else
                                    {
                                        cs.sendMessage(ChatColor.RED + "This action requires a player.");
                                    }
                                    break;
                                default:
                                    cs.sendMessage(ChatColor.RED + "Unknow subcommand.");
                                case "help":
                                case "h":
                                    cs.sendMessage(ChatColor.GOLD + "/" + label + " " + params[0] + " subcommands:");
                                    cs.sendMessage(ChatColor.YELLOW + "create" + ChatColor.GREEN + " (organisation)" + ChatColor.WHITE + " - create a private or an organisation account.");
                                    cs.sendMessage(ChatColor.YELLOW + "remove" + ChatColor.GREEN + " (organisation)" + ChatColor.WHITE + " - remove a private or an organisations account.");
                                    cs.sendMessage(ChatColor.YELLOW + "list" + ChatColor.GREEN + " (" + ChatColor.BLUE + "ORGANISATION" + ChatColor.GREEN + "/" + ChatColor.BLUE + "PLAYER" + ChatColor.GREEN + ") (site) (search pattern)" + ChatColor.WHITE + " - list bank accounts");
                                    break;
                            }
                        }
                        break;
                }
            }
            return true;
        }
        return false;
    }
    
    public List<BankAccount> findAccounts(BankAccountOwnerType type, String search, int site, CommandSender messageReceiver)
    {
        ArrayList<BankAccount> list = findAccounts(type, search);
        ArrayList<BankAccount> selected = new ArrayList<>();
        int begin = CMDS_PER_SITE * (site - 1);
        int maxSites = Math.max((int) Math.ceil((double) list.size() / CMDS_PER_SITE), 1);
        
        String searchDesc = ChatColor.WHITE + "Listing all accounts";
        
        if (type != null)
        {
            searchDesc += " of type " + ChatColor.BLUE + type.name() + ChatColor.WHITE;
        }
        if (search != null)
        {
            searchDesc += " that contain \"" + ChatColor.BLUE + search + ChatColor.WHITE + "\"";
        }
        
        searchDesc += "(" + (site <= maxSites ? ChatColor.BLUE : ChatColor.RED) + site + ChatColor.WHITE + 
                "/" + ChatColor.BLUE + maxSites + ChatColor.WHITE + ").";
        
        messageReceiver.sendMessage(searchDesc);
        if (list.size() < 1)
        {
            messageReceiver.sendMessage(ChatColor.GRAY + "No accounts found.");
            return null;
        }
        if (begin >= list.size())
        {
            messageReceiver.sendMessage(ChatColor.RED + "This search has only " + ChatColor.WHITE + 
                    maxSites + ChatColor.RED + " sites.");
            return null;
        }
        for (int i = begin; i < Math.min(begin + CMDS_PER_SITE, list.size()); i++)
        {
            selected.add(list.get(i));
        }
        
        return selected;
    }
    
    public ArrayList<BankAccount> findAccounts(BankAccountOwnerType type, String search)
    {
        ArrayList<BankAccount> list = new ArrayList<>();
        
        if (type == null && search == null)
        {
            return getAccounts();
        }
        
        for (BankAccount acc : accounts)
        {
            if ((type == null || acc.getOwner().getType() == type)
                    && (search == null || acc.getOwner().getName().toLowerCase().contains(search.toLowerCase())))
            {
                list.add(acc);
            }
        }
        
        return list;
    }
    
    public ArrayList<BankAccount> getAccounts()
    {
        return new ArrayList<>(accounts);
    }
    
    public BankAccount bankByOwner(String name, BankAccountOwnerType type)
    {
        for (BankAccount acc : accounts)
        {
            BankAccountOwner owner = acc.getOwner();
            
            if (owner.getType() == type && owner.getName().equals(name))
            {
                return acc;
            }
        }
        
        return null;
    }
    
    public boolean hasBankAccount(String name, BankAccountOwnerType type)
    {
        return bankByOwner(name, type) != null;
    }
    
    public BankAccount createBankAccount(Player owner, String name, BankAccountOwnerType type)
    {
        BankAccount acc;
        if (type == BankAccountOwnerType.PLAYER)
        {
            acc = new BankAccount(owner.getName());
        }
        else
        {
            acc = new BankAccount(owner.getName(), name);
        }
        accounts.add(acc);
        return acc;
    }
    
    public boolean removeBankAccount(String name, BankAccountOwnerType type)
    {
        BankAccount acc = bankByOwner(name, type);
        if (acc == null)
        {
            return false;
        }
        else
        {
            accounts.remove(acc);
            return true;
        }
    }
    
    public <T> T getConfig(String path, T def)
    {
        return (T) getConfig().get(path, def);
    }
}