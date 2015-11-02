package nl.jdj.jqueues.r4;

import nl.jdj.jsimulation.r4.SimEventList;

/** A partial implementation of a {@link SimQueue}, taking care of listener and event-list management.
 * 
 * <p>All concrete subclasses of {@link AbstractSimQueueBase} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * <p>
 * This class (helped by its ancestor {@link AbstractSimEntity}))
 * takes care of storing the (final) event list, doing all listener management,
 * and firing all generic {@link SimQueue}-related events upon request from concrete subclasses.
 * 
 * <p>
 * For a more complete (though still partial) implementation, see {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractSimQueueBase<J extends SimJob, Q extends AbstractSimQueueBase>
  extends AbstractSimEntity<J, Q>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates an abstract queue given an event list.
   *
   * @param eventList The event list to use.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   * 
   */
  protected AbstractSimQueueBase (final SimEventList eventList)
  {
    super (eventList);
    if (eventList == null)
      throw new IllegalArgumentException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A private instance of a {@link StdOutSimQueueListener}.
   * 
   * @see #registerStdOutSimQueueListener
   * @see #unregisterStdOutSimQueueListener
   * 
   */
  private final StdOutSimQueueListener<J, Q> stdOutSimQueueListener = new StdOutSimQueueListener<> ();
  
  /** Registers the (private) {@link StdOutSimQueueListener} as listener (convenience method).
   * 
   * @see #unregisterStdOutSimQueueListener
   * @see #registerSimEntityListener
   * 
   */
  public final void registerStdOutSimQueueListener ()
  {
    registerSimEntityListener (this.stdOutSimQueueListener);
  }
  
  /** Unregisters the (private) {@link StdOutSimQueueListener} as listener, if registered (convenience method).
   * 
   * @see #registerStdOutSimQueueListener
   * @see #unregisterSimEntityListener
   * 
   */
  public final void unregisterStdOutSimQueueListener ()
  {
    unregisterSimEntityListener (this.stdOutSimQueueListener);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners upon an immediate upcoming update at this queue.
   *
   * @param time The current time, which has not been set yet on this queue.
   *
   * @see SimQueueListener#notifyUpdate
   * 
   */
  protected final void fireUpdate (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyUpdate (time, (Q) this);
  }
  
  /** Notifies all listeners of a change in the <code>noWaitArmed</code> property.
   * 
   * @param time        The current time.
   * @param noWaitArmed The new value of the <code>noWaitArmed</code> property.
   * 
   * @see SimQueue#isNoWaitArmed
   * @see SimQueueListener#notifyNewNoWaitArmed
   * 
   */
  protected final void fireNewNoWaitArmed (final double time, final boolean noWaitArmed)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyNewNoWaitArmed (time, (Q) this, noWaitArmed);
  }

  /** Notifies all vacation listeners of the start of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#startQueueAccessVacation
   * @see SimQueueListener#notifyStartQueueAccessVacation
   * 
   */
  protected final void fireStartQueueAccessVacation (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStartQueueAccessVacation (time, this);
  }
  
  /** Notifies all vacation listeners of the end of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#stopQueueAccessVacation
   * @see SimQueueListener#notifyStopQueueAccessVacation
   * 
   */
  protected final void fireStopQueueAccessVacation (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  /** Notifies all vacation listeners if this queue has run out of server-access credits.
   * 
   * This method actually checks first to see if indeed {@link #getServerAccessCredits} returns zero.
   * 
   * @param time The current time.
   * 
   * @see #getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueListener#notifyOutOfServerAccessCredits
   * 
   */
  protected final void fireIfOutOfServerAccessCredits (final double time)
  {
    if (getServerAccessCredits () == 0)
      for (SimEntityListener<J, Q> l : getSimEntityListeners ())
        if (l instanceof SimQueueListener)
          ((SimQueueListener) l).notifyOutOfServerAccessCredits (time, this);    
  }
  
  /** Notifies all vacation listeners that this queue has regained server-access credits.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueListener#notifyRegainedServerAccessCredits
   * 
   */
  protected final void fireRegainedServerAccessCredits (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyRegainedServerAccessCredits (time, this);    
  }

}
