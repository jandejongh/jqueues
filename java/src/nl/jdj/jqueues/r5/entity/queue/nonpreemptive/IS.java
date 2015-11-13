package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link IS} queue serves all jobs simultaneously.
 *
 * Infinite Server.
 *
 * <p>
 * This queueing discipline, unlike e.g., {@link FCFS}, has multiple (actually an infinite number of) servers.
 *
 * <p>
 * In the presence of vacations, i.e., jobs are not immediately admitted to the servers,
 * this implementation respects the arrival order of jobs.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class IS<J extends SimJob, Q extends IS>
extends AbstractNonPreemptiveInfiniteServerSimQueue<J, Q>
{

  /** Returns the job-service time as obtained through {@link SimJob#getServiceTime} for this {@link IS}.
   * 
   * @return The job-service time as obtained through {@link SimJob#getServiceTime} for this {@link IS}.
   * 
   */
  @Override
  protected final double getServiceTime (final J job)
  {
    return job.getServiceTime (this);
  }
  
  /** Creates a new {@link IS} queue with given {@link SimEventList}.
   * 
   * @param eventList The event list to use.
   * 
   */
  public IS (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link IS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link IS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public IS<J, Q> getCopySimQueue ()
  {
    return new IS<> (getEventList ());
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
  
  /** Returns "IS".
   * 
   * @return "IS".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "IS";
  }

}
