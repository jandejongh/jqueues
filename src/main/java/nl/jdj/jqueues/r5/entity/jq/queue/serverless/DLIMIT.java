/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.jdj.jqueues.r5.entity.jq.queue.serverless;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimJQEventScheduler;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** In the {@link DLIMIT} queue jobs depart without service in arrival order,
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
 * The {@link DLIMIT} queueing discipline is equivalent to {@link LeakyBucket} with infinite buffer size.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see ZERO
 * @see SINK
 * @see DELAY
 * @see LeakyBucket
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
public class DLIMIT<J extends SimJob, Q extends DLIMIT>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link DLIMIT} queue with infinite buffer size given an event list and (departure) rate limit.
   *
   * @param eventList The event list to use.
   * @param rateLimit The (departure) rate limit, non-negative.
   *
   * @throws IllegalArgumentException If the rate limit is strictly negative.
   * 
   */
  public DLIMIT (final SimEventList eventList, final double rateLimit)
  {
    this (eventList, Integer.MAX_VALUE, rateLimit);
  }
  
  /** Creates a {@link DLIMIT} queue with infinite buffer size given an event list and (departure) rate limit.
   *
   * <p>
   * This method is package private for use by {@link LeakyBucket}.
   * 
   * @param eventList  The event list to use.
   * @param bufferSize The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   * @param rateLimit  The (departure) rate limit, non-negative.
   *
   * @throws IllegalArgumentException If the rate limit is strictly negative.
   * 
   */
  DLIMIT (final SimEventList eventList, final int bufferSize, final double rateLimit)
  {
    super (eventList, bufferSize);
    if (rateLimit < 0)
      throw new IllegalArgumentException ();
    this.rateLimit = rateLimit;
    this.isRateLimited = (this.rateLimit == 0.0);    
  }
  
  /** Returns a new {@link DLIMIT} object on the same {@link SimEventList} with the same (departure) rate limit.
   * 
   * @return A new {@link DLIMIT} object on the same {@link SimEventList} with the same (departure) rate limit.
   * 
   * @see #getEventList
   * @see #getRateLimit
   * 
   */
  @Override
  public DLIMIT<J, Q> getCopySimQueue ()
  {
    return new DLIMIT<> (getEventList (), getRateLimit ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "DLIMIT[rateLimit]".
   * 
   * @return "DLIMIT[rateLimit]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "DLIMIT[" + getRateLimit () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE DLIMIT
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
  // QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Class getQoSClass ()
  {
    return super.getQoSClass ();
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final Object getQoS ()
  {
    return super.getQoS ();
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
  

  /** Does nothing.
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Makes the job depart if the queue is not rate-limited; otherwise drop it if the waiting queue is "overflown".
   * 
   * @see #isRateLimited
   * @see #depart
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! isRateLimited ())
      depart (time, job);
    else if (getBufferSize () < Integer.MAX_VALUE && getNumberOfJobsInWaitingArea () > getBufferSize ())
      drop (job, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDrop (final J job, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
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
   * Invokes {@link #update}
   * and {@link #clearAndUnlockPendingNotificationsIfLocked},
   * insisting to be a top-level event (at the expense of an {@link IllegalStateException}).
   * 
   * <p>
   * If there are job in the waiting area, it makes depart the first one.
   * Otherwise, it invokes {@link #triggerPotentialNewStartArmed}.
   * 
   * <p>
   * Finally, it notifies listeners through {@link #fireAndLockPendingNotifications}.
   * 
   * @param event The event that invoked us through its {@link SimEventAction}, non-{@code null}.
   * 
   * @see RateLimitExpirationEvent
   * @see #eventsScheduled
   * @see #isRateLimited
   * @see #hasJobsInWaitingArea
   * @see #depart
   * @see #triggerPotentialNewStartArmed
   * @see #clearAndUnlockPendingNotificationsIfLocked
   * @see #fireAndLockPendingNotifications
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
    update (time);
    final boolean isTopLevel = clearAndUnlockPendingNotificationsIfLocked ();
    if (! isTopLevel)
      throw new IllegalStateException ();
    if (hasJobsInWaitingArea ())
      depart (time, getFirstJobInWaitingArea ());
    else
      triggerPotentialNewStartArmed (time);
    fireAndLockPendingNotifications ();
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
  protected final static class RateLimitExpirationEvent<Q extends DLIMIT>
  extends SimQueueEvent<SimJob, Q>
  {
    
    /** Creates the actions that invokes {@link DLIMIT#rateLimitExpiration} on the queue,
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
      super ("RateLimitExpiration@" + queue, expirationTime, queue, (SimEventAction) (final SimEvent event) ->
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
    public final RateLimitExpirationEvent<Q> copyForQueueAndJob (final Q newQueue, final SimJob newJob)
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
    SimJQEventScheduler.scheduleJQ (getEventList (), event);
    this.eventsScheduled.add (event);
    return event;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponDeparture (final J departingJob, final double time)
  {
  }

  /** Schedules a new {@link RateLimitExpirationEvent} if the departure-rate limit is finite and non-zero.
   * 
   * <p>
   * If the rate limit is zero or negative, this method throws an exception, as departures are not supposed to happen.
   * 
   * @see #isRateLimited
   * @see #getRateLimit
   * @see #scheduleRateLimitExpirationEvent
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
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
