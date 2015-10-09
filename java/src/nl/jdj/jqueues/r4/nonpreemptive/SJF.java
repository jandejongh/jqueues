package nl.jdj.jqueues.r4.nonpreemptive;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link SJF} queue serves jobs one at a time in order of ascending requested service times.
 *
 * Shortest-Job First.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 * @see SimJob#getServiceTime
 *
 */
public class SJF<J extends SimJob, Q extends SJF> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  /** Creates a SJF queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public SJF (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Inserts the job in the job queue maintaining non-decreasing service-time ordering.
   * 
   * {@inheritDoc}
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
      && this.jobQueue.get (newPosition).getServiceTime (this) <= job.getServiceTime (this))
      newPosition++;
    this.jobQueue.add (newPosition, job);    
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }  
  
  /** Returns "SJF".
   * 
   * @return "SJF".
   * 
   */
  @Override
  public String toString ()
  {
    return "SJF";
  }

}
