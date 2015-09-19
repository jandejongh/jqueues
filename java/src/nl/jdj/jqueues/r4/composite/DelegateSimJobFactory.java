package nl.jdj.jqueues.r4.composite;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/** A delegate job visits {@link SimQueue}s on behalf of another job.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.

*/
public interface DelegateSimJobFactory<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
{
  
  /** Creates a new delegate {@link SimJob} for given {@link SimJob}.
   * 
   * @param time The current time.
   * @param job  The job for which a delegate job is to be created.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalArgumentException If (e.g.) time is in the past, or if a <code>null</code> job is passed.
   * 
   */
  public DJ newInstance (final double time, final J job);
  
}
