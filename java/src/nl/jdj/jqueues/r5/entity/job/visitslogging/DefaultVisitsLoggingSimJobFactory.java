package nl.jdj.jqueues.r5.entity.job.visitslogging;

import java.util.Map;
import nl.jdj.jqueues.r5.SimJobFactory;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link DefaultVisitsLoggingSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultVisitsLoggingSimJob
 * 
 */
public class DefaultVisitsLoggingSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultVisitsLoggingSimJob, Q>
{

  /** Returns a new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @return A new {@link DefaultVisitsLoggingSimJob} with given parameters.
   * 
   * @see DefaultVisitsLoggingSimJob#DefaultVisitsLoggingSimJob
   * 
   */
  @Override
  public DefaultVisitsLoggingSimJob newInstance
  (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultVisitsLoggingSimJob (eventList, name, requestedServiceTimeMap);
  }
  
}
