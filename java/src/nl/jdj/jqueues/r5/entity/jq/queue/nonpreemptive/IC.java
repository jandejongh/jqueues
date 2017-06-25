package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue; /* Forced for javadoc. */
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link IC} queue serves all jobs in zero time.
 *
 * Infinite Capacity.
 *
 * <p>
 * This queueing discipline guarantees that it will never invoke {@link SimJob#getServiceTime}.
 *
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to service,
 * this implementation respects the arrival order of jobs.
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
public class IC<J extends SimJob, Q extends IC>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link IC} queue with given {@link SimEventList}.
   * 
   * @param eventList The event list to use.
   * 
   */
  public IC (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE, Integer.MAX_VALUE);
  }

  /** Returns a new {@link IC} object on the same {@link SimEventList}.
   * 
   * @return A new {@link IC} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public IC<J, Q> getCopySimQueue ()
  {
    return new IC<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "IC".
   * 
   * @return "IC".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "IC";
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
  // SERVICE TIME FOR JOB
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns zero.
   * 
   * @return zero.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return 0;
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
