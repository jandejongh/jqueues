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
package org.javades.jqueues.r5.util.predictor.workload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.SimJQEvent.Arrival;
import org.javades.jqueues.r5.entity.jq.SimJQEvent.Revocation;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.QueueAccessVacation;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueEvent.ServerAccessCredits;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueSimpleEventType;
import org.javades.jqueues.r5.event.map.DefaultSimEntityEventMap;

/** A default implementation of {@link WorkloadSchedule}.
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
public class DefaultWorkloadSchedule
extends DefaultSimEntityEventMap
implements WorkloadSchedule, WorkloadScheduleHandler
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimJQEvent}s.
   * 
   * @param <E>         The event type.
   * @param queues      The queues to consider; events related to other queues are ignored; if {@code null},
   *                    consider all queues found in the {@code queueEvents} set.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule
  (final Set<? extends SimQueue> queues,
   final Set<E> queueEvents)
  throws WorkloadScheduleException
  {
    super (queueEvents);
    if (queueEvents != null)
      this.queueEvents.addAll (queueEvents);
    this.queueEvents.remove (null);
    if (queues != null)
      this.queues.addAll (queues);
    else
      this.queues.addAll (getSimQueueTimeSimEntityEventMap ().keySet ());
    registerHandler (this);
  }
  
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a set of
   *  {@link SimJQEvent}s.
   * 
   * <p>
   * With this constructor, all queues found in the {@code queueEvent}s are considered.
   * 
   * <p>
   * Implemented as {@code this (null, queueEvents)}.
   * 
   * @param <E>         The event type.
   * @param queueEvents The set of events to parse (parsing is actually done in this constructor).
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule
  (final Set<E> queueEvents)
  throws WorkloadScheduleException
  {
    this (null, queueEvents);
  }

  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a map of
   *  {@link SimJQEvent}s.
   * 
   * @param <E>         The event type.
   * @param queues      The queues to consider; events related to other queues are ignored; if {@code null},
   *                    consider all queues found in the {@code queueEvents} set.
   * @param queueEvents The set of events (as a map) to parse (parsing is actually done in this constructor).
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule
  (final Set<? extends SimQueue> queues,
   final Map<Double, Set<E>> queueEvents)
  throws WorkloadScheduleException
  {
    super (queueEvents);
    if (queueEvents != null)
      for (final Set<E> queueEvents_q : queueEvents.values ())
        this.queueEvents.addAll (queueEvents_q);
    this.queueEvents.remove (null);
    if (queues != null)
      this.queues.addAll (queues);
    else
      this.queues.addAll (getSimQueueTimeSimEntityEventMap ().keySet ());
    registerHandler (this);    
  }
    
  /** Creates a new {@link DefaultWorkloadSchedule}, filling out all the internal sets and maps from scanning a map of
   *  {@link SimJQEvent}s.
   * 
   * <p>
   * With this constructor, all queues found in the {@code queueEvent}s are considered.
   * 
   * <p>
   * Implemented as {@code this (null, queueEvents)}.
   * 
   * @param <E>         The event type.
   * @param queueEvents The set of events (as a map) to parse (parsing is actually done in this constructor).
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public <E extends SimJQEvent>
  DefaultWorkloadSchedule
  (final Map<Double, Set<E>> queueEvents)
  throws WorkloadScheduleException
  {
    this (null, queueEvents);
  }
      
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WorkloadSchedule
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public double getNextEventTimeBeyond
  (final SimQueue queue,
   final double time,
   final Set<SimEntitySimpleEventType.Member> eventTypes)
   throws WorkloadScheduleException
  {
    if (eventTypes != null)
      eventTypes.clear ();
    if (queue == null || ! getQueues ().contains (queue))
      return Double.NaN;
    double nextEventTime = Double.NaN;
    final NavigableMap<Double,Set<SimJQEvent>> timeSimEntityMap = getSimQueueTimeSimEntityEventMap ().get (queue);
    if (timeSimEntityMap != null)
    {
      final Entry<Double, Set<SimJQEvent>> nextEntry =
        (Double.isNaN (time) ? timeSimEntityMap.firstEntry () : timeSimEntityMap.higherEntry (time));
      if (nextEntry != null)
      {
        nextEventTime = nextEntry.getKey ();
        if (eventTypes != null)
        {
          for (final SimJQEvent entityEvent : nextEntry.getValue ())
            if (entityEvent == null)
              throw new RuntimeException ();
            else if (! this.handlerEventMap.containsKey (entityEvent.getClass ()))
            {
              // XXX
              System.err.println ("Handler for SimEventEntity class " + entityEvent.getClass () + " NOT found!.");
              throw new WorkloadScheduleInvalidException ();
            }
            else if (this.handlerEventMap.get (entityEvent.getClass ()) == null)
              throw new RuntimeException ();
            else if (! this.handlerEventMap.get (entityEvent.getClass ()).getEventMap ().containsKey (entityEvent.getClass ()))
              throw new RuntimeException ();
            else if (this.handlerEventMap.get (entityEvent.getClass ()).getEventMap ().get (entityEvent.getClass ()) == null)
              throw new RuntimeException ();
            else
              eventTypes.add (this.handlerEventMap.get (entityEvent.getClass ()).getEventMap ().get (entityEvent.getClass ()));
        }
      }
    }
    return nextEventTime;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // HANDLERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<Class<? extends SimJQEvent>, WorkloadScheduleHandler> handlerEventMap = new HashMap<> ();
  
  private final Map<String, WorkloadScheduleHandler> handlerNameMap = new HashMap<> ();
  
  /** Registers a handler for {@link SimJQEvent}s, and, upon request of the handler, passes control to it for scanning.
   * 
   * @param handler The handler.
   * 
   * @throws IllegalArgumentException If the handler or its name are {@code null},
   *                                  if a handler with the same name has been registered already,
   *                                  if the handler's event map has a {@code null} key,
   *                                  or if it tries to register {@link SimJQEvent}s that are already registered
   *                                  by other handlers.
   * 
   * @see WorkloadScheduleHandler
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous (for instance).
   * 
   */
  public final void registerHandler (final WorkloadScheduleHandler handler)
  throws WorkloadScheduleException
  {
    if (handler == null
      || handler.getHandlerName () == null
      || this.handlerNameMap.containsKey (handler.getHandlerName ()))
      throw new IllegalArgumentException ();
    final Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> eventMap = handler.getEventMap ();
    if (eventMap != null)
    {
      if (eventMap.containsKey (null))
        throw new IllegalArgumentException ();
      for (Class<? extends SimJQEvent> eventClass : eventMap.keySet ())
      {
        if (this.handlerEventMap.containsKey (eventClass))
          throw new IllegalArgumentException ();
        this.handlerEventMap.put (eventClass, handler);
      }
    }
    this.handlerNameMap.put (handler.getHandlerName (), handler);
    if (handler.needsScan ())
    {
      final Set<SimJQEvent> handlerProcessedQueueEvents = handler.scan (this);
      if (handlerProcessedQueueEvents != null)
        for (final SimJQEvent event : handlerProcessedQueueEvents)
          if (event == null || this.processedQueueEvents.contains (event))
            throw new WorkloadScheduleInvalidException ();
      if (handlerProcessedQueueEvents != null)
        this.processedQueueEvents.addAll (handlerProcessedQueueEvents);
    }
  }
  
  /** Gets a handler by name.
   * 
   * @param name The name to look for.
   * 
   * @return The handler, or {@code null} if not found.
   * 
   */
  public final WorkloadScheduleHandler getHandler (final String name)
  {
    return this.handlerNameMap.get (name);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimJQEvent> queueEvents = new LinkedHashSet<> ();
  
  @Override
  public final Set<SimJQEvent> getQueueEvents ()
  {
    return Collections.unmodifiableSet (this.queueEvents);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PROCESSED QUEUE EVENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimJQEvent> processedQueueEvents = new HashSet<> ();
  
  @Override
  public final Set<SimJQEvent> getProcessedQueueEvents ()
  {
    return Collections.unmodifiableSet (this.processedQueueEvents);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimQueue> queues = new HashSet<> ();
  
  @Override
  public final Set<SimQueue> getQueues ()
  {
    return Collections.unmodifiableSet (this.queues);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimQueue, NavigableMap<Double, List<Boolean>>> qavTimesMap = new HashMap<> ();
  
  @Override
  public final NavigableMap<Double, List<Boolean>> getQueueAccessVacationMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.qavTimesMap.get (queue));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // JOBS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<SimJob> jobs = new HashSet<> ();
  
  @Override
  public final Set<SimJob> getJobs ()
  {
    return Collections.unmodifiableSet (this.jobs);
  }

  @Override
  public final Set<SimJob> getJobs (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_SET;
    final Set<SimJob> jobs_q = new HashSet<> (this.arrTimesMap.get (queue).keySet ());
    jobs_q.addAll (this.revTimesMap.get (queue).keySet ());
    return Collections.unmodifiableSet (jobs_q);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVALS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimQueue, Map<SimJob, List<Double>>> arrTimesMap = new HashMap<> ();
  
  private final Map<SimQueue, NavigableMap<Double, List<SimJob>>> timeArrsMap = new HashMap<> ();
 
  @Override
  public final Map<SimJob, List<Double>> getArrivalTimesMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_MAP;
    else
      return Collections.unmodifiableMap (this.arrTimesMap.get (queue));
  }
  
  @Override
  public final NavigableMap<Double, List<SimJob>> getJobArrivalsMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.timeArrsMap.get (queue));    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Map<SimQueue, Map<SimJob, List<Map<Double, Boolean>>>> revTimesMap = new HashMap<> ();
  
  private final Map<SimQueue, NavigableMap<Double, List<Map<SimJob, Boolean>>>> timeRevsMap = new HashMap<> ();
  
  @Override
  public final Map<SimJob, List<Map<Double, Boolean>>> getRevocationTimesMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.EMPTY_MAP;
    else
      return Collections.unmodifiableMap (this.revTimesMap.get (queue));
  }
  
  @Override
  public final NavigableMap<Double, List<Map<SimJob, Boolean>>> getJobRevocationsMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.timeRevsMap.get (queue));    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER-ACCESS CREDITS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final Map<SimQueue, NavigableMap<Double, List<Integer>>> sacTimesMap = new HashMap<> ();

  @Override
  public final NavigableMap<Double, List<Integer>> getServerAccessCreditsMap (final SimQueue queue)
  {
    if (queue == null || ! this.queues.contains (queue))
      return Collections.unmodifiableNavigableMap (new TreeMap<> ());
    else
      return Collections.unmodifiableNavigableMap (this.sacTimesMap.get (queue));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WorkloadScheduleHandler
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * 
   * @return "SimQueueHandler".
   * 
   */
  @Override
  public final String getHandlerName ()
  {
    return "SimQueueHandler";
  }

  private final static Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> EVENT_MAP = new HashMap<> ();
  
  static
  {
    DefaultWorkloadSchedule.EVENT_MAP.put (SimQueueEvent.QueueAccessVacation.class, SimQueueSimpleEventType.QUEUE_ACCESS_VACATION);
    DefaultWorkloadSchedule.EVENT_MAP.put (SimJQEvent.Arrival.class,                SimQueueSimpleEventType.ARRIVAL);
    DefaultWorkloadSchedule.EVENT_MAP.put (SimJQEvent.Revocation.class,             SimQueueSimpleEventType.REVOCATION);
    DefaultWorkloadSchedule.EVENT_MAP.put (SimQueueEvent.ServerAccessCredits.class, SimQueueSimpleEventType.SERVER_ACCESS_CREDITS);
  }
  
  /** Returns a appropriate event map for this handler
   *  for queue-access vacations, arrivals, revocations and server-access credits.
   * 
   * @return An event map for queue-access vacations, arrivals, revocations and server-access credits.
   * 
   * @see QueueAccessVacation
   * @see Arrival
   * @see Revocation
   * @see ServerAccessCredits
   * @see SimQueueSimpleEventType#QUEUE_ACCESS_VACATION
   * @see SimQueueSimpleEventType#ARRIVAL
   * @see SimQueueSimpleEventType#REVOCATION
   * @see SimQueueSimpleEventType#SERVER_ACCESS_CREDITS
   * 
   */
  @Override
  public final Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> getEventMap ()
  {
    return DefaultWorkloadSchedule.EVENT_MAP;
  }

  /** Returns {@code true}.
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean needsScan ()
  {
    return true;
  }

  @Override
  public final Set<SimJQEvent> scan (final DefaultWorkloadSchedule workloadSchedule)
  {
    if (workloadSchedule != this)
      throw new IllegalArgumentException ();
    final Set<SimJQEvent> handlerProcessedQueueEvents = new HashSet<> ();
    for (SimQueue q : this.queues)
    {
      this.qavTimesMap.put (q, new TreeMap<> ());
      this.arrTimesMap.put (q, new HashMap<> ());
      this.timeArrsMap.put (q, new TreeMap<> ());
      this.revTimesMap.put (q, new HashMap<> ());
      this.timeRevsMap.put (q, new TreeMap<> ());
      this.sacTimesMap.put (q, new TreeMap<> ());
    }
    if (this.queueEvents != null)
    {
      for (final SimJQEvent event : this.queueEvents)
      {
        final double time = event.getTime ();
        final SimQueue queue = event.getQueue ();
        final SimJob job = event.getJob ();
        if (this.queues.contains (queue))
        {
          if (event instanceof SimQueueEvent.QueueAccessVacation)
          {
            handlerProcessedQueueEvents.add (event);
            final boolean vacation = ((SimQueueEvent.QueueAccessVacation) event).getVacation ();
            final NavigableMap<Double, List<Boolean>> qavTimesMap_q = this.qavTimesMap.get (queue);
            if (! qavTimesMap_q.containsKey (time))
              qavTimesMap_q.put (time, new ArrayList<> ());
            qavTimesMap_q.get (time).add (vacation);
          }
          else if (event instanceof SimJQEvent.Arrival)
          {
            handlerProcessedQueueEvents.add (event);
            this.jobs.add (job);
            final Map<SimJob, List<Double>> arrTimesMap_q = this.arrTimesMap.get (queue);
            if (! arrTimesMap_q.containsKey (job))
              arrTimesMap_q.put (job, new ArrayList<> ());
            arrTimesMap_q.get (job).add (time);
            final NavigableMap<Double, List<SimJob>> timeArrsMap_q = this.timeArrsMap.get (queue);
            if (! timeArrsMap_q.containsKey (time))
              timeArrsMap_q.put (time, new ArrayList<> ());
            timeArrsMap_q.get (time).add (job);
          }
          else if (event instanceof SimJQEvent.Revocation)
          {
            handlerProcessedQueueEvents.add (event);
            this.jobs.add (job);
            final boolean interruptService = ((SimJQEvent.Revocation) event).isInterruptService ();
            final  Map<SimJob, List<Map<Double, Boolean>>> revTimesMap_q = this.revTimesMap.get (queue);
            if (! revTimesMap_q.containsKey (job))
              revTimesMap_q.put (job, new ArrayList<> ());
            final Map<Double, Boolean> timeIsMap = new HashMap<> ();
            timeIsMap.put (time, interruptService);
            revTimesMap_q.get (job).add (timeIsMap);
            final NavigableMap<Double, List<Map<SimJob, Boolean>>> timeRevsMap_q = this.timeRevsMap.get (queue);
            if (! timeRevsMap_q.containsKey (time))
              timeRevsMap_q.put (time, new ArrayList<> ());
            final Map<SimJob, Boolean> jobIsMap = new HashMap<> ();
            jobIsMap.put (job, interruptService);
            timeRevsMap_q.get (time).add (jobIsMap);
          }
          else if (event instanceof SimQueueEvent.ServerAccessCredits)
          {
            handlerProcessedQueueEvents.add (event);
            final int credits = ((SimQueueEvent.ServerAccessCredits) event).getCredits ();
            final NavigableMap<Double, List<Integer>> sacTimesMap_q = this.sacTimesMap.get (queue);
            if (! sacTimesMap_q.containsKey (time))
              sacTimesMap_q.put (time, new ArrayList<> ());
            sacTimesMap_q.get (time).add (credits);
          }
        }
      }
    }
    return handlerProcessedQueueEvents;
  }
  
}
