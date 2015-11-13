package nl.jdj.jqueues.r5.util.loadfactory;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** Abstract implementation of {@link LoadFactory_SQ_SV}, mostly meant to store utility methods common to
 *  concrete implementations.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractLoadFactory_SQ_SV<J extends SimJob, Q extends SimQueue>
implements LoadFactory_SQ_SV<J, Q>
{
  
}
