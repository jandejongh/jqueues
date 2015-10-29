package nl.jdj.jqueues.r4;

import java.util.Map;

/** A factory for {@link SimJob}s.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @see DefaultSimJobFactory
 * 
 */
public interface SimJobFactory<J extends SimJob, Q extends SimQueue>
{
  
  /** Creates a new {@link SimJob} with given name and requested-service time map.
   * 
   * @param name                    The name of the new job; may be <code>null</code>.
   * @param requestedServiceTimeMap The requested service time for each key {@link SimQueue},
   *                                a <code>null</code> key can be used for unlisted {@link SimQueue}s.
   *                                If the map is <code>null</code> or no value could be found,
   *                                a factory default is used for the requested service time.
   * 
   * @return The new job.
   * 
   */
  public J newInstance (String name, Map<Q, Double> requestedServiceTimeMap);
  
  /** Creates a new {@link SimJob} with given name and <code>null</code> requested-service time map.
   * 
   * @param name The name of the new job; may be <code>null</code>.
   * 
   * @return The new job.
   * 
   */
  default J newInstance (String name)
  {
    return newInstance (name, null);
  }
  
  /** Creates a new {@link SimJob} with <code>null</code> name and given requested-service time map.
   * 
   * @param requestedServiceTimeMap The requested service time for each key {@link SimQueue},
   *                                a <code>null</code> key can be used for unlisted {@link SimQueue}s.
   *                                If the map is <code>null</code> or no value could be found,
   *                                a factory default is used for the requested service time.
   * 
   * @return The new job.
   * 
   */
  default J newInstance (Map<Q, Double> requestedServiceTimeMap)
  {
    return newInstance (null, requestedServiceTimeMap);
  }
  
  /** Creates a new {@link SimJob} with <code>null</code> name and <code>null</code> requested-service time map.
   * 
   * @return The new job.
   * 
   */
  default J newInstance ()
  {
    return newInstance (null, null);
  }
  
}
