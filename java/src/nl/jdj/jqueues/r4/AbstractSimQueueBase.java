package nl.jdj.jqueues.r4;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
import nl.jdj.jsimulation.r4.SimEventListListener;

/** A partial implementation of a {@link SimQueue}, taking care of listener and event-list management.
 * 
 * <p>All concrete subclasses of {@link AbstractSimQueueBase} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * <p>
 * This class takes care of storing the (final) event list, doing all listener management,
 * and firing events upon request from concrete subclasses.
 * It also implements a rudimentary (and override-able) {@link SimEventListListener},
 * invoking {@link SimQueue#reset} when the event list resets.
 * 
 * <p>
 * For a more complete (though still partial) implementation, see {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractSimQueueBase<J extends SimJob, Q extends AbstractSimQueueBase>
  implements SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // eventList
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The underlying event list for {@link SimQueue} operations
   *  (to be supplied and fixed in the constructor).
   *
   * Non-<code>null</code>.
   * 
   */
  private final SimEventList eventList;
  
  /** Returns the underlying event list for {@link SimQueue} operations.
   * 
   * The event list is fixed, non-<code>null</code> and supplied in the constructor.
   * 
   * @return The underlying event list, immutable and non-<code>null</code>.
   * 
   */
  public final SimEventList getEventList ()
  {
    return this.eventList;
  }
  
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
    if (eventList == null)
      throw new IllegalArgumentException ();
    this.eventList = eventList;
    this.eventList.addListener (this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LISTENERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The {@link SimQueueListener}s (including {@link SimQueueVacationListener}s) of this queue.
   * 
   */
  private final List<SimQueueListener<J, Q>> queueListeners = new ArrayList<> ();

  @Override
  public final void registerQueueListener (final SimQueueListener<J, Q> listener)
  {
    if (listener == null)
      return;
    if (! this.queueListeners.contains (listener))
      this.queueListeners.add (listener);
  }

  @Override
  public final void unregisterQueueListener (final SimQueueListener<J, Q> listener)
  {
    this.queueListeners.remove (listener);
  }  

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EVENT NOTIFICATIONS
  //  - reset
  //  - update
  //  - arrival
  //  - start
  //  - drop
  //  - revocation
  //  - departure
  //  - queue-access vacation
  //  - server-access credits
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a reset.
   *
   * @param oldTime The (old) time of the reset.
   *
   * @see SimQueueListener#notifyReset
   * 
   */
  protected final void fireReset (final double oldTime)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyReset (oldTime, (Q) this);
  }
  
  /** Notifies all listeners upon an immediate upcoming update at this queue.
   *
   * @param time The current time, which has not been set yet on this queue.
   *
   * @see SimQueueListener#notifyUpdate
   * 
   */
  protected final void fireUpdate (final double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyUpdate (time, (Q) this);
  }
  
  /** Notifies all listeners and invokes job specific actions upon a job arrival at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param time The current time.
   * @param job The job.
   * 
   * @see SimQueue#arrive
   * @see SimQueueListener#notifyArrival
   * @see SimJob#getQueueArriveAction
   *
   */
  protected final void fireArrival (final double time, final J job)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyArrival (time, job, (Q) this);
    final SimEventAction<J> aAction = job.getQueueArriveAction ();
    if (aAction != null)
      aAction.action (new SimEvent (time, job, aAction));
  }
  
  /** Notifies all listeners and invokes job specific actions upon a job starting at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see SimQueueListener#notifyStart
   * @see SimJob#getQueueStartAction
   * 
   */
  protected final void fireStart (final double time, final J job)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyStart (time, job, (Q) this);
    final SimEventAction<J> sAction = job.getQueueStartAction ();
    if (sAction != null)
      sAction.action (new SimEvent (time, job, sAction));
  }
  
  /** Notifies all listeners and invokes job specific actions upon a job drop at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see SimQueueListener#notifyDrop
   * @see SimJob#getQueueDropAction
   * 
   */
  protected final void fireDrop (final double time, final J job)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyDrop (time, job, (Q) this);
    final SimEventAction<J> sAction = job.getQueueDropAction ();
    if (sAction != null)
      sAction.action (new SimEvent (time, job, sAction));    
  }
  
  /** Notifies all listeners and invokes job specific actions upon a successful job revocation at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see SimQueue#revoke
   * @see SimQueueListener#notifyRevocation
   * @see SimJob#getQueueRevokeAction
   * 
   */
  protected final void fireRevocation (final double time, final J job)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyRevocation (time, job, (Q) this);
    final SimEventAction<J> rAction = job.getQueueRevokeAction ();
    if (rAction != null)
      rAction.action (new SimEvent<> (time, job, rAction));
  }
  
  /** Notifies all listeners and invokes job specific actions of a job departure at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param job The job.
   * @param event The event causing the departure.
   *
   * @see SimQueueListener#notifyDeparture
   * @see SimJob#getQueueDepartAction
   * 
   */
  protected final void fireDeparture (final J job, final SimEvent event)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyDeparture (event.getTime (), job, (Q) this);
    final SimEventAction<J> dAction = job.getQueueDepartAction ();
    if (dAction != null)
      dAction.action (event);
  }

  /** Notifies all listeners and invokes job specific actions of a job departure at this queue.
   *
   * This method first informs the queue listeners and then invokes job-specific actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see SimQueueListener#notifyDeparture
   * @see SimJob#getQueueDepartAction
   * 
   */
  protected final void fireDeparture (final double time, final J job)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyDeparture (time, job, (Q) this);
    final SimEventAction<J> dAction = job.getQueueDepartAction ();
    if (dAction != null)
      dAction.action (new SimEvent (time, job, dAction));
  }
  
  /** Notifies all listeners of a change in the <code>noWaitArmed</code> property.
   * 
   * @param t The current time.
   * @param noWaitArmed The new value of the <code>noWaitArmed</code> property.
   * 
   * @see SimQueue#isNoWaitArmed
   * @see SimQueueListener#notifyNewNoWaitArmed
   * 
   */
  protected final void fireNewNoWaitArmed (final double t, final boolean noWaitArmed)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.notifyNewNoWaitArmed (t, (Q) this, noWaitArmed);
  }

  /** Notifies all vacation listeners of the start of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#startQueueAccessVacation
   * @see SimQueueVacationListener#notifyStartQueueAccessVacation
   * 
   */
  protected final void fireStartQueueAccessVacation (final double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyStartQueueAccessVacation (time, this);
  }
  
  /** Notifies all vacation listeners of the end of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#stopQueueAccessVacation
   * @see SimQueueVacationListener#notifyStopQueueAccessVacation
   * 
   */
  protected final void fireStopQueueAccessVacation (final double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  /** Notifies all vacation listeners if this queue has run out of server-access credits.
   * 
   * This method actually checks first to see if indeed {@link #getServerAccessCredits} returns zero.
   * 
   * @param time The current time.
   * 
   * @see #getServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueVacationListener#notifyOutOfServerAccessCredits
   * 
   */
  protected final void fireIfOutOfServerAccessCredits (final double time)
  {
    if (getServerAccessCredits () == 0)
      for (SimQueueListener<J, Q> l : this.queueListeners)
        if (l instanceof SimQueueVacationListener)
          ((SimQueueVacationListener) l).notifyOutOfServerAccessCredits (time, this);    
  }
  
  /** Notifies all vacation listeners that this queue has regained server-access credits.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueVacationListener#notifyRegainedServerAccessCredits
   * 
   */
  protected final void fireRegainedServerAccessCredits (final double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyRegainedServerAccessCredits (time, this);    
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimEventListListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls {@link #reset}.
   * 
   * {@inheritDoc}
   *
   * <p>
   * This method is <code>final</code>; use {@link #reset} to override/augment behavior.
   * 
   */
  @Override
  public final void notifyEventListReset (final SimEventList eventList)
  {
    reset ();
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void notifyEventListUpdate (final SimEventList eventList, final double time)
  {
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void notifyEventListEmpty (final SimEventList eventList, final double time)
  {
  }
  
}
