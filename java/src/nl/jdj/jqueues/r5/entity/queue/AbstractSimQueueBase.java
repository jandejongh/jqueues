package nl.jdj.jqueues.r5.entity.queue;

import nl.jdj.jqueues.r5.SimEntityListener;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;
import nl.jdj.jqueues.r5.entity.AbstractSimEntity;
import nl.jdj.jqueues.r5.listener.StdOutSimQueueListener;
import nl.jdj.jsimulation.r5.SimEventList;

/** A partial implementation of a {@link SimQueue}, taking care of listeners, notifications, and event-list management.
 * 
 * <p>
 * All concrete subclasses of {@link AbstractSimQueueBase} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * <p>
 * This class (helped by its ancestor {@link AbstractSimEntity}))
 * takes care of storing the (final) event list, doing all listener management,
 * and firing all generic {@link SimQueue}-related events upon request from concrete subclasses.
 * In addition, it supports caching of the last reported {@code noWaitArmed} state
 * and of the availability of server-access credits.
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
   * <p>
   * Registers {@link #setInitNoWaitArmed} as a pre-event hook in order to find the initial {@code noWaitArmed} state.
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
    registerPreEventHook (this::setInitNoWaitArmed);
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
  // RESET ENTITY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Invokes super method, and clears the cache of the previous server-access-credits availability
   *  and of the {@code NoWaitArmed} state.
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.previousSacAvailability = true; // Every SimQueue must have infinite sacs upon construction and after reset.
    this.previousNoWaitArmedSet = false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SAC AVAILABILITY CACHING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The previous reported SAC availability.
   * 
   * <p>
   * Set to {@code true} upon construction and upon reset, since by contract,
   * the number of server-access credits is infinite then.
   * 
   */
  private boolean previousSacAvailability = true; // Every SimQueue must have infinite sacs upon construction and after reset.
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NoWaitArmed CACHING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean previousNoWaitArmedSet = false;
  
  private boolean previousNoWaitArmed = false;
  
  /** Sets the initial state (after construction or after a reset) of {@code NoWaitArmed}.
   * 
   * <p>
   * This method is called as a pre-event hook, and not meant to be called from user code (in sub-classes).
   * It is left protected for {@code javadoc}.
   * 
   * @param newTime The new time.
   * 
   */
  protected final void setInitNoWaitArmed (final double newTime)
  {
    if (! this.previousNoWaitArmedSet)
    {
      this.previousNoWaitArmedSet = true;
      this.previousNoWaitArmed = isNoWaitArmed ();
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all queue listeners of the start of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#setQueueAccessVacation
   * @see SimQueueListener#notifyStartQueueAccessVacation
   * 
   */
  protected final void fireStartQueueAccessVacation (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStartQueueAccessVacation (time, this);
  }
  
  /** Notifies all queue listeners of the end of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#setQueueAccessVacation
   * @see SimQueueListener#notifyStopQueueAccessVacation
   * 
   */
  protected final void fireStopQueueAccessVacation (final double time)
  {
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  /** Notifies all queue listeners that this queue has run out of server-access credits.
   * 
   * <p>
   * The reported server-access-credits availability is cached internally in order to detect changes.
   * 
   * @param time The current time.
   * 
   * @throws IllegalStateException If the queue still has server-access credits.
   * 
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueListener#notifyOutOfServerAccessCredits
   * @see #fireIfOutOfServerAccessCredits
   * @see #fireIfNewServerAccessCreditsAvailability
   * 
   */
  protected final void fireOutOfServerAccessCredits (final double time)
  {
    if (getServerAccessCredits () != 0)
      throw new IllegalStateException ();
    this.previousSacAvailability = false;
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyOutOfServerAccessCredits (time, this);    
  }
  
  /** Notifies all queue listeners <i>if</i> this queue has run out of server-access credits.
   * 
   * <p>
   * This method actually checks first to see if indeed {@link #getServerAccessCredits} returns zero.
   * 
   * <p>
   * If applicable, the reported server-access-credits availability is cached internally in order to detect changes.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see #fireOutOfServerAccessCredits
   * @see #fireIfNewServerAccessCreditsAvailability
   * 
   */
  protected final void fireIfOutOfServerAccessCredits (final double time)
  {
    if (getServerAccessCredits () == 0)
      fireOutOfServerAccessCredits (time);
  }
  
  /** Notifies all queue listeners that this queue has regained server-access credits.
   * 
   * <p>
   * The reported server-access-credits availability is cached internally in order to detect changes.
   * 
   * @param time The current time.
   * 
   * @throws IllegalStateException If the queue has <i>no</i> server-access credits <i>after all</i>.
   * 
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueListener#notifyRegainedServerAccessCredits
   * @see #fireIfNewServerAccessCreditsAvailability
   * 
   */
  protected final void fireRegainedServerAccessCredits (final double time)
  {
    if (getServerAccessCredits () == 0)
      throw new IllegalStateException ();
    this.previousSacAvailability = true;
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyRegainedServerAccessCredits (time, this);    
  }

  /** Assesses whether the availability of server-access credits has changed since the last notification (or since the last reset,
   *  or since construction), and if so, notifies all queue listeners appropriately.
   * 
   * <p>
   * The reported server-access-credits availability is cached internally in order to detect changes.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see #fireOutOfServerAccessCredits
   * @see #fireRegainedServerAccessCredits
   * 
   */
  protected final void fireIfNewServerAccessCreditsAvailability (final double time)
  {
    final boolean sacAvailability = (getServerAccessCredits () > 0);
    if (sacAvailability != this.previousSacAvailability)
    {
      if (sacAvailability)
        fireRegainedServerAccessCredits (time);
      else
        fireOutOfServerAccessCredits (time);
    }
  }
  
  /** Notifies all queue listeners of a change in the <code>noWaitArmed</code> property.
   * 
   * <p>
   * The reported {@code noWaitArmed} state is cached internally in order to detect changes.
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
    this.previousNoWaitArmed = noWaitArmed;
    for (SimEntityListener<J, Q> l : getSimEntityListeners ())
      if (l instanceof SimQueueListener)
        ((SimQueueListener) l).notifyNewNoWaitArmed (time, (Q) this, noWaitArmed);
  }

  /** Only if necessary, notifies all queue listeners of a suspected change in the <code>noWaitArmed</code> property.
   * 
   * <p>
   * This method does nothing if the {@code noWaitArmed} argument matches that of the previously reported state.
   * 
   * <p>
   * If applicable, the reported {@code noWaitArmed} state is cached internally in order to detect changes.
   * 
   * @param time        The current time.
   * @param noWaitArmed The actual, possibly new, new value of the <code>noWaitArmed</code> property.
   * 
   * @see #fireNewNoWaitArmed
   * @see SimQueue#isNoWaitArmed
   * @see SimQueueListener#notifyNewNoWaitArmed
   * 
   */
  protected final void fireIfNewNoWaitArmed (final double time, final boolean noWaitArmed)
  {
    if (! this.previousNoWaitArmedSet)
      throw new IllegalStateException ();
    if (noWaitArmed != this.previousNoWaitArmed)
      fireNewNoWaitArmed (time, noWaitArmed);
  }

}
