package nl.jdj.jqueues.r3.composite;

import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;

/**
 *
 */
public interface DelegateSimJobFactory<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
{
  
  /** Creates a new delegate {@link DelegateSimJob} for given {@link SimJob}.
   * 
   */
  public DJ newInstance (final double time, final J job);
  
}
