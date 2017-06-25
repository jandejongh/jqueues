package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link LCFS} queue with given (possibly infinite) buffer size.
 *
 * <p>
 * Last Come First Served with buffer size B and a single server.
 * 
 * <p>
 * When a job arrives when the buffer is full and non-empty,
 * the job in the waiting queue that arrived least recently is dropped.
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
public class LCFS_B<J extends SimJob, Q extends LCFS_B>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a single-server LCFS queue given an event list and given (possibly infinite) buffer size.
   *
   * @param eventList  The event list to use.
   * @param bufferSize The buffer size (non-negative), {@link Integer#MAX_VALUE} is interpreted as infinity.
   *
   * @throws IllegalArgumentException If the buffer size is negative.
   * 
   */
  public LCFS_B (final SimEventList eventList, final int bufferSize)
  {
    super (eventList, bufferSize, 1);
  }
  
  /** Returns a new {@link LCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @return A new {@link LCFS_B} object on the same {@link SimEventList} with the same buffer size.
   * 
   * @see #getEventList
   * @see #getBufferSize
   * 
   */
  @Override
  public LCFS_B<J, Q> getCopySimQueue ()
  {
    return new LCFS_B<> (getEventList (), getBufferSize ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "LCFS_B[buffer size]".
   * 
   * @return "LCFS_B[buffer size]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "LCFS_B[" + getBufferSize () + "]";
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
  
  /** Inserts the job at the head of the job queue.
   * 
   * @see #jobQueue
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void insertAdmittedJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (0, job);
  }

  /** Returns the job at the tail of the wait queue.
   * 
   * @param arrivingJob The job that arrived at the queue.
   * @param time        THe job's arrival time.
   * 
   * @return The job at the tail of the wait queue.
   * 
   * @throws IllegalStateException If the waiting area is empty,
   *                               or the arriving job is {@code null}
   *                               or already present in {@link #jobQueue}.
   * 
   * @see #getLastJobInWaitingArea
   * 
   */
  @Override
  protected final J selectJobToDropAtFullQueue (final J arrivingJob, final double time)
  {
    if (getNumberOfJobsInWaitingArea () == 0 || this.jobQueue.contains (arrivingJob))
      throw new IllegalStateException ();
    return getLastJobInWaitingArea ();
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
