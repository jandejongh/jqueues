package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.extensions.qos.SimQoS;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link IS_CST} queue serves all jobs simultaneously with fixed job-independent service times.
 *
 * <p>
 * Infinite Server with Constant Service Times.
 *
 * <P>
 * Each job has the same queue-determined service time.
 * The job is <i>not</i> consulted for its service time through {@link SimJob#getServiceTime}.
 * 
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
 * this implementation respects the arrival order of jobs.
 *
 * <p>
 * For jobs with identical arrival times, it is <i>not</i> guaranteed that they will depart in order of arrival.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class IS_CST<J extends SimJob, Q extends IS_CST>
extends AbstractNonPreemptiveWorkConservingSimQueue<J, Q>
implements SimQoS<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link IS_CST} queue with given {@link SimEventList} and (fixed) service time.
   * 
   * @param eventList The event list to use.
   * @param serviceTime The service time for all jobs.
   * 
   * @throws IllegalArgumentException If <code>serviceTime</code> is strictly negative.
   * 
   */
  public IS_CST (final SimEventList eventList, final double serviceTime)
  {
    super (eventList, Integer.MAX_VALUE, Integer.MAX_VALUE);
    if (serviceTime < 0)
      throw new IllegalArgumentException ();
    this.serviceTime = serviceTime;
  }
  
  /** Returns a new {@link IS_CST} object on the same {@link SimEventList} with the same service time.
   * 
   * @return A new {@link IS_CST} object on the same {@link SimEventList} with the same service time.
   * 
   * @see #getEventList
   * @see #getServiceTime
   * 
   */
  @Override
  public IS_CST<J, Q> getCopySimQueue ()
  {
    return new IS_CST<> (getEventList (), getServiceTime ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME / toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "IS_CST[service time]".
   * 
   * @return "IS_CST[service time]".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "IS_CST[" + getServiceTime () + "]";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME (FOR JOB)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final double serviceTime;
  
  /** Returns the service time for all jobs.
   * 
   * @return The service time for all jobs, non-negative.
   * 
   */
  public final double getServiceTime ()
  {
    return this.serviceTime;
  }
  
  /** Returns the service time for all jobs.
   * 
   * @return The service time for all jobs as obtained through {@link #getServiceTime()}.
   * 
   */
  @Override
  protected final double getServiceTimeForJob (final J job)
  {
    return getServiceTime ();
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
