package nl.jdj.jqueues.r5;

import nl.jdj.jsimulation.r4.SimEventList;

/** A factory for {@link SimQueue}s.
 * 
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
@FunctionalInterface
public interface SimQueueFactory<J extends SimJob, Q extends SimQueue>
{
  
  /** Creates a new {@link SimQueue} with given name.
   * 
   * @param eventList The event list to use; could be (but in most cases should <i>not</i>be) <code>null</code>.
   * @param name      The name of the new queue; may be <code>null</code>.
   * 
   * @return The new queue.
   * 
   */
  public Q newInstance (SimEventList eventList, String name);
  
}
