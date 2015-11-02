package nl.jdj.jqueues.r4.util.jobfactory;

import java.util.Map;
import nl.jdj.jqueues.r4.SimJobFactory;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A factory for {@link DefaultSelfListeningSimJob}s.
 *
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSelfListeningSimJob
 * 
 */
public class DefaultSelfListeningSimJobFactory<Q extends SimQueue>
implements SimJobFactory<DefaultSelfListeningSimJob, Q>
{

  /** Returns a new {@link DefaultSelfListeningSimJob} with given parameters.
   * 
   * @return A new {@link DefaultSelfListeningSimJob} with given parameters.
   * 
   * @see DefaultSelfListeningSimJob#DefaultSelfListeningSimJob
   * 
   */
  @Override
  public DefaultSelfListeningSimJob newInstance
  (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    return new DefaultSelfListeningSimJob (eventList, name, requestedServiceTimeMap);
  }
  
}
