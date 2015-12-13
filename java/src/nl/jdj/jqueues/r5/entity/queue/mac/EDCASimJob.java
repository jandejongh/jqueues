package nl.jdj.jqueues.r5.entity.queue.mac;

import nl.jdj.jqueues.r5.entity.job.qos.DefaultSimJobQoS;

/**
 *
 */
public class EDCASimJob<P extends Enum<P> & AC>
extends DefaultSimJobQoS<EDCASimJob, EDCA, P>
{

  public EDCASimJob (final Class<P> qosClass, final P qos)
  {
    super (null, "EDCASimJob", 0.0, qosClass, qos);
  }

}
