package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue; /* Forced for javadoc. */
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
 * <p>
 * For jobs with identical arrival times, it is <i>not</i> guaranteed that they will depart in order of arrival.
 * In that case, a job may even depart before the arrival of another job (should the underlying {@link SimEventList} not
 * respect insertion order).
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class IC<J extends SimJob, Q extends IC>
extends AbstractNonPreemptiveInfiniteServerSimQueue<J, Q>
{

  /**  Creates a new {@link IC} queue with given {@link SimEventList}.
   * 
   * @param eventList The event list to use.
   * 
   */
  public IC (final SimEventList eventList)
  {
    super (eventList);
  }

  /**  Returns a new {@link IC} object on the same {@link SimEventList}.
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
  
  /** Returns zero.
   * 
   * @return 0
   * 
   */
  @Override
  protected final double getServiceTime (final J job)
  {
    return 0;
  }
  
  /** Returns "IC".
   * 
   * @return "IC".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "IC";
  }

}
