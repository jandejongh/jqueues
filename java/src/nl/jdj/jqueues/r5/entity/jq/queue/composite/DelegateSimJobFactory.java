package nl.jdj.jqueues.r5.entity.jq.queue.composite;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** A factory for delegate {@link SimJob}s, as used in composite queues.
 * 
 * <p>
 * A delegate job visits {@link SimQueue}s on behalf of another job, the "real" job.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public interface DelegateSimJobFactory<DJ extends SimJob, DQ extends SimQueue, J extends SimJob, Q extends SimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new delegate {@link SimJob} for given "real" {@link SimJob}.
   * 
   * @param time  The current time.
   * @param job   The "real" job for which a delegate job is to be created.
   * @param queue The queue the "real" job is visiting and for which creation of a delegate job is required.
   * 
   * @return The delegate job.
   * 
   * @throws IllegalArgumentException If (e.g.) time is in the past, or if a <code>null</code> job or queue is passed.
   * 
   */
  public DJ newInstance (double time, J job, Q queue);
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
