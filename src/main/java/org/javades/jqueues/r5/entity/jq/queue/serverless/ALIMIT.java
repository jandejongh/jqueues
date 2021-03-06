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
package org.javades.jqueues.r5.entity.jq.queue.serverless;

import org.javades.jqueues.r5.entity.jq.SimJQEventScheduler;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;

/** In the {@link ALIMIT} queue jobs depart without service in arrival order,
 *  but are dropped if they exceed a give arrival-rate limit.
 * 
 * <p>
 * This {@link SimQueue} is server-less, actually, without waiting area as well (it never exposes jobs present).
 * 
 * <p>
 * The operation of this {@link SimQueue} is controlled by the {@code rateLimit} property, see {@link #getRateLimit}.
 * It must be non-negative and is set upon construction; and cannot be changed afterwards.
 * If set to zero, this queue behaves as {@link DROP}.
 * If so to positive infinite, this queue behaves as {@link ZERO}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see DROP
 * @see ZERO
 * @see DLIMIT
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
public class ALIMIT<J extends SimJob, Q extends ALIMIT>
extends AbstractServerlessSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link ALIMIT} queue with (irrelevant) infinite buffer size given an event list and (arrival) rate limit.
   *
   * @param eventList The event list to use.
   * @param rateLimit The (arrival) rate limit, non-negative.
   *
   * @throws IllegalArgumentException If the rate limit is strictly negative.
   * 
   */
  public ALIMIT (final SimEventList eventList, final double rateLimit)
  {
    super (eventList, Integer.MAX_VALUE);
    if (rateLimit < 0)
      throw new IllegalArgumentException ();
    this.rateLimit = rateLimit;
    this.isRateLimited = (this.rateLimit == 0.0);    
  }
  
  /** Returns a new {@link ALIMIT} object on the same {@link SimEventList} with the same (arrival) rate limit.
   * 
   * @return A new {@link ALIMIT} object on the same {@link SimEventList} with the same (arrival) rate limit.
   * 
   * @see #getEventList
   * @see #getRateLimit
   * 
   */
  @Override
  public ALIMIT<J, Q> getCopySimQueue ()
  {
    return new ALIMIT<> (getEventList (), getRateLimit ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "ALIMIT[rateLimit]".
   * 
   * @return "ALIMIT[rateLimit]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "ALIMIT[" + getRateLimit () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE DLIMIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double rateLimit;
  
  /** Returns the (immutable) rate limit for arrivals (non-negative).
   * 
   * <p>
   * The (arrival) rate limit is set upon construction, and cannot be changed afterwards.
   * 
   * @return The (immutable) rate limit for arrivals (non-negative).
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
  
  /** Returns whether this queue is currently (arrival) rate limited.
   * 
   * @return {code true} if this queue is currently (arrival) rate limited.
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

  /** Drops the job if the queue is currently rate-limited,
   *  but makes it depart immediately otherwise, scheduling a new {@link RateLimitExpirationEvent} if needed.
   * 
   * @see #isRateLimited
   * @see #depart
   * @see #drop
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void rescheduleAfterArrival (final J job, final double time)
  {
    if (! isRateLimited ())
    {
      depart (time, job);
      // XXX Need additional check for finite time... Same holds for DLIMIT/LeakyBucket...
      if (Double.isFinite (this.rateLimit))
      {
        this.isRateLimited = true;
        scheduleRateLimitExpirationEvent (time + 1.0 / this.rateLimit);
      }
    }
    else
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
  
  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponRevokation (final J job, final double time, final boolean auto)
  {
    throw new IllegalStateException ();
  }

  /** Throws {@link IllegalStateException}.
   * 
   * @throws IllegalStateException Always, as a call to this method is unexpected.
   * 
   */
  @Override
  protected final void rescheduleAfterRevokation (final J job, final double time, final boolean auto)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RATE-LIMIT EXPIRATION
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notification of rate-limit expiration from an internally-scheduled {@link RateLimitExpirationEvent}.
   * 
   * <p>
   * Resets the {@code RateLimited} property.
   * 
   * <p>
   * Removes the event for {@link #eventsScheduled}.
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
  protected final static class RateLimitExpirationEvent<Q extends ALIMIT>
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
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  protected final void rescheduleAfterDeparture (final J departedJob, final double time)
  {
    /* EMPTY */
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
