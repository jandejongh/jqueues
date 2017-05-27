package nl.jdj.jqueues.r5.event;

import nl.jdj.jqueues.r5.entity.jq.SimJQEvent;
import java.util.Set;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Arrival;
import nl.jdj.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent.QueueAccessVacation;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueEvent.ServerAccessCredits;
import nl.jdj.jsimulation.r5.SimEventList;

/** A utility class capable of scheduling {@link SimJQEvent}s on an event list.
 *
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public abstract class SimEntityEventScheduler
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inhibits instantiation.
   * 
   */
  private SimEntityEventScheduler ()
  {
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UTILITY METHODS FOR SCHEDULING
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Schedules all {@link SimJQEvent}s on the given {@link SimEventList}, optionally after resetting it to a specific time.
   * 
   * @param eventList   The event list.
   * @param reset       Whether to reset the event list before scheduling.
   * @param resetTime   The new time to which to reset the event list (if requested so). 
   * @param queueEvents The {@link SimJQEvent}s to schedule.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @throws IllegalArgumentException If <code>eventList == null</code> or the (non-<code>null</code>) set of events
   *                                  has at least one <code>null</code> entry,
   *                                  or if any of the events is to be scheduled in the past after the optional event-list reset
   *                                  (compared to the time on the event list),
   * 
   * @see #schedule(SimEventList, SimJQEvent) 
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  schedule
  (final SimEventList eventList, final boolean reset, final double resetTime, final Set<SimJQEvent<J, Q>> queueEvents)
  {
    if (eventList == null)
      throw new IllegalArgumentException ();
    if (queueEvents != null && queueEvents.contains (null))
      throw new IllegalArgumentException ();
    if (reset)
      eventList.reset (resetTime);
    if (queueEvents != null)
      for (final SimJQEvent<J, Q> event : queueEvents)
        SimEntityEventScheduler.schedule (eventList, event);
  }

  /** Schedules a single queue event on a given event list.
   * 
   * @param eventList  The event list, non-{@code null}.
   * @param queueEvent The queue event, non-{@code null}.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @throws IllegalArgumentException If the event list or event is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  schedule
  (final SimEventList eventList, final SimJQEvent<J, Q> queueEvent)
  {
    if (eventList == null || queueEvent == null)
      throw new IllegalArgumentException ();
    final double eventListTime = eventList.getTime ();
    final double eventTime = queueEvent.getTime ();
    if (eventTime < eventListTime)
      throw new IllegalArgumentException ();
    final J job = queueEvent.getJob ();
    if (job != null && job.getEventList () != null && job.getEventList () != eventList)
      throw new IllegalArgumentException ();
    final Q queue = queueEvent.getQueue ();
    if (queue != null && queue.getEventList () != null && queue.getEventList () != eventList)
      throw new IllegalArgumentException ();
    eventList.add (queueEvent);
  }
  
  /** Creates a (default) queue-access vacation event and schedules it.
   * 
   * @param queue    The queue at which to start or end a queue-access vacation.
   * @param time     The time at which to start or end a queue-access vacation.
   * @param vacation Whether a queue-access vacation starts (<code>true</code>) or ends (<code>false</code>).
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see QueueAccessVacation
   * @see SimQueue#setQueueAccessVacation
   * @see #schedule(SimEventList, SimJQEvent) 
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  scheduleQueueAccessVacation
  (final Q queue, final double time, final boolean vacation)
  {
    if (queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.schedule (queue.getEventList (),
      new SimQueueEvent.QueueAccessVacation<> (queue, time, vacation));
  }
    
  /** Creates a (default) job-arrival event and schedules it.
   * 
   * @param job         The job that arrives.
   * @param queue       The queue at which the job arrives.
   * @param arrivalTime The scheduled arrival time.
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see Arrival
   * @see SimQueue#arrive
   * @see #schedule(SimEventList, SimJQEvent) 
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  scheduleJobArrival
  (final J job, final Q queue, final double arrivalTime)
  {
    if (job == null || queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.schedule (queue.getEventList (), new SimJQEvent.Arrival<> (job, queue, arrivalTime));
  }
    
  /** Creates a (default) job-revocation event and schedules it.
   * 
   * @param job             The job that is to be revoked.
   * @param queue           The queue at which the job is to be revoked from.
   * @param revocationTime  The scheduled revocation time.
   * @param interruptService Whether to request interruption of service (if applicable).
   * 
   * @throws IllegalArgumentException If the job or queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or if the job is attached to a different event list than the queue.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see Revocation
   * @see SimQueue#revoke
   * @see #schedule(SimEventList, SimJQEvent) 
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  scheduleJobRevocation
  (final J job, final Q queue, final double revocationTime, final boolean interruptService)
  {
    if (job == null || queue == null || queue.getEventList () == null)
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.schedule (queue.getEventList (),
      new SimJQEvent.Revocation<> (job, queue, revocationTime, interruptService));
  }
    
  /** Creates a (default) a server-access-credits event and schedules it.
   * 
   * @param queue   The queue at which to set server-access credits.
   * @param time    The time at which to set server-access credits.
   * @param credits The number of credits to grant.
   * 
   * @throws IllegalArgumentException If the queue is <code>null</code>,
   *                                  if the scheduled time is in the past
   *                                  (compared to the time on the event list),
   *                                  if no event list could be retrieved form the queue,
   *                                  or the number of credits is strictly negative.
   * 
   * @param <J> The type of {@link SimJob}s supported.
   * @param <Q> The type of {@link SimQueue}s supported.
   * 
   * @see ServerAccessCredits
   * @see SimQueue#setServerAccessCredits
   * @see #schedule(SimEventList, SimJQEvent) 
   * 
   */
  public static
  <J extends SimJob, Q extends SimQueue>
  void
  scheduleServerAccessCredits
  (final Q queue, final double time, final int credits)
  {
    if (queue == null || queue.getEventList () == null || credits < 0)
      throw new IllegalArgumentException ();
    SimEntityEventScheduler.schedule (queue.getEventList (),
      new SimQueueEvent.ServerAccessCredits<> (queue, time, credits));
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
