package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link FCFS} queue with given (possibly infinite) buffer size.
 *
 * <p>
 * First Come First Served with buffer size B and a single server.
 * 
 * <p>
 * Jobs arriving when the buffer is full are dropped.
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
public class FCFS_B<J extends SimJob, Q extends FCFS_B>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server FCFS queue given an event list and given (possibly infinite) buffer size.
   *
   * @param eventList  The event list to use.
   * @param bufferSize The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   * @throws IllegalArgumentException If the buffer size is negative.
   * 
   */
  public FCFS_B (final SimEventList eventList, final int bufferSize)
  {
    super (eventList, bufferSize, 1);
  }
  
  /** Returns a new {@link FCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @return A new {@link FCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @see #getEventList
   * @see #getBufferSize
   * 
   */
  @Override
  public FCFS_B<J, Q> getCopySimQueue ()
  {
    return new FCFS_B<> (getEventList (), getBufferSize ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "FCFS_B[buffer size]".
   * 
   * @return "FCFS_B[buffer size]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "FCFS_B[" + getBufferSize () + "]";
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void insertAdmittedJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * @return The result from the super method.
   * 
   */
  @Override
  protected final J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    return super.selectJobToDropAtFullQueue (arrivingJob, time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // START
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the result from {@link #getFirstJobInWaitingArea}.
   * 
   * @return The result from {@link #getFirstJobInWaitingArea}.
   * 
   */
  @Override
  protected final J selectJobToStart ()
  {
    return getFirstJobInWaitingArea ();
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

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void removeJobFromQueueUponExit  (final J exitingJob, final double time)
  {
    super.removeJobFromQueueUponExit (exitingJob, time);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
