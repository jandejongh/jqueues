package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link FCFS} queue serves jobs one at a time in order of arrival times.
 *
 * First Come First Served.
 * 
 * <p>
 * This implementation has infinite buffer size.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see FCFS_B
 * 
 */
public class FCFS<J extends SimJob, Q extends FCFS> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  /** Creates a FCFS queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public FCFS (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link FCFS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link FCFS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public FCFS<J, Q> getCopySimQueue ()
  {
    return new FCFS<> (getEventList ());
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
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
  
  /** Returns "FCFS".
   * 
   * @return "FCFS".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "FCFS";
  }

}