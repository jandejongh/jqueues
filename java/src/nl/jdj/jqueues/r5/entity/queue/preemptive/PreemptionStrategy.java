package nl.jdj.jqueues.r5.entity.queue.preemptive;

/** A list of possible strategies at {@link SimJob} preemption.
 *
 */
public enum PreemptionStrategy
{

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
  /** Departs the preempted job, even though it has not finished its service requirements.
   * 
   */
  DEPART,
  /** Takes a different approach at job preemption than mentioned in this list.
   * 
   */
  CUSTOM

}
