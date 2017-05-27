package nl.jdj.jqueues.r5.entity.jq.queue.preemptive;

import nl.jdj.jqueues.r5.entity.jq.job.SimJob;

/** A list of possible strategies at {@link SimJob} preemption.
 *
 * @see AbstractPreemptiveSimQueue
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
public enum PreemptionStrategy
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PREEMPTION STRATEGY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Drops the preempted job.
   * 
   */
  DROP,
  /** Puts the preempted job on hold; future service resumption continues at the point where the previous service was interrupted.
   * 
   */
  RESUME,
  /** Puts the preempted job on hold; future service resumption requires the job to be served from scratch.
   * 
   */
  RESTART,
  /** Puts the preempted job on hold; future service resumption requires the job to be served from scratch
   *  with a new required service time.
   * 
   */
  REDRAW,
  /** Departs the preempted job, even though may not have finished its service requirements.
   * 
   */
  DEPART,
  /** Takes a different approach at job preemption than mentioned in this list.
   * 
   */
  CUSTOM

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
