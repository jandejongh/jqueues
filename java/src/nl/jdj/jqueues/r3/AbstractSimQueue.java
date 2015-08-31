package nl.jdj.jqueues.r3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventList;

/** A partial implementation of a {@link SimQueue}.
 * 
 * <p>All concrete subclasses of {@link AbstractSimQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractSimQueue<J extends SimJob, Q extends AbstractSimQueue>
  implements SimQueue<J, Q>
{
  
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
  
  /** The underlying event list for {@link SimQueue} operations
   *  (to be supplied and fixed in the constructor).
   *
   * Non-<code>null</code>.
   * 
   */
  protected final SimEventList eventList;

  /** Jobs currently in queue.
   * 
   * Note: this includes jobs in service (executing).
   *
   */
  protected final List<J> jobQueue = new ArrayList<> ();

  @Override
  public int getNumberOfJobs ()
  {
    return this.jobQueue.size ();
  }

  /** Jobs currently being executed by the server(s).
   *
   * Any job in this set must also be in {@link #jobQueue}.
   * 
   */
  protected final Set<J> jobsExecuting
    = new HashSet<> ();

  @Override
  public int getNumberOfJobsExecuting ()
  {
    return this.jobsExecuting.size ();
  }
  
  /** Events scheduled on behalf of this {@link SimQueue}.
   * 
   * Any events in this set must also be in the {@link #eventList}.
   *
   */
  protected final Set<SimEvent<J>> eventsScheduled
    = new HashSet<> ();

  /** Resets the last update time to negative infinity, removes all jobs without notifications,
   * and ends all vacations.
   * 
   */
  @Override
  public void reset ()
  {
    this.lastUpdateTime = Double.NEGATIVE_INFINITY;
    for (SimJob j : this.jobQueue)
      j.setQueue (null);
    this.jobQueue.clear ();
    this.jobsExecuting.clear ();
    // XXX Shouldn't we remove events from the event list?
    this.eventsScheduled.clear ();
    this.eventList.remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = false;
    this.serverAccessCredits = Integer.MAX_VALUE;
  }
  
  /** The last update time of this queue.
   * 
   */
  private double lastUpdateTime = Double.NEGATIVE_INFINITY;

  /** Gets the time of the last update of this queue.
   * 
   * @return The time of the last update of this queue.
   * 
   * @see #update
   * 
   */
  public final double getLastUpdateTime ()
  {
    return this.lastUpdateTime;
  }
  
  /** Updates this queue (primarily for internal use).
   * 
   * For a precise definition of an update of a queue, refer to {@link SimQueueListener#update}.
   * 
   * <p>
   * This method can be safely called by external parties at any time, at the expense of listener notifications.
   * Appropriate occasions for it are at the start and at the end of a simulation.
   * 
   * <p>
   * Because subclasses may present a more refined model of queue updates, this method is not <code>final</code>.
   * 
   * <p>
   * This implementation only notifies the queues listeners, and updates its internal time.
   * 
   * @param time The time of the update (i.c., the current time).
   * 
   * @see SimQueueListener#update
   * @see #fireUpdate
   * 
   */
  public void update (double time)
  {
    assert time >= this.lastUpdateTime;
    fireUpdate (time);
    this.lastUpdateTime = time;
  }
  
  /** The {@link SimEvent} used internally for scheduling {@link SimJob} departures.
   * 
   * Simplifying, a {@link NonPreemptiveQueue}, and several other classes of queues, only need to consider job arrivals and
   * job departures. In between such events, basically nothing changes from the viewpoint of the queue.
   * Therefore, a natural approach towards simulating a queue is to schedule the first departure event and just sit and wait
   * (really, that's all there is to it...).
   * 
   * <p>The {@link DepartureEvent}, once activated, simply calls {@link NonPreemptiveQueue#DEPARTURE_ACTION} with the {@link SimJob}
   * leaving as the event argument. Implementations are encouraged to avoid creation of {@link DepartureEvent}s for each departure,
   * but instead reuse a single instance.
   * 
   */
  protected class DepartureEvent extends SimEvent<J>
  {
    public DepartureEvent
      (final double time,
      final J job)
    {
      super (time, job, AbstractSimQueue.this.DEPARTURE_ACTION);
    }
  }

  private boolean isQueueAccessVacation = false;
  
  /** The single {@link SimEventAction} used to wakeup the queue from queue-access vacations.
   * 
   */
  protected final SimEventAction END_QUEUE_ACCESS_VACATION_ACTION
    = new SimEventAction<J> ()
          {
            @Override
            public void action
              (final SimEvent<J> event)
            {
              if (! AbstractSimQueue.this.isQueueAccessVacation)
                throw new IllegalStateException ();
              AbstractSimQueue.this.isQueueAccessVacation = false;
              AbstractSimQueue.this.fireStopQueueAccessVacation (event.getTime ());
            }
          };

  /** The single {@link SimEvent} used to wakeup the queue from queue-access vacations.
   * 
   */
  protected final SimEvent END_QUEUE_ACCESS_VACATION_EVENT
    = new SimEvent (0.0, null, END_QUEUE_ACCESS_VACATION_ACTION);

  @Override
  public void startQueueAccessVacation ()
  {
    boolean notify = ! this.isQueueAccessVacation;
    if (notify)
      update (this.eventList.getTime ());
    this.eventList.remove (END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = true;
    if (notify)
      fireStartQueueAccessVacation (this.eventList.getTime ());
  }

  @Override
  public void startQueueAccessVacation (double duration)
  {
    boolean notify = ! this.isQueueAccessVacation;
    if (duration < 0)
      throw new IllegalArgumentException ();
    if (notify)
      update (this.eventList.getTime ());
    this.eventList.remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.END_QUEUE_ACCESS_VACATION_EVENT.setTime (this.eventList.getTime () + duration);
    this.eventList.add (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = true;
    if (notify)
      fireStartQueueAccessVacation (this.eventList.getTime ());
  }

  @Override
  public void stopQueueAccessVacation ()
  {
    boolean notify = this.isQueueAccessVacation;
    if (notify)
      update (this.eventList.getTime ());
    this.eventList.remove (this.END_QUEUE_ACCESS_VACATION_EVENT);
    this.isQueueAccessVacation = false;
    if (notify)
      fireStopQueueAccessVacation (this.eventList.getTime ());
  }

  @Override
  public final boolean isQueueAccessVacation ()
  {
    return this.isQueueAccessVacation;
  }
  
  private int serverAccessCredits = Integer.MAX_VALUE;
  
  @Override
  public final int getServerAccessCredits ()
  {
    return this.serverAccessCredits;
  }
  
  @Override
  public final void setServerAccessCredits (final int credits)
  {
    if (credits < 0)
      throw new IllegalArgumentException ();
    final int oldCredits = this.serverAccessCredits;
    if (oldCredits != credits)
      update (this.eventList.getTime ()); 
    this.serverAccessCredits = credits;
    if (oldCredits == 0 && credits > 0)
      fireRegainedServerAccessCredits (this.lastUpdateTime);
    else if (oldCredits > 0 && credits == 0)
      fireOutOfServerAccessCredits (this.lastUpdateTime);
    if (oldCredits == 0 && credits > 0)
      rescheduleForNewServerAccessCredits (this.lastUpdateTime);
  }
  
  protected final boolean hasServerAcccessCredits ()
  {
    return this.serverAccessCredits > 0;
  }
  
  protected final void takeServerAccessCredit ()
  {
    if (this.serverAccessCredits <= 0)
      throw new IllegalStateException ();
    // Integer.MAX_VALUE is treated as infinity.
    if (this.serverAccessCredits < Integer.MAX_VALUE)
    {
      update (this.eventList.getTime ());      
      this.serverAccessCredits--;
      if (this.serverAccessCredits == 0)
        fireOutOfServerAccessCredits (this.lastUpdateTime);
    }
  }
  
  protected abstract void insertJobInQueueUponArrival (J job, double time);
  
  protected abstract void rescheduleAfterArrival (J job, double time);

  protected abstract void rescheduleForNewServerAccessCredits (double time);
  
  protected abstract void removeJobFromQueueUponDrop (J job, double time);
  
  protected abstract void rescheduleAfterDrop (J job, double time);
  
  protected abstract boolean removeJobFromQueueUponRevokation (J job, double time, boolean interruptService);
  
  protected abstract void rescheduleAfterRevokation (J job, double time);
  
  protected abstract void removeJobFromQueueUponDeparture (J departingJob, double time);
  
  /** Reschedules an event after a job departure.
   * 
   * Must be implemented by concrete subclasses.
   * All known subclasses of {@link NonPreemptiveQueue} schedule one or more departure events on the event list, see
   * {@link DepartureEvent}, which in turn invoke {@link #DEPARTURE_ACTION} for each departure.
   * The purpose of this method is to schedule a new {@link DepartureEvent} if needed.
   * Note that internal bookkeeping has already been done upon calling this method (i.e.,de facto, the job has already left the
   * queue). Also, there is no need to notify listeners or invoke registered job/queue actions of the departure event, as this
   * has already been done in {@link #DEPARTURE_ACTION}, just before this method is invoked.
   * 
   * <p>Subclasses are obviously free to use a different approach in terms of scheduling on the event list (i.e., they don't have
   * to schedule {@link #DEPARTURE_ACTION}s. They then need to make an empty implementation of this method, and mimic the
   * internal bookkeeping and notification policies in {@link #DEPARTURE_ACTION} upon job departures.
   * 
   * @param departedJob The departed job.
   * @param time The departure (current) time.
   * 
   */
  protected abstract void rescheduleAfterDeparture (J departedJob, double time);

  @Override
  public final void arrive (J job, double time)
  {
    if (job == null)
      throw new IllegalArgumentException ();
    if (job.getQueue () != null)
      throw new IllegalArgumentException ();
    if (this.jobQueue.contains (job))
      throw new RuntimeException ();
    update (time);
    insertJobInQueueUponArrival (job, time);
    job.setQueue (this);
    fireArrival (time, job);
    if (this.isQueueAccessVacation)
      drop (job, time);
    else
      rescheduleAfterArrival (job, time);
  }

  protected final void drop (J job, double time)
  {
    if (job == null || job.getQueue () != this || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    update (time);
    removeJobFromQueueUponDrop (job, time);
    job.setQueue (null);
    fireDrop (time, job);
    rescheduleAfterDrop (job, time);
  }

  @Override
  public final boolean revoke
    (final J job,
    final double time,
    final boolean interruptService)
  {
    if (job == null || job.getQueue () != this || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    update (time);
    final boolean revoked = removeJobFromQueueUponRevokation (job, time, interruptService);
    if (! revoked)
      return false;
    job.setQueue (null);
    fireRevocation (time, job);
    rescheduleAfterRevokation (job, time);
    return true;
  }

  /** A {@link SimEventAction} that is invoked when a job departs from the queue.
   *
   * This action takes care of administration of the internal data, i.e.,
   * clearing the job's queue {@link SimJob#setQueue},
   * removing it from the {@link #jobQueue}
   * and the {@link #jobsExecuting} lists,
   * and updating {@link #eventsScheduled}.
   * It then invokes {@link #fireDeparture} and thereafter
   * the discipline-specific {@link #rescheduleAfterDeparture} method.
   * 
   */
  protected final SimEventAction<J> DEPARTURE_ACTION
    = new SimEventAction<J> ()
          {
            @Override
            public void action
              (final SimEvent<J> event)
            {
              if (! AbstractSimQueue.this.eventsScheduled.contains (event))
                throw new IllegalStateException ();
              final double time = event.getTime ();
              final J job = event.getObject ();
              if (job.getQueue () != AbstractSimQueue.this)
                throw new IllegalStateException ();
              AbstractSimQueue.this.update (time);
              job.setQueue (null);
              AbstractSimQueue.this.removeJobFromQueueUponDeparture (job, time);
              AbstractSimQueue.this.eventsScheduled.remove (event);
              AbstractSimQueue.this.fireDeparture (job, event);
              AbstractSimQueue.this.rescheduleAfterDeparture (job, time);
            }
          };

  /** Creates an abstract queue given an event list.
   *
   * @param eventList The event list to use.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>.
   * 
   */
  protected AbstractSimQueue (final SimEventList eventList)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    this.eventList = eventList;
    this.eventList.addListener (this);
  }

  protected final List<SimEventAction> arrivalActions
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

  protected final List<SimEventAction> startActions
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

  protected final List<SimEventAction> dropActions
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

  protected final List<SimEventAction> revocationActions
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

  protected final List<SimEventAction> departureActions
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
   * @param time The current time, which has not been set on this queue.
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

  protected final void fireStartQueueAccessVacation (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyStartQueueAccessVacation (time, this);
  }
  
  protected final void fireStopQueueAccessVacation (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyStopQueueAccessVacation (time, this);
  }
  
  protected final void fireOutOfServerAccessCredits (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyOutOfServerAccessCredits (time, this);    
  }
  
  public void fireRegainedServerAccessCredits (double time)
  {
    for (SimQueueListener<J, Q> l : this.queueListeners)
      if (l instanceof SimQueueVacationListener)
        ((SimQueueVacationListener) l).notifyRegainedServerAccessCredits (time, this);    
  }

  /** Calls {@link #reset}.
   * 
   * {@inheritDoc}
   *
   */
  @Override
  public void notifyEventListReset (SimEventList eventList)
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
