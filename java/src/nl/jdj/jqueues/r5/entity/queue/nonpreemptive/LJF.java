package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link LJF} queue serves jobs one at a time in order of descending requested service times.
 *
 * Longest-Job First.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SimJob#getServiceTime
 *
 */
public class LJF<J extends SimJob, Q extends LJF> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  /** Creates a LJF queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public LJF (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link LJF} object on the same {@link SimEventList}.
   * 
   * @return A new {@link LJF} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public LJF<J, Q> getCopySimQueue ()
  {
    return new LJF<> (getEventList ());
  }
  
  /** Inserts the job in the job queue maintaining non-increasing service-time ordering.
   * 
   * In case of ties, jobs are scheduled for service in order of arrival from the underlying event list.
   * 
   * @see #jobQueue
   * @see SimJob#getServiceTime
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    int newPosition = 0;
    while (newPosition < this.jobQueue.size ()
      && this.jobQueue.get (newPosition).getServiceTime (this) >= job.getServiceTime (this))
      newPosition++;
    this.jobQueue.add (newPosition, job);
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
  
  /** Returns "LJF".
   * 
   * @return "LJF".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "LJF";
  }

}
