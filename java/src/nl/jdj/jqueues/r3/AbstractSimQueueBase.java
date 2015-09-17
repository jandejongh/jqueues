package nl.jdj.jqueues.r3;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventList;
import nl.jdj.jsimulation.r3.SimEventListListener;

/** A partial implementation of a {@link SimQueue}, taking listener and event-list management.
 * 
 * <p>All concrete subclasses of {@link AbstractSimQueueBase} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * <p>
 * This class takes care of storing the (final) event list, and doing all listener management,
 * management of registered actions, and firing events upon request from concrete subclasses.
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

  /** The {@link SimQueueListener}s (including {@link SimQueueVacationListener}s) of this queue.
   * 
   */
  private final List<SimQueueListener<J, Q>> queueListeners = new ArrayList<> ();

  @Override
  public final void registerQueueListener (SimQueueListener<J, Q> listener)
  {
    if (listener == null)
      return;
    if (! this.queueListeners.contains (listener))
      this.queueListeners.add (listener);
  }

  @Override
  public final void unregisterQueueListener (SimQueueListener<J, Q> listener)
  {
    this.queueListeners.remove (listener);
  }  

  private final List<SimEventAction> arrivalActions
    = new ArrayList<> ();

  @Override
  public void addArrivalAction (final SimEventAction action)
  {
    if (action == null)
      return;
    if (this.arrivalActions.contains (action))
      return;
    this.arrivalActions.add (action);
  }

  @Override
  public void removeArrivalAction (final SimEventAction action)
  {
    this.arrivalActions.remove (action);
  }

  private final List<SimEventAction> startActions
    = new ArrayList<> ();

  @Override
  public void addStartAction (final SimEventAction action)
  {
    if (action == null)
      return;
    if (this.startActions.contains (action))
      return;
    this.startActions.add (action);
  }

  @Override
  public void removeStartAction (final SimEventAction action)
  {
    this.startActions.remove (action);
  }

  private final List<SimEventAction> dropActions
    = new ArrayList<> ();

  @Override
  public void addDropAction (final SimEventAction action)
  {
    if (action == null)
      return;
    if (this.dropActions.contains (action))
      return;
    this.dropActions.add (action);
  }

  @Override
  public void removeDropAction (final SimEventAction action)
  {
    this.dropActions.remove (action);
  }

  private final List<SimEventAction> revocationActions
    = new ArrayList<> ();

  @Override
  public void addRevocationAction (final SimEventAction action)
  {
    if (action == null)
      return;
    if (this.revocationActions.contains (action))
      return;
    this.revocationActions.add (action);
  }

  @Override
  public void removeRevocationAction (final SimEventAction action)
  {
    this.revocationActions.remove (action);
  }

  private final List<SimEventAction> departureActions
    = new ArrayList<> ();

  @Override
  public void addDepartureAction (final SimEventAction action)
  {
    if (action == null)
      return;
    if (this.departureActions.contains (action))
      return;
    this.departureActions.add (action);
  }

  @Override
  public void removeDepartureAction (final SimEventAction action)
  {
    this.departureActions.remove (action);
  }

  /** Notifies all listeners upon an immediate upcoming update at this queue.
   *
   * @param time The current time, which has not been set yet on this queue.
   *
   * @see SimQueueListener#update
   * 
   */
  protected final void fireUpdate (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.update (time, (Q) this);
  }
  
  /** Notifies all listeners and invoke all queue and job specific actions upon a job arrival at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param time The current time.
   * @param job The job.
   * 
   * @see SimQueue#arrive
   * @see #addArrivalAction
   * @see SimQueueListener#arrival
   * @see SimJob#getQueueArriveAction
   *
   */
  protected final void fireArrival (double time, J job)
  {
    for (SimEventAction<J> action: this.arrivalActions)
      action.action (new SimEvent (time, job, action));
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.arrival (time, job, (Q) this);
    final SimEventAction<J> aAction = job.getQueueArriveAction ();
    if (aAction != null)
      aAction.action (new SimEvent (time, job, aAction));
  }
  
  /** Notifies all listeners and invoke all queue and job specific actions upon a job starting at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see #addStartAction
   * @see SimQueueListener#start
   * @see SimJob#getQueueStartAction
   * 
   */
  protected final void fireStart (double time, J job)
  {
    for (SimEventAction<J> action: this.startActions)
      action.action (new SimEvent (time, job, action));
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.start (time, job, (Q) this);
    final SimEventAction<J> sAction = job.getQueueStartAction ();
    if (sAction != null)
      sAction.action (new SimEvent (time, job, sAction));
  }
  
  /** Notifies all listeners and invoke all queue and job specific actions upon a job drop at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see #addDropAction
   * @see SimQueueListener#drop
   * @see SimJob#getQueueDropAction
   * 
   */
  protected final void fireDrop (double time, J job)
  {
    for (SimEventAction<J> action: this.dropActions)
      action.action (new SimEvent (time, job, action));
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.drop (time, job, (Q) this);
    final SimEventAction<J> sAction = job.getQueueDropAction ();
    if (sAction != null)
      sAction.action (new SimEvent (time, job, sAction));    
  }
  
  /** Notifies all listeners and invoke all queue and job specific actions upon a successful job revocation at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see SimQueue#revoke
   * @see #addRevocationAction
   * @see SimQueueListener#revocation
   * @see SimJob#getQueueRevokeAction
   * 
   */
  protected final void fireRevocation (double time, J job)
  {
    for (SimEventAction<J> action: this.revocationActions)
      action.action (new SimEvent (time, job, action));
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.revocation (time, job, (Q) this);
    final SimEventAction<J> rAction = job.getQueueRevokeAction ();
    if (rAction != null)
      rAction.action (new SimEvent<> (time, job, rAction));
  }
  
  /** Notifies all listeners and invoke all queue and job specific actions of a job departure at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param job The job.
   * @param event The event causing the departure.
   *
   * @see #addDepartureAction
   * @see SimQueueListener#departure
   * @see SimJob#getQueueDepartureAction
   * 
   */
  protected final void fireDeparture (J job, SimEvent event)
  {
    for (SimEventAction<J> action: this.departureActions)
      action.action (event);
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.departure (event.getTime (), job, (Q) this);
    final SimEventAction<J> dAction = job.getQueueDepartAction ();
    if (dAction != null)
      dAction.action (event);
  }

  /** Notifies all listeners and invoke all queue and job specific actions of a job departure at this queue.
   *
   * This method first invokes any queue-specific actions, then informs the queue listeners and finally invokes job-specific
   * actions.
   *
   * @param time The current time.
   * @param job The job.
   *
   * @see #addDepartureAction
   * @see SimQueueListener#departure
   * @see SimJob#getQueueDepartureAction
   * 
   */
  protected final void fireDeparture (double time, J job)
  {
    for (SimEventAction<J> action: this.departureActions)
      action.action (new SimEvent (time, job, action));
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.departure (time, job, (Q) this);
    final SimEventAction<J> dAction = job.getQueueDepartAction ();
    if (dAction != null)
      dAction.action (new SimEvent (time, job, dAction));
  }

  /** Notifies all vacation listeners of the start of a queue-access vacation.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#startQueueAccessVacation
   * @see SimQueueVacationListener#notifyStartQueueAccessVacation
   * 
   */
  protected final void fireStartQueueAccessVacation (double time)
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
  protected final void fireStopQueueAccessVacation (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  /** Notifies all vacation listeners that this queue has run out of server-access credits.
   * 
   * @param time The current time.
   * 
   * @see SimQueue#setServerAccessCredits
   * @see SimQueueVacationListener#notifyOutOfServerAccessCredits
   * 
   */
  protected final void fireOutOfServerAccessCredits (double time)
  {
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
  protected final void fireRegainedServerAccessCredits (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyRegainedServerAccessCredits (time, this);    
  }

  /** Calls {@link #reset}.
   * 
   * {@inheritDoc}
   *
   * <p>
   * This method is <code>final</code>; use {@link #reset} to override/augment behavior.
   * 
   */
  @Override
  public final void notifyEventListReset (SimEventList eventList)
  {
    reset ();
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public void notifyEventListUpdate (SimEventList eventList, double time)
  {
  }

  /** Does nothing.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public void notifyEventListEmpty (SimEventList eventList, double time)
  {
  }
  
}
