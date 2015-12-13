package nl.jdj.jqueues.r5.entity.queue.mac;

import nl.jdj.jqueues.r5.entity.job.AbstractSimJob;

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

  public DCFSimJob ()
  {
    super (null, "DCFSimJob");
  }
  
}
