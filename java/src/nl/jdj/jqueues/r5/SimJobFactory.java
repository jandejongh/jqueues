package nl.jdj.jqueues.r5;

import java.util.Map;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJobFactory;
import nl.jdj.jsimulation.r5.SimEventList;

/** A factory for {@link SimJob}s.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSimJobFactory
 * 
 */
@FunctionalInterface
public interface SimJobFactory<J extends SimJob, Q extends SimQueue>
{
  
  /** Creates a new {@link SimJob} with given name and requested-service time map.
   * 
   * @param eventList               The event list to use; may be (and in most case should be) <code>null</code>.
   * @param name                    The name of the new job; may be <code>null</code>.
   * @param requestedServiceTimeMap The requested service time for each key {@link SimQueue},
   *                                a <code>null</code> key can be used for unlisted {@link SimQueue}s.
   *                                If the map is <code>null</code> or no value could be found,
   *                                a factory default is used for the requested service time.
   * 
   * @return The new job.
   * 
   */
  public J newInstance (SimEventList eventList, String name, Map<Q, Double> requestedServiceTimeMap);
  
}
