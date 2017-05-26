package nl.jdj.jqueues.r5.entity.jq;

import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue.AutoRevocationPolicy;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;

/** A {@link SimEvent} for a {@link SimEntity} operation on (both) jobs and queues.
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
public abstract class SimJQEvent<J extends SimJob, Q extends SimQueue>
extends SimEntityEvent
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
   * @param queue  The queue related to the event (if applicable), may be {@code null}.
   * @param job    The job related to the event (if applicable), may be {@code null}.
   * @param action The {@link SimEventAction} to take; may be {@code null}.
   * 
   */
  protected SimJQEvent
  (final String name,
   final double time,
   final Q queue,
   final J job,
   final SimEventAction<? extends SimEntity> action)
  {
    super (name, time, queue != null ? queue : job, action);
    this.queue = queue;
    this.job = job;
  }
  
  /** Creates a copy of this event, but for a different queue and/or a different job.
   * 
   * <p>
   * When a parameter is {@code null}, it is left untouched.
   * 
   * <p>
   * The event's action, if non-{@code null}, has to be copied into an action retrofitted to the new queue and/or job.
   * 
   * @param newQueue The new queue (the {@link SimQueue} to which the newly created event applies).
   * @param newJob   The new job (the {@link SimQueue} to which the newly created event applies).
   * 
   * @return A copy of this event but for given queue and/or job.
   * 
   * @throws IllegalArgumentException      If (the combination of) the new queue and/or new job is illegal for this event type.
   * @throws UnsupportedOperationException If the event cannot be copied for the new values of queue and job,
   *                                         for instance, because of lack of knowledge on the {@link SimEventAction} present.
   * 
   */
  public abstract SimJQEvent<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob);
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Q queue;
  
  /** Gets the queue (if applicable) at which the event occurs.
   * 
   * @return The queue (if applicable) to which the event applies, may be {@code null}.
   * 
   */
  public final Q getQueue ()
  {
    return this.queue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final J job;
  
  /** Gets the job (if applicable) to which the event applies.
   * 
   * @return The job (if applicable) to which the event applies, may be {@code null}.
   * 
   */
  public final J getJob ()
  {
    return this.job;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An arrival {@link SimEvent} of a job at a queue.
   * 
   * <p>
   * The event always has a non-{@code null} {@link SimEventAction}, even if used as a notification.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class Arrival<J extends SimJob, Q extends SimQueue>
  extends SimJQEvent<J, Q>
  {
 
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
    private static
    <J extends SimJob, Q extends SimQueue>
    SimEventAction<J>
    createAction (final J job, final Q queue)
    {
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
      return (final SimEvent<J> event) ->
      {
        queue.arrive (event.getTime (), job);
      };
    }
  
    /** Creates a job-arrival event at a specific queue.
     * 
     * <p>
     * The event is provided with an appropriate non-{@code null} new {@link SimEventAction},
     * invoking {@link SimQueue#arrive}.
     * 
     * @param job         The job that arrives.
     * @param queue       The queue at which the job arrives.
     * @param arrivalTime The scheduled arrival time.
     * 
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     * 
     * @see SimQueue#arrive
     * 
     */
    public Arrival
    (final J job, final Q queue, final double arrivalTime)
    {
      super ("Arr[" + job + "]@" + queue, arrivalTime, queue, job, createAction (job, queue));
    }
  
    /** Creates a new job-arrival event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new job-arrival event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     */
    @Override
    public Arrival<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      return new Arrival<> (newJob != null ? newJob : getJob (),
                            newQueue != null ? newQueue : getQueue (),
                            getTime ());
    }
  
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A drop {@link SimEvent} of a job at a queue.
   *
   * <p>
   * Do not <i>ever</i> schedule this yourself unless for your own implementation; it is for private use by {@link SimQueue}
   * implementations.
   *
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   *
   */
  public final static class Drop<J extends SimJob, Q extends SimQueue>
    extends SimJQEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /** Creates a job-drop event at a specific queue with user-supplied {@link SimEventAction}.
     *
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     *
     * @param job      The job that is to be dropped.
     * @param queue    The queue at which the job drops.
     * @param dropTime The scheduled drop time.
     * @param action   The {@link SimEventAction} to take; non-{@code null}.
     *
     * @throws IllegalArgumentException If the job, queue or action is <code>null</code>.
     *
     */
    public Drop (final J job, final Q queue, final double dropTime, final SimEventAction<J> action)
    {
      super ("Drop[" + job + "]@" + queue, dropTime, queue, job, action);
      if (action == null)
        throw new IllegalArgumentException ();
    }

    /** Creates a job-drop event at a specific queue without {@link SimEventAction}.
     *
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     *
     * @param job      The job that is to be dropped.
     * @param queue    The queue at which the job drops.
     * @param dropTime The scheduled drop time.
     *
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     *
     */
    public Drop (final J job, final Q queue, final double dropTime)
    {
      super ("Drop[" + job + "]@" + queue, dropTime, queue, job, null);
    }

    /** Creates a new job-drop event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new job-drop event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @throws UnsupportedOperationException If the {@link SimEventAction} is non-{@code null} and
     *                                       by whatever means a new queue and/or job has been specified.
     * 
     * @see #getEventAction
     * 
     */
    @Override
    public Drop<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (getEventAction () != null
        && ((newQueue != null && newQueue != getQueue ()) || (newJob != null && newJob != getJob ())))
        throw new UnsupportedOperationException ();
      if (getEventAction () != null)
        return new Drop<> (newJob != null ? newJob : getJob (),
          newQueue != null ? newQueue : getQueue (),
          getTime (),
          getEventAction ());
      else
        return new Drop<> (newJob != null ? newJob : getJob (),
          newQueue != null ? newQueue : getQueue (),
          getTime ());        
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A revocation {@link SimEvent} (request) of a job at a queue.
   * 
   * <p>
   * The event always has a non-{@code null} {@link SimEventAction}, even if used as a notification.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class Revocation<J extends SimJob, Q extends SimQueue>
  extends SimJQEvent<J, Q>
  {
 
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
    private static
    <J extends SimJob, Q extends SimQueue>
    SimEventAction<J>
    createAction (final J job, final Q queue, final boolean interruptService)
    {
      if (job == null || queue == null)
        throw new IllegalArgumentException ();
      return (final SimEvent<J> event) ->
      {
        queue.revoke (event.getTime (), job, interruptService);
      };
    }
  
    /** Creates a job-revocation event at a specific queue.
     * 
     * <p>
     * The event is provided with an appropriate non-{@code null} new {@link SimEventAction},
     * invoking {@link SimQueue#revoke}.
     * 
     * @param job              The job that is to be revoked.
     * @param queue            The queue from which the job is to be revoked.
     * @param revocationTime   The scheduled revocation time.
     * @param interruptService Whether to request interruption of service (if applicable).
     * 
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     * 
     * @see SimQueue#revoke
     * 
     */
    public Revocation
    (final J job, final Q queue, final double revocationTime, final boolean interruptService)
    {
      super ("Rev[" + job + "]@" + queue, revocationTime, queue, job, createAction (job, queue, interruptService));
      this.interruptService = interruptService;
    }

    /** Creates a new revocation event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new revocation event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     */
    @Override
    public Revocation<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      return new Revocation<> (newJob != null ? newJob : getJob (),
                               newQueue != null ? newQueue : getQueue (),
                               getTime (),
                               isInterruptService ());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // INTERRUPT SERVICE
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final boolean interruptService;

    /** Returns whether to request interruption of service (if applicable).
     * 
     * @return Whether to request interruption of service (if applicable).
     * 
     */
    public final boolean isInterruptService ()
    {
      return this.interruptService;
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AUTO-REVOCATION [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** An auto-revocation {@link SimEvent} of a job at a queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself unless for your own implementation; it is for private use by {@link SimQueue}
   * implementations.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class AutoRevocation<J extends SimJob, Q extends SimQueue>
  extends SimJQEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates an auto-revocation event of a specific job at at a specific queue with user-supplied {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job              The job that is revoked.
     * @param queue            The queue from which the job is revoked.
     * @param revocationTime   The revocation time.
     * @param action           The {@link SimEventAction} to take; non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job, queue or action is <code>null</code>.
     * 
     * @see AutoRevocationPolicy
     * @see SimQueue#setAutoRevocationPolicy
     * 
     */
    public AutoRevocation
    (final J job, final Q queue, final double revocationTime, final SimEventAction<J> action)
    {
      super ("AutoRev[" + job + "]@" + queue, revocationTime, queue, job, action);
      if (action == null)
        throw new IllegalArgumentException ();
    }
    
    /** Creates an auto-revocation event at a specific queue without {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job              The job that is revoked.
     * @param queue            The queue from which the job is revoked.
     * @param revocationTime   The revocation time.
     * 
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     * 
     * @see AutoRevocationPolicy
     * @see SimQueue#setAutoRevocationPolicy
     * 
     */
    public AutoRevocation
    (final J job, final Q queue, final double revocationTime)
    {
      super ("AutoRev[" + job + "]@" + queue, revocationTime, queue, job, null);
    }

    /** Creates a new auto-revocation event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new auto-revocation event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @throws UnsupportedOperationException If the {@link SimEventAction} is non-{@code null} and
     *                                       by whatever means a new queue and/or job has been specified.
     * 
     * @see #getEventAction
     * 
     */
    @Override
    public AutoRevocation<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (getEventAction () != null
        && ((newQueue != null && newQueue != getQueue ()) || (newJob != null && newJob != getJob ())))
        throw new UnsupportedOperationException ();
      if (getEventAction () != null)
        return new AutoRevocation<> (newJob != null ? newJob : getJob (),
                                     newQueue != null ? newQueue : getQueue (),
                                     getTime (),
                                     getEventAction ());
      else
        return new AutoRevocation<> (newJob != null ? newJob : getJob (),
                                     newQueue != null ? newQueue : getQueue (),
                                     getTime ());
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A start {@link SimEvent} of a job at a queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself unless for your own implementation;
   * it is for private use by {@link SimQueue} implementations.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class Start<J extends SimJob, Q extends SimQueue>
  extends SimJQEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates a job-start event at a specific queue with user-supplied {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job       The job that is to start.
     * @param queue     The queue at which the job starts.
     * @param startTime The scheduled start time.
     * @param action    The {@link SimEventAction} to take; non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job, queue or action is <code>null</code>.
     * 
     */
    public Start
    (final J job, final Q queue, final double startTime, final SimEventAction<J> action)
    {
      super ("Start[" + job + "]@" + queue, startTime, queue, job, action);
      if (action == null)
        throw new IllegalArgumentException ();
    }

    /** Creates a job-start event at a specific queue without {@link SimEventAction}.
     * 
     * <p>
     * Do not <i>ever</i> schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job       The job that is to start.
     * @param queue     The queue at which the job starts.
     * @param startTime The scheduled start time.
     * 
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     * 
     */
    public Start
    (final J job, final Q queue, final double startTime)
    {
      super ("Start[" + job + "]@" + queue, startTime, queue, job, null);
    }

    /** Creates a new start event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new start event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @throws UnsupportedOperationException If the {@link SimEventAction} is non-{@code null} and
     *                                       by whatever means a new queue and/or job has been specified.
     * 
     * @see #getEventAction
     * 
     */
    @Override
    public Start<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (getEventAction () != null
        && ((newQueue != null && newQueue != getQueue ()) || (newJob != null && newJob != getJob ())))
        throw new UnsupportedOperationException ();
      if (getEventAction () != null)
        return new Start<> (newJob != null ? newJob : getJob (),
                            newQueue != null ? newQueue : getQueue (),
                            getTime (),
                            getEventAction ());
      else
        return new Start<> (newJob != null ? newJob : getJob (),
                            newQueue != null ? newQueue : getQueue (),
                            getTime ());
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE [EVENT]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** A departure {@link SimEvent} of a job at a queue.
   * 
   * <p>
   * Do not <i>ever</i> schedule this yourself unless for your own implementation;
   * it is for private use by {@link SimQueue} implementations.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   */
  public final static class Departure<J extends SimJob, Q extends SimQueue>
  extends SimJQEvent<J, Q>
  {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // CONSTRUCTOR(S) / FACTORY / CLONING
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Creates a job-departure event at a specific queue with user-supplied {@link SimEventAction}.
     * 
     * <p>
     * Do not schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job           The job that is to depart.
     * @param queue         The queue at which the job departs.
     * @param departureTime The scheduled departure time.
     * @param action        The {@link SimEventAction} to take; non-{@code null}.
     * 
     * @throws IllegalArgumentException If the job, queue or action is <code>null</code>.
     * 
     */
    public Departure
    (final J job, final Q queue, final double departureTime, final SimEventAction<J> action)
    {
      super ("Dep[" + job + "]@" + queue, departureTime, queue, job, action);
      if (action == null)
        throw new IllegalArgumentException ();
    }

    /** Creates a job-departure event at a specific queue without {@link SimEventAction}.
     * 
     * <p>
     * Do not schedule this yourself; it is for private use by {@link SimQueue} implementations.
     * 
     * @param job           The job that is to depart.
     * @param queue         The queue at which the job departs.
     * @param departureTime The scheduled departure time.
     * 
     * @throws IllegalArgumentException If the job or queue is <code>null</code>.
     * 
     */
    public Departure
    (final J job, final Q queue, final double departureTime)
    {
      super ("Dep[" + job + "]@" + queue, departureTime, queue, job, null);
    }

    /** Creates a new departure event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @return A new departure event at given queue (if non-{@code null}) for given job (if non-{@code null}).
     * 
     * @throws UnsupportedOperationException If the {@link SimEventAction} is non-{@code null} and
     *                                       by whatever means a new queue and/or job has been specified.
     * 
     * @see #getEventAction
     * 
     */
    @Override
    public Departure<J, Q> copyForQueueAndJob (final Q newQueue, final J newJob)
    {
      if (getEventAction () != null
        && ((newQueue != null && newQueue != getQueue ()) || (newJob != null && newJob != getJob ())))
        throw new UnsupportedOperationException ();
      if (getEventAction () != null)
        return new Departure<> (newJob != null ? newJob : getJob (),
                                newQueue != null ? newQueue : getQueue (),
                                getTime (),
                                getEventAction ());
      else
        return new Departure<> (newJob != null ? newJob : getJob (),
                                newQueue != null ? newQueue : getQueue (),
                                getTime ());
    }

  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}