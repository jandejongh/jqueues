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

  /** Returns a new {@link AbstractSimJob} with zero requested service time.
   * 
   * {@inheritDoc}
   * 
   * @see AbstractSimJob#getServiceTime
   * 
   */
  @Override
  public AbstractSimJob newInstance (final double time, final J job)
  {
    return new AbstractSimJob ()
    {
      @Override
      public final double getServiceTime (final SimQueue queue) throws IllegalArgumentException
      {
        return 0.0;
      }
    };
  }
  
}
