package nl.jdj.jqueues.r2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nl.jdj.jsimulation.r3.SimEvent;
import nl.jdj.jsimulation.r3.SimEventAction;
import nl.jdj.jsimulation.r3.SimEventList;

/** A partial implementation of a {@link SimQueue}.
 * 
 * <p>All concrete subclasses of {@link AbstractQueue} take
 * the {@link SimEventList} used for event scheduling and processing as one of their arguments upon construction.
 * It is up to the caller to properly start processing the event list.
 * 
 * @param <J> The type of {@link SimJobs}s supported.
 * @param <Q> The type of {@link SimQueues}s supported.
 * 
 */
public abstract class AbstractSimQueue<J extends SimJob, Q extends AbstractSimQueue>
  implements SimQueue<J, Q>
{
  
  protected final List<SimQueueListener<J, Q>> queueListeners = new ArrayList<> ();

  @Override
  public void registerQueueListener (SimQueueListener<J, Q> listener)
  {
    if (listener == null)
      return;
    if (! this.queueListeners.contains (listener))
      this.queueListeners.add (listener);
  }

  @Override
  public void unregisterQueueListener (SimQueueListener<J, Q> listener)
  {
    this.queueListeners.remove (listener);
  }  
  
  /** The underlying event list for {@link SimQueue} operations
   *  (to be supplied and fixed in the constructor).
   *
   */
  protected final SimEventList eventList;

  /** Jobs currently in queue.
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
   */
  protected final Set<SimEvent<J>> eventsScheduled
    = new HashSet<> ();

  private double lastUpdateTime = Double.NEGATIVE_INFINITY;

  /** Resets the last update time to negative infinity, and removes all jobs without notifications.
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
    this.eventsScheduled.clear ();
  }
  
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
   * @see #notifyUpdate
   * 
   */
  public void update (double time)
  {
    assert time >= this.lastUpdateTime;
    notifyUpdate (time);
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

  /** A {@link SimEventAction} that is invoked when a job departs from the queue.
   *
   * This action takes care of administration of the internal data, i.e.,
   * clearing the job's queue {@link SimJob#setQueue},
   * removing it from the {@link #jobQueue}
   * and the {@link #jobsExecuting} lists,
   * and updating {@link #eventsScheduled}.
   * It then invokes {@link #notifyDeparture} and thereafter
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
              final double time = event.getTime ();
              // System.out.println ("Departure from queue @" + time);
              final J job = event.getObject ();
              assert job.getQueue () == AbstractSimQueue.this;
              AbstractSimQueue.this.update (time);
              job.setQueue (null);
              boolean found;
              found = AbstractSimQueue.this.jobQueue.remove (job);
              assert found;
              found = AbstractSimQueue.this.jobsExecuting.remove (job);
              assert found;
              found = AbstractSimQueue.this.eventsScheduled.remove (event);
              assert found;
              AbstractSimQueue.this.notifyDeparture (job, event);
              AbstractSimQueue.this.rescheduleAfterDeparture (job, time);
            }
          };

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
  protected abstract void rescheduleAfterDeparture
    (J departedJob, double time);

  /** Creates an abstract queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  protected AbstractSimQueue (final SimEventList eventList)
  {
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
  protected final void notifyUpdate (double time)
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
  protected final void notifyArrival (double time, J job)
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
  protected final void notifyStart (double time, J job)
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
  protected final void notifyDrop (double time, J job)
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
  protected final void notifyRevocation (double time, J job)
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
  protected final void notifyDeparture (J job, SimEvent event)
  {
    for (SimEventAction<J> action: this.departureActions)
      action.action (event);
    for (SimQueueListener<J, Q> l : this.queueListeners)
      l.departure (event.getTime (), job, (Q) this);
    final SimEventAction<J> dAction = job.getQueueDepartAction ();
    if (dAction != null)
      dAction.action (event);
  }

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
