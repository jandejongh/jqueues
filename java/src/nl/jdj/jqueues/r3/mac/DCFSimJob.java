package nl.jdj.jqueues.r3.mac;

import nl.jdj.jqueues.r3.AbstractSimJob;

/**
 *
 */
public class DCFSimJob
extends AbstractSimJob<DCFSimJob, DCF>
{

  @Override
  public double getServiceTime (final DCF queue)
  {
    return 0.0;
  }
  
}
