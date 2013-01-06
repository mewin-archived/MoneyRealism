package de.mewin.util;


/**
 *
 * @author mewin<mewin001@hotmail.de>
 */
public abstract class Saver
{
    public abstract Class[] getClasses();
    
    public abstract Object serialize(Object obj);
    
    public abstract Object deserialize(Object obj);
}
