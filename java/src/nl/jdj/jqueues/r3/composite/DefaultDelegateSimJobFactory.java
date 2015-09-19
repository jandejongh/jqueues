package nl.jdj.jqueues.r3.composite;

import nl.jdj.jqueues.r3.AbstractSimJob;
import nl.jdj.jqueues.r3.SimJob;
import nl.jdj.jqueues.r3.SimQueue;

/**
 *
 */
public class DefaultDelegateSimJobFactory
<DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
implements DelegateSimJobFactory<AbstractSimJob, DQ, J, Q>
{

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
