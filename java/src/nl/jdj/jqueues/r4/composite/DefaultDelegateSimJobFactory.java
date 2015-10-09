package nl.jdj.jqueues.r4.composite;

import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/**
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
   * {@inheritDoc}
   * 
   * @see AbstractSimJob#getServiceTime
   * 
   */
  @Override
  public AbstractSimJob newInstance (final double time, final J job, final Q queue)
  {
    return new AbstractSimJob ()
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
