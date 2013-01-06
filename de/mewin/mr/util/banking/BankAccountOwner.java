package de.mewin.mr.util.banking;

import de.mewin.util.Saveable;

/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public interface BankAccountOwner extends Saveable
{
    public abstract String getName();
    
    public abstract BankAccountOwnerType getType();
}
