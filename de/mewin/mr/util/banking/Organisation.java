package de.mewin.mr.util.banking;

import de.mewin.util.SavingValue;
import de.mewin.util.ValueSaver;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class Organisation implements BankAccountOwner
{
    @SavingValue
    private String name;
    @SavingValue
    private String owner;
    @SavingValue
    private List<String> members;
    private ValueSaver saver;
    
    private Organisation()
    {
        this.saver = new ValueSaver(this);
    }
    
    public Organisation(String name, String owner)
    {
        this.name = name;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner);
        this.saver = new ValueSaver(this);
    }

    @Override
    public String getName()
    {
        return this.name;
    }
    
    public boolean hasMember(String name)
    {
        return members.contains(name);
    }
    
    public void addMember(String name)
    {
        this.members.add(name);
    }
    
    public boolean removeMember(String name)
    {
        if (name.equals(owner))
        {
            return false;
        }
        else
        {
            return members.remove(name);
        }
    }
    
    public String getOwner()
    {
        return this.owner;
    }
    
    public void setOwner(String newOwner)
    {
        if (!hasMember(newOwner))
        {
            addMember(newOwner);
        }
        
        this.owner = newOwner;
    }

    @Override
    public Object serialize()
    {
        return saver.getYaml();
    }
    
    private void load(List yaml)
    {
        saver.load(yaml);
    }
    
    public static Organisation deserialize(Object yaml)
    {
        Organisation org = new Organisation();
        org.load((List) yaml);
        return org;
    }
    
    @Override
    public BankAccountOwnerType getType()
    {
        return BankAccountOwnerType.ORGANISATION;
    }
}
