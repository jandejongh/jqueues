package nl.jdj.jqueues.r4.util.loadfactory;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** Abstract implementation of {@link LoadFactorySingleQueueSingleVisit}, mostly meant to store utility methods common to
 *  concrete implementations.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractLoadFactorySingleQueueSingleVisit<J extends SimJob, Q extends SimQueue>
implements LoadFactorySingleQueueSingleVisit<J, Q>
{
  
}
