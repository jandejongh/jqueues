package nl.jdj.jqueues.r4.event.simple;

import nl.jdj.jqueues.r4.event.SimEntityEvent;
import nl.jdj.jqueues.r4.event.SimQueueJobArrivalEvent;
import nl.jdj.jqueues.r4.event.SimQueueJobDepartureEvent;
import nl.jdj.jqueues.r4.event.SimQueueJobDropEvent;
import nl.jdj.jqueues.r4.event.SimQueueJobRevocationEvent;
import nl.jdj.jqueues.r4.event.SimQueueJobStartEvent;

/** A simple representation of a {@link SimEntityEvent}.
 * 
 * <p>
 * Because in Java enumerated types cannot be extended, we use single instances for the representations.
 * This class is mainly used in and intended for testing.
 * 
 */
public interface SimEntitySimpleEventType
{
  
  /** A member of this interface.
   * 
   */
  public final class Member
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

    /**
     * Gets the (fixed) name of the event type.
     *
     * @return The (fixed) name of the event type.
     *
     */
    public final String getName ()
    {
      return this.name;
    }

  }
  
  /** A job arrival.
   * 
   * @see SimQueueJobArrivalEvent
   * 
   */
  public static Member ARRIVAL = new Member ("ARRIVAL");
  
  /** A job drop.
   * 
   * @see SimQueueJobDropEvent
   * 
   */
  public static Member DROP = new Member ("DROP");
  
  /** A job revocation.
   * 
   * @see SimQueueJobRevocationEvent
   * 
   */
  public static Member REVOCATION = new Member ("REVOCATION");
  
  /** A job start.
   * 
   * @see SimQueueJobStartEvent
   * 
   */
  public static Member START = new Member ("START");
  
  /** A job departure.
   * 
   * @see SimQueueJobDepartureEvent
   * 
   */
  public static Member DEPARTURE = new Member ("DEPARTURE");
  
}
