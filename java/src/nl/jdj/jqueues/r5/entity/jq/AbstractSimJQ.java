package nl.jdj.jqueues.r5.entity.jq;

import java.util.logging.Logger;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityListener;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.job.AbstractSimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.AbstractSimQueue;
import nl.jdj.jqueues.r5.entity.SimEntityEvent;
import nl.jdj.jqueues.r5.entity.AbstractSimEntity;
import nl.jdj.jsimulation.r5.SimEventList;

/** An implementation of the common part of a {@link SimJob} and a {@link SimQueue}.
 * 
 * <p>
 * This class implements event notifications
 * for {@link SimQueue} and {@link SimJob} common operations,
 * viz., arrival, drop, revocation, auto-revocation, start, and departure.
 * 
 * <p>
 * A {@link SimQueue} will automatically notify listeners on {@link SimJob}s about these operations.
 * 
 * <p>
 * For a more complete (though still partial) implementations of jobs, see {@link AbstractSimJob}.
 * 
 * <p>
 * For more complete (though still partial) implementations of queues, see {@link AbstractSimQueue}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SimJob
 * @see SimQueue
 * @see AbstractSimJob
 * @see AbstractSimQueue
 * @see SimJQSimpleEventType
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
public abstract class AbstractSimJQ<J extends SimJob, Q extends SimQueue>
extends AbstractSimEntity
implements SimQoS<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // LOGGER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private static final Logger LOGGER = Logger.getLogger (AbstractSimJQ.class.getName ());
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Creates a new {@link AbstractSimJQ} with given event list and name.
   * 
   * <p>
   * The constructor registers the notification types common to queues and jobs.
   * 
   * @param eventList The event list to use, may be {@code null} for {@link SimJob}s.
   * @param name      The name of the entity, may be <code>null</code>.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If this object is neither a {@link SimJob} <i>or</i> a {@link SimQueue},
   *                                  or if it is <i>both</i>.
   * 
   */
  public AbstractSimJQ (final SimEventList eventList, final String name)
  {
    super (eventList, name);
    if (! ((this instanceof SimJob) || (this instanceof SimQueue)))
      throw new IllegalArgumentException ("AbstractSimJQ must be SimJob or a SimQueue");
    if ((this instanceof SimJob) && (this instanceof SimQueue))
      throw new IllegalArgumentException ("AbstractSimJQ cannot be both a SimJob and a SimQueue");
    registerNotificationType (SimJQSimpleEventType.ARRIVAL, this::fireArrival);
    registerNotificationType (SimJQSimpleEventType.DROP, this::fireDrop);
    registerNotificationType (SimJQSimpleEventType.REVOCATION, this::fireRevocation);
    registerNotificationType (SimJQSimpleEventType.AUTO_REVOCATION, this::fireAutoRevocation);
    registerNotificationType (SimJQSimpleEventType.START, this::fireStart);
    registerNotificationType (SimJQSimpleEventType.DEPARTURE, this::fireDeparture);
  }
    
  /** Creates a new {@link AbstractSimJQ} with given event list and <code>null</code> (initial) name.
   * 
   * @param eventList The event list, may be {@code null}.
   * 
   * @see #getEventList
   * @see #setName
   * 
   * @throws IllegalArgumentException If this object is neither a {@link SimJob} <i>or</i> a {@link SimQueue},
   *                                  or if it is <i>both</i>.
   * 
   */
  public AbstractSimJQ (final SimEventList eventList)
  {
    this (eventList, null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET [OPERATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Does nothing.
   * 
   * @see SimEntity#resetEntity
   * 
   */
  @Override
  protected void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a job arrival at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   * 
   * @see SimEntityListener#notifyArrival
   * 
   */
  private void fireArrival (final SimEntityEvent event)
  // private void fireArrival (final SimQueueJobArrivalEvent<J, Q> event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.Arrival))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyArrival (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyArrival (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a job drop at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyDrop
   * 
   */
  private void fireDrop (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.Drop))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyDrop (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyDrop (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // REVOCATION [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a successful job revocation at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyRevocation
   * 
   */
  private void fireRevocation (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.Revocation))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyRevocation (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyRevocation (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AUTO-REVOCATION [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a job auto-revocation at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   *
   * @see SimEntityListener#notifyAutoRevocation
   * 
   */
  private void fireAutoRevocation (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.AutoRevocation))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyAutoRevocation (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyAutoRevocation (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a job starting at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param job The job.
   * 
   * @see SimEntityListener#notifyStart
   * 
   */
  private void fireStart (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.Start))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyStart (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyStart (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEPARTURE [NOTIFICATION]
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Notifies all listeners of a job departure at a queue.
   *
   * <p>
   * A {@link SimQueue} will automatically propagate notifications to the listeners on the {@link SimJob} as well.
   * 
   * @param time  The current time.
   * @param job   The job.
   * @param queue The queue.
   *
   * @see SimEntityListener#notifyDeparture
   * 
   */
  private void fireDeparture (final SimEntityEvent event)
  {
    if (event == null)
      throw new IllegalArgumentException ();
    if (! (event instanceof SimJQEvent.Departure))
      throw new IllegalArgumentException ();
    final double time = getLastUpdateTime ();
    if (event.getTime () != time)
      throw new IllegalArgumentException ();
    final Q queue = (Q) ((SimJQEvent) event).getQueue ();
    final J job = (J) ((SimJQEvent) event).getJob ();
    if (queue == null || job == null)
      throw new IllegalArgumentException ();
    for (SimEntityListener l : getSimEntityListeners ())
      if (l instanceof SimJQListener)
        ((SimJQListener) l).notifyDeparture (time, job, queue);
    if (this instanceof SimQueue)
      for (SimEntityListener l : job.getSimEntityListeners ())
        if (l instanceof SimJQListener)
          ((SimJQListener) l).notifyDeparture (time, job, queue);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
