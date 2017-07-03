package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link RANDOM} queue serves jobs one at a time in random order.
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
public class RANDOM<J extends SimJob, Q extends RANDOM>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a RANDOM queue given an event list with new private {@link Random} random-number-generator.
   *
   * @param eventList The event list to use.
   *
   * @see Random
   * 
   */
  public RANDOM (final SimEventList eventList)
  {
    this (eventList, null);
  }

  /** Creates a RANDOM queue given an event list and {@link Random} random-number-generator.
   *
   * @param eventList The event list to use.
   * @param RNG The random-number-generator, if <code>null</code>, a new {@link Random} object will be created.
   *
   * @see Random
   * 
   */
  public RANDOM (final SimEventList eventList, final Random RNG)
  {
    super (eventList, Integer.MAX_VALUE, 1);
    this.RNG = ((RNG == null) ? new Random () : RNG);
  }
  
  /** Returns a new {@link RANDOM} object on the same {@link SimEventList}.
   *
   * <p>
   * The new object has its own newly created {@link Random} RNG.
   * 
   * @return A new {@link RANDOM} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public RANDOM<J, Q> getCopySimQueue ()
  {
    return new RANDOM<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "RANDOM".
   * 
   * @return "RANDOM".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "RANDOM";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RNG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Random RNG;

  /** Returns the random-number generator used to sequence arriving jobs for service.
   * 
   * @return The random-number generator, non-<code>null</code>.
   * 
   */
  public final Random getRNG ()
  {
    return this.RNG;
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
  
  /** Calls super method and clears the internal RANDOM queue.
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
    this.randomWaitingQueue.clear ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RANDOM WAITING QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final List<J> randomWaitingQueue = new ArrayList<> ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at a random position the internal RANDOM wait queue.
   * 
   * @see #getRNG
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    final int newPosition = getRNG ().nextInt (this.randomWaitingQueue.size () + 1);
    this.randomWaitingQueue.add (newPosition, job);
  }

  /** Throws an exception.
   * 
   * @param  arrivingJob The arriving job.
   * @param  time        The arrival time.
   * 
   * @return This method does not return.
   * 
   * @throws IllegalStateException As invocation of this method is unexpected (buffer cannot be full).
   * 
   */
  @Override
  protected final J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    throw new IllegalStateException ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the first job in (and removes it from) the internal RANDOM queue.
   * 
   * @return The first job in the internal RANDOM queue.
   * 
   */
  @Override
  protected final J selectJobToStart ()
  {
    return this.randomWaitingQueue.remove (0);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return super.getServiceTimeForJob (job);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // EXIT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Calls super method and removes the job (if present) from the internal RANDOM queue.
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit  (final J exitingJob, final double time)
  {
    super.removeJobFromQueueUponExit (exitingJob, time);
    this.randomWaitingQueue.remove (exitingJob);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
