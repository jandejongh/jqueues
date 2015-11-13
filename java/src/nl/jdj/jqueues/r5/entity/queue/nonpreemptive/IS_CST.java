package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link IS_CST} queue serves all jobs simultaneously with fixed job-independent service times.
 *
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
extends AbstractNonPreemptiveInfiniteServerSimQueue<J, Q>
{

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
  protected final double getServiceTime (final J job)
  {
    return getServiceTime ();
  }
  
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
    super (eventList);
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
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Returns "IS_CST[service time]".
   * 
   * @return "IS_CST[service time]".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "IS_CST[" + getServiceTime () + "]";
  }

}
