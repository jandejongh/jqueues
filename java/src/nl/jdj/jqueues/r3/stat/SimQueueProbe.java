package nl.jdj.jqueues.r3.stat;

import nl.jdj.jqueues.r3.SimQueue;

/** Object capable of obtaining a value (<code>double</code>) from a {@link SimQueue}.
 * 
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public interface SimQueueProbe<Q extends SimQueue>
{
  
  /** Gets the value from given queue.
   * 
   * @param queue The queue.
   * 
   * @return The value.
   * 
   * @throws IllegalArgumentException If the argument is <code>null</code> or of invalid type for this probe.
   * 
   */
  public double get (Q queue);
  
}
