package nl.jdj.jqueues.r4.composite;

import java.util.Set;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/**
 *
 */
public interface BlackSimQueueNetwork<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
extends SimQueue<J, Q>
{
  
  /** Returns the set of queues.
   * 
   * @return The non-<code>null</code> set of queues, each non-<code>null</code>.
   * 
   */
  public Set<? extends DQ> getQueues ();

}
