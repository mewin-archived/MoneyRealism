
package de.mewin.mr.util.banking;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public class PlayerOwner implements BankAccountOwner
{
    private String player;
    
    public PlayerOwner(String player)
    {
        this.player = player;
    }
    
    @Override
    public String getName()
    {
        return player;
    }

    @Override
    public Object serialize()
    {
        return player;
    }
    
    public static PlayerOwner deserialize(Object obj)
    {
        return new PlayerOwner((String) obj);
    }
    
    @Override
    public BankAccountOwnerType getType()
    {
        return BankAccountOwnerType.PLAYER;
    }
}
