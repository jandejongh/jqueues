package nl.jdj.jqueues.r5.entity.queue.composite;

import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link DelegateSimJobFactory} for any (real) {@link SimJob}, as used in black composite queues.
 * 
 * <p>
 * It generates delegate jobs of type {@link AbstractSimJob} that redirect requests for
 * their required service time to a request of the corresponding real job at the real queue.
 *
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class DefaultDelegateSimJobFactory
<DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
implements DelegateSimJobFactory<AbstractSimJob, DQ, J, Q>
{

  /** Returns a new {@link AbstractSimJob} requesting the service time from the real job.
   * 
   * <p>
   * The name has empty (i.e. default) name and is not attached to the underlying event list (there is no need to).
   * 
   * @see AbstractSimJob#getServiceTime
   * 
   */
  @Override
  public AbstractSimJob newInstance (final double time, final J job, final Q queue)
  {
    return new AbstractSimJob (null, null)
    {
      @Override
      public final double getServiceTime (final SimQueue delegateQueue) throws IllegalArgumentException
      {
        // return 0.0;
        return job.getServiceTime (queue);
      }
    };
  }
  
}
