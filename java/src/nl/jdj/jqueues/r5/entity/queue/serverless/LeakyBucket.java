package nl.jdj.jqueues.r5.entity.queue.serverless;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.event.SimEntityEvent;
import nl.jdj.jqueues.r5.event.SimEntityEventScheduler;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** In the {@link LeakyBucket} queue jobs depart without service in arrival order,
 *  but not at a higher rate than a given limit, at the expense of waiting.
 * 
 * <p>
 * This {@link SimQueue} is server-less.
 * 
 * <p>
 * The operation of this {@link SimQueue} is controlled by the {@code rateLimit} property, see {@link #getRateLimit}.
 * It must be non-negative and is set upon construction; and cannot be changed afterwards.
 * If set to zero, this queue behaves as {@link SINK}.
 * If so to positive infinite, this queue behaves as {@link ZERO}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see ZERO
 * @see SINK
 * @see DELAY
 * 
 */
public class LeakyBucket<J extends SimJob, Q extends LeakyBucket>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link LeakyBucket} queue given an event list and (departure) rate limit.
   *
   * @param eventList The event list to use.
   * @param rateLimit The (departure) rate limit, non-negative.
   *
   * @throws IllegalArgumentException If the rate limit is strictly negative.
   * 
   */
  public LeakyBucket (final SimEventList eventList, final double rateLimit)
  {
    super (eventList);
    if (rateLimit < 0)
      throw new IllegalArgumentException ();
    this.rateLimit = rateLimit;
    this.isRateLimited = (this.rateLimit == 0.0);
  }
  
  /** Returns a new {@link LeakyBucket} object on the same {@link SimEventList} with the same (departure) rate limit.
   * 
   * @return A new {@link LeakyBucket} object on the same {@link SimEventList} with the same (departure) rate limit.
   * 
   * @see #getEventList
   * @see #getRateLimit
   * 
   */
  @Override
  public LeakyBucket<J, Q> getCopySimQueue ()
  {
    return new LeakyBucket<> (getEventList (), getRateLimit ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "LeakyBucket[rateLimit]".
   * 
   * @return "LeakyBucket[rateLimit]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "LeakyBucket[" + getRateLimit () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE LIMIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double rateLimit;
  
  /** Returns the (immutable) rate limit for departures (non-negative).
   * 
   * <p>
   * The (departure) rate limit is set upon construction, and cannot be changed afterwards.
   * 
   * @return The (immutable) rate limit for departures (non-negative).
   * 
   */
  public final double getRateLimit ()
  {
    return this.rateLimit;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // IsRateLimited
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private boolean isRateLimited = false;
  
  /** Returns whether this queue is currently (departure) rate limited.
   * 
   * @return {code true} if this queue is currently (departure) rate limited.
   * 
   */
  protected final boolean isRateLimited ()
  {
    return this.isRateLimited;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code true} if there are no jobs present in the system and this queue is not {@link #isRateLimited}.
   * 
   * @return {@code true} if there are no jobs present in the system and this queue is not {@link #isRateLimited}.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return getNumberOfJobs () == 0 && ! isRateLimited ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and properly sets the rate limitation.
   * 
   * <p>
   * Since we always schedule {@link RateLimitExpirationEvent} through {@link #eventsScheduled},
   * our super-class will cancel them automatically (if needed).
   * 
   * @see #isRateLimited
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.isRateLimited = (this.rateLimit == 0.0);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Adds the job to the tail of the {@link #jobQueue}.
   * 
   * @see #arrive
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Makes the job depart if the queue is not rate-limited.
   * 
   * @see #isRateLimited
   * @see #depart
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! isRateLimited ())
      depart (time, job);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job, after passing sanity checks, from the job queue {@link #jobQueue}.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time)
  {
    if (job == null || ! this.jobQueue.contains (job))
      throw new IllegalArgumentException ();
    if (! this.jobsInServiceArea.isEmpty ())
      throw new IllegalStateException ();
    this.jobQueue.remove (job);
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE-LIMIT EXPIRATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notification of rate-limit expiration from an internally-scheduled {@link RateLimitExpirationEvent}.
   * 
   * <p>
   * If there are job in the waiting area, it makes depart the first one.
   * Otherwise, it invokes {@link #triggerPotentialNewNoWaitArmed}.
   * 
   * @param event The event that invoked us through its {@link SimEventAction}, non-{@code null}.
   * 
   */
  protected final void rateLimitExpiration (final RateLimitExpirationEvent<Q> event)
  {
    if (event == null)
      throw new RuntimeException ();
    if (! this.eventsScheduled.contains (event))
      throw new IllegalStateException ();
    this.eventsScheduled.remove (event);
    if (! this.isRateLimited)
      throw new IllegalStateException ();
    this.isRateLimited = false;
    final double time = event.getTime ();
    // Note: all jobs present are in waiting area; might as well check 'jobQueue' directly.
    if (! this.jobQueue.isEmpty ())
      depart (time, getFirstJobInWaitingArea ());
    else
      triggerPotentialNewNoWaitArmed (time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE-LIMIT EXPIRATION EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The default {@link SimEvent} used internally for scheduling rate-limit expirations.
   * 
   * <p>
   * The {@link RateLimitExpirationEvent} (actually, its {@link SimEventAction}), once activated,
   * calls {@link #rateLimitExpiration}.
   * 
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  protected final static class RateLimitExpirationEvent<Q extends LeakyBucket>
  extends SimEntityEvent<SimJob, Q>
  {
    
    /** Creates the actions that invokes {@link LeakyBucket#rateLimitExpiration} on the queue,
     *  and invokes the super method.
     * 
     * @param expirationTime The scheduled expiration time.
     * @param queue          The queue for which the rate-limit expiration is scheduled.
     * 
     * @throws IllegalArgumentException If the queue is {@code null}.
     * 
     */
    public RateLimitExpirationEvent
    (final double expirationTime,
     final Q queue)
    {
      super ("RateLimitExpiration@" + queue, expirationTime, queue, null, (SimEventAction) (final SimEvent event) ->
      {
        queue.rateLimitExpiration ((RateLimitExpirationEvent) event);
      });
    }   

    /** Throws an {@link UnsupportedOperationException}.
     * 
     * <p>
     * A {@link RateLimitExpirationEvent} is a queue-internal event.
     * 
     * @throws UnsupportedOperationException Always.
     * 
     */
    @Override
    public final SimEntityEvent<SimJob, Q> copyForQueue (final Q destQueue)
    {
      throw new UnsupportedOperationException ();
    }
    
  }

  /** Schedules a suitable {@link SimEvent} for a rate-limit expiration on the event list.
   * 
   * <p>
   * The implementation creates a new {@link RateLimitExpirationEvent},
   * adds it to {@link #eventsScheduled} and schedules the new event on the event list.
   * Effectively, this ensures that unless the event is canceled,
   * the method {@link #rateLimitExpiration} is invoked upon reaching the event.
   * 
   * @param expirationTime The scheduled expiration time.
   * 
   * @return The event created and scheduled on the event list.
   * 
   * @see #getEventList
   * @see #getLastUpdateTime
   * @see #eventsScheduled
   * 
   */
  protected final RateLimitExpirationEvent<Q> scheduleRateLimitExpirationEvent (final double expirationTime)
  {
    if (expirationTime < getLastUpdateTime ())
      throw new IllegalArgumentException ();
    final RateLimitExpirationEvent<Q> event = new RateLimitExpirationEvent<> (expirationTime, (Q) this);
    SimEntityEventScheduler.schedule (getEventList (), event);
    this.eventsScheduled.add (event);
    return event;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Removes the job from the {@link #jobQueue}.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
    if (! this.jobQueue.contains (departingJob))
      throw new IllegalStateException ();
    if (this.jobsInServiceArea.contains (departingJob))
      throw new IllegalStateException ();
    this.jobQueue.remove (departingJob);
  }

  /** Schedules a new {@link RateLimitExpirationEvent} if the departure-rate limit is finite and non-zero.
   * 
   * <p>
   * If the rate limit is zero or negative, this method throws an exception, as departures are not supposed to happen.
   * If the rate limit is {@link Double#POSITIVE_INFINITY},
   * this method departs the next job in the waiting area, i.c., {@link #getFirstJobInWaitingArea},
   * through {@link #depart} (if such a job is available).
   * 
   * @see #isRateLimited
   * @see #getRateLimit
   * @see #scheduleRateLimitExpirationEvent
   * @see #jobsInServiceArea
   * @see #getFirstJobInWaitingArea
   * @see #depart
   * 
   * @throws IllegalStateException If the rate limit is not strictly positive, or if this queue is currently under
   *                               rate-limitation, as determined through {@link #isRateLimited},
   *                               in both cases of which departures should not occur.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    if (isRateLimited () || this.rateLimit <= 0.0)
      throw new IllegalStateException ();
    if (Double.isFinite (this.rateLimit))
    {
      this.isRateLimited = true;
      scheduleRateLimitExpirationEvent (time + 1.0 / this.rateLimit);
    }
    else if (! this.jobsInServiceArea.isEmpty ())
      depart (time, getFirstJobInWaitingArea ());
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
