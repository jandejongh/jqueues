package nl.jdj.jqueues.r5.entity.jq.queue;

import nl.jdj.jqueues.r5.entity.jq.job.*;
import nl.jdj.jqueues.r5.entity.jq.*;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A {@link SimEvent} for a {@link SimQueue} operation on {@link SimQueue}s.
 * 
 * <p>
 * This class only administers the key parameters for the event; it does not actually schedule it.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public abstract class SimQueueEvent<J extends SimJob, Q extends SimQueue>
extends SimJQEvent<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY / CLONING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new event for a queue.
   * 
   * @param name   The (optional) name of the event, may be  {@code null}.
   * @param time   The time at which the event occurs.
   * @param queue  The queue related to the event, non-{@code null}.
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   * @throws IllegalArgumentException If the queue is {@code null}.
   * 
   */
  protected SimQueueEvent
  (final String name,
   final double time,
   final Q queue,
   final SimEventAction<? extends SimEntity> action)
  {
    super (name, time, queue, null, action);
    if (queue == null)
      throw new IllegalArgumentException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATION [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A {@link SimEvent} for the start or end of a queue-access vacation at a queue.
   * 
   * <p>
   * The event always has a non-{@code null} {@link SimEventAction}, even if used as a notification.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public static final class QueueAccessVacation<J extends SimJob, Q extends SimQueue>
  extends SimQueueEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static
    <J extends SimJob, Q extends SimQueue>
    SimEventAction<J>
    createAction (final Q queue, final boolean vacation)
    {
      if (queue == null)
        throw new IllegalArgumentException ();
      return (final SimEvent<J> event) -> queue.setQueueAccessVacation (event.getTime (), vacation);
    }

    /** Creates a queue-access vacation event at a specific queue.
     * 
     * <p>
     * The event is provided with an appropriate non-{@code null} new {@link SimEventAction},
     * invoking {@link SimQueue#setQueueAccessVacation}.
     * 
     * @param queue    The queue at which to start or end a queue-access vacation.
     * @param time     The time at which to start or end a queue-access vacation.
     * @param vacation Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
     * 
     * @throws IllegalArgumentException If the queue is <code>null</code>.
     * 
     * @see SimQueue#setQueueAccessVacation
     * 
     */
    public QueueAccessVacation
    (final Q queue, final double time, final boolean vacation)
    {
      super ("QAV[" + vacation + "]@" + queue, time, queue, createAction (queue, vacation));
      this.vacation = vacation;
    }

    /** Creates a new queue-access vacation event at given queue (if non-{@code null}).
     * 
     * @return A new queue-access vacation event at given queue (if non-{@code null}).
     * 
     * @throws IllegalArgumentException If the job is non-{@code null}.
     * 
     * @see #getVacation
     * 
     */
    @Override
    public QueueAccessVacation<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (newJob != null)
        throw new IllegalArgumentException ();
      return new QueueAccessVacation<> (newQueue != null ? newQueue : getQueue (), getTime (), getVacation ());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // VACATION
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final boolean vacation;

    /** Returns whether the vacation starts or ends.
     * 
     * @return Whether the queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
     * 
     */
    public final boolean getVacation ()
    {
      return this.vacation;
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START-ARMED [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** A {@link SimEvent} for the start or end of the {@code startArmed} condition at a queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself unless for your own implementation; it is for private use by {@link SimQueue}
   * implementations.
   *
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class StartArmed<J extends SimJob, Q extends SimQueue>
  extends SimQueueEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates a {@link SimEvent} for the start or end of the {@code startArmed} condition at a queue
     *  with user-supplied {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     *
     * @param queue      The queue to which the event applies.
     * @param time       The time of the event.
     * @param startArmed The value of the {@code startArmed} condition at the queue.
     * @param action     The {@link SimEventAction} to take; non-{@code null}.
     * 
     * @throws IllegalArgumentException If the queue or action is <code>null</code>.
     * 
     * @see SimQueue#isStartArmed
     * 
     */
    public StartArmed
    (final Q queue, final double time, final boolean startArmed, final SimEventAction<Q> action)
    {
      super ("StartArmed[" + startArmed + "]@" + queue, time, queue, action);
      if (action == null)
        throw new IllegalArgumentException ();
      this.startArmed = startArmed;
    }

    /** Creates a {@link SimEvent} for the start or end of the {@code startArmed} condition at a queue
     *  without {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     *
     * @param queue      The queue to which the event applies.
     * @param time       The time of the event.
     * @param startArmed The value of the {@code startArmed} condition at the queue.
     * 
     * @throws IllegalArgumentException If the queue is <code>null</code>.
     * 
     * @see SimQueue#isStartArmed
     * 
     */
    public StartArmed
    (final Q queue, final double time, final boolean startArmed)
    {
      super ("StartArmed[" + startArmed + "]@" + queue, time, queue, null);
      this.startArmed = startArmed;
    }

    /** Creates a new start-armed event at given queue (if non-{@code null}).
     * 
     * @return A new start-armed event at given queue (if non-{@code null}).
     * 
     * @throws IllegalArgumentException      If the job is non-{@code null}.
     * @throws UnsupportedOperationException If the {@link SimEventAction} is non-{@code null} and
     *                                       by whatever means a new queue has been specified.
     * 
     * @see #isStartArmed
     * @see #getEventAction
     * 
     */
    @Override
    public StartArmed<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (newJob != null)
        throw new IllegalArgumentException ();
      if (getEventAction () != null && newQueue != null && newQueue != getQueue ())
        throw new UnsupportedOperationException ();
      if (getEventAction () != null)
        return new StartArmed (newQueue != null ? newQueue : getQueue (), getTime (), isStartArmed (), getEventAction ());
      else
        return new StartArmed (newQueue != null ? newQueue : getQueue (), getTime (), isStartArmed ());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // START-ARMED
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final boolean startArmed;

    /** Returns whether the queue is {@code startArmed} or not.
     * 
     * @return Whether the queue is {@code startArmed} (<code>true</code>) or not (<code>false</code>).
     * 
     */
    public final boolean isStartArmed ()
    {
      return this.startArmed;
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** A {@link SimEvent} for setting server-access credits at a queue.
   * 
   * <p>
   * The event always has a non-{@code null} {@link SimEventAction}, even if used as a notification.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class ServerAccessCredits<J extends SimJob, Q extends SimQueue>
  extends SimQueueEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static
    <J extends SimJob, Q extends SimQueue>
    SimEventAction<Q>
    createAction (final Q queue, final int credits)
    {
      if (queue == null || credits < 0)
        throw new IllegalArgumentException ();
      return (final SimEvent<Q> event) -> queue.setServerAccessCredits (event.getTime (), credits);
    }

    /** Creates a server-access-credits event at a specific queue.
     * 
     * <p>
     * The event is provided with an appropriate non-{@code null} new {@link SimEventAction},
     * invoking {@link SimQueue#setServerAccessCredits}.
     * 
     * @param queue   The queue at which to set server-access credits.
     * @param time    The time at which to set server-access credits.
     * @param credits The number of credits to grant.
     * 
     * @throws IllegalArgumentException If <code>queue == null</code> or the number of credits is strictly negative.
     * 
     * @see SimQueue#setServerAccessCredits
     * 
     */
    public ServerAccessCredits
    (final Q queue, final double time, final int credits)
    {
      super ("SAC[" + credits + "]@" + queue, time, queue, createAction (queue, credits));
      this.credits = credits;
    }

    /** Creates a new server-access-credits event at given queue (if non-{@code null}).
     * 
     * @return A new server-access-credits event at given queue (if non-{@code null}).
     * 
     * @throws IllegalArgumentException If the job is non-{@code null}.
     * 
     * @see #getCredits
     * 
     */
    @Override
    public ServerAccessCredits<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (newJob != null)
        throw new IllegalArgumentException ();
      return new ServerAccessCredits<> (newQueue != null ? newQueue : getQueue (), getTime (), getCredits ());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CREDITS
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final int credits;

    /** Returns the number of credits to grant.
     * 
     * @return The number of credits to grant.
     * 
     */
    public final int getCredits ()
    {
      return this.credits;
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}