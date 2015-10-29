package nl.jdj.jqueues.r4;

import java.util.Map;

/** A factory for {@link DefaultSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSimJob
 * 
 */
public class DefaultSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultSimJob, Q>
{

  /** Returns a new {@link DefaultSimJob} with given parameters.
   * 
   * @return A new {@link DefaultSimJob} with given parameters.
   * 
   * @see DefaultSimJob#DefaultSimJob(java.lang.String, java.util.Map)
   * 
   */
  @Override
  public DefaultSimJob newInstance (final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSimJob (name, requestedServiceTimeMap);
  }
  
}
