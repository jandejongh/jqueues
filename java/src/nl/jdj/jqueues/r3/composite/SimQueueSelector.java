package nl.jdj.jqueues.r3.composite;

import nl.jdj.jqueues.r3.SimJob;
import nl.jdj.jqueues.r3.SimQueue;

/**
 *
 */
public interface SimQueueSelector<J extends SimJob, DJ extends SimJob, DQ extends SimQueue>
{
  
  /** Returns the first queue to visit for an arriving job.
   * 
   * @param time The time of arrival of the job.
   * @param job The job, non-<code>null</code>.
   * 
   * @return The first queue to visit, if <code>null</code>, the job is to depart immediately.
   * 
   */
  public SimQueue<DJ, DQ> selectFirstQueue (double time, J job);
  
  /** Returns the next queue to visit for a job.
   * 
   * @param time The current time, i.e., the departure time of the job at its previous queue.
   * @param job The job, non-<code>null</code>.
   * @param previousQueue The previous queue the job visited, and just departed from.
   * 
   * @return The next queue to visit, if <code>null</code>, the job is to depart immediately.
   * 
   */
  public SimQueue<DJ, DQ> selectNextQueue (double time, J job, DQ previousQueue);
  
}
