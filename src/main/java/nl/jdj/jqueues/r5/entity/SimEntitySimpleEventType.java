package nl.jdj.jqueues.r5.entity;

/** A simple representation of a {@link SimEntityEvent}.
 * 
 * <p>
 * Because in Java enumerated types cannot be extended, we use single instances for the representations.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public interface SimEntitySimpleEventType
{
  
  /** A member of this interface.
   * 
   */
  public class Member
  {

    /** Creates a member with given name.
     * 
     * @param name The name.
     * 
     */
    public Member (final String name)
    {
      this.name = name;
    }

    private final String name;

    /** Gets the (fixed) name of the event type.
     *
     * @return The (fixed) name of the event type.
     *
     */
    public final String getName ()
    {
      return this.name;
    }

    @Override
    public String toString ()
    {
      if (this.name != null)
        return this.name;
      else
        return super.toString ();
    }
    
  }
  
  /** An entity reset.
   * 
   * @see SimEntity#resetEntity
   * 
   */
  public static Member RESET = new Member ("RESET");
  
  /** An entity update.
   * 
   * @see SimEntity#update
   * 
   */
  public static Member UPDATE = new Member ("UPDATE");
  
}
