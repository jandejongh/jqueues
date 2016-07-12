package nl.jdj.jqueues.r5.entity.queue.composite.dual.collector;

import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for drop-collector queues.
 *
 * <p>
 * A drop-collector is a composite queue with two queues, a main one and one collecting all dropped jobs from the main queue.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 * 
 */
public class DropCollectorSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a {@link SimQueueSelector} for a drop-collector queue.
   * 
   * @param mainQueue  The main queue.
   * @param dropQueue The drop queue.
   * 
   * @throws IllegalArgumentException If one of or both queues are <code>null</code>.
   * 
   */
  public DropCollectorSimQueueSelector (final DQ mainQueue, final DQ dropQueue)
  {
    if (mainQueue == null || dropQueue == null)
      throw new IllegalArgumentException ();
    this.mainQueue = mainQueue;
    this.dropQueue = dropQueue;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN AND DROP QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final DQ mainQueue;
  
  private final DQ dropQueue;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public DQ selectFirstQueue (final double time, final J job)
  {
    return this.mainQueue;
  }

  @Override
  public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (previousQueue == null || (previousQueue != this.mainQueue && previousQueue != this.dropQueue))
      throw new IllegalStateException ();
    return null;
  }
  
}
