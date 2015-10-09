package nl.jdj.jqueues.r4.mac;

import nl.jdj.jqueues.r4.composite.*;

/**
 *
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class DCFSimJobForEDCAFactory
<DQ extends DCF, J extends EDCASimJob, Q extends EDCA>
implements DelegateSimJobFactory<DCFSimJobForEDCA, DQ, J, Q>
{

  /** Returns a new {@link DCFSimJob}.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public DCFSimJobForEDCA newInstance (final double time, final J job, final Q queue)
  {
    return new DCFSimJobForEDCA (job);
  }
  
}
