package nl.jdj.jqueues.r5.entity.jq.queue.nonpreemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link SJF} queue serves jobs one at a time in order of ascending requested service times.
 *
 * <p>
 * Shortest-Job First.
 *
 * <p>
 * In case of ties, jobs are scheduled for service in order of arrival.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SimJob#getServiceTime
 * @see LJF
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
public class SJF<J extends SimJob, Q extends SJF>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a SJF queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public SJF (final SimEventList eventList)
  {
    super (eventList, Integer.MAX_VALUE, 1);
  }
  
  /** Returns a new {@link SJF} object on the same {@link SimEventList}.
   * 
   * @return A new {@link SJF} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public SJF<J, Q> getCopySimQueue ()
  {
    return new SJF<> (getEventList ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "SJF".
   * 
   * @return "SJF".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "SJF";
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
  
  /** Inserts the job in the job queue maintaining non-decreasing service-time ordering.
   * 
   * <p>
   * In case of ties, jobs are scheduled for service in order of arrival from the underlying event list.
   * 
   * @see #jobQueue
   * @see #getServiceTimeForJob
   * @see #insertJobInQueueUponArrival
   * 
   */
  @Override
  protected final void insertAdmittedJobInQueueUponArrival (final J job, final double time)
  {
    int newPosition = 0;
    while (newPosition < this.jobQueue.size ()
      && getServiceTimeForJob (this.jobQueue.get (newPosition)) <= getServiceTimeForJob (job))
      newPosition++;
    this.jobQueue.add (newPosition, job);    
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
