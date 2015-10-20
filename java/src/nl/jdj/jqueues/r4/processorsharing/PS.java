package nl.jdj.jqueues.r4.processorsharing;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** The {@link PS} queue serves all jobs simultaneously, equally distributing its service capacity.
 *
 * <p>
 * WORK IN PROGRESS! ABSTRACT FOR NOW!
 * 
 * <p>
 * Processor Sharing.
 * 
 * <p>
 * This implementation has infinite buffer size.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class PS<J extends SimJob, Q extends PS> extends AbstractProcessorSharingSimQueue<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a PS queue given an event list.
   *
   * @param eventList The event list to use.
   *
   */
  public PS (final SimEventList eventList)
  {
    super (eventList);
  }
  
  /** Returns a new {@link PS} object on the same {@link SimEventList}.
   * 
   * @return A new {@link PS} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public PS<J, Q> getCopySimQueue ()
  {
    // return new PS<> (getEventList ());
    throw new UnsupportedOperationException ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "PS".
   * 
   * @return "PS".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "PS";
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns <code>true</code>.
   * 
   * @return True.
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return true;
  }

  private double virtualTime = 0;
  
  protected final double getVirtualTime ()
  {
    return this.virtualTime;
  }
  
  private final SortedMap<Double, Set<J>> departureEpochsInVirtualTime = new TreeMap<> ();
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Updates the virtual time and calls super method (in that order!).
   * 
   * @see #getNumberOfJobsExecuting
   * @see #getVirtualTime
   * @see #getLastUpdateTime
   * 
   */
  @Override
  public final void update (final double time)
  {
    if (time < getLastUpdateTime ())
      throw new IllegalStateException ();
    final int numberOfJobsExecuting = getNumberOfJobsExecuting ();
    if (numberOfJobsExecuting == 0)
      this.virtualTime = 0;
    else if (time > getLastUpdateTime ())
      this.virtualTime += ((time - getLastUpdateTime ()) / numberOfJobsExecuting);
    // Super method will set this.lastUpdateTime to time.
    // That is why it should not be called first, or we will lose this.lastUpdateTime.
    super.update (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MAIN OPERATIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method, resets the virtual time to zero and clears the departure-epochs mapping.
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
    this.virtualTime = 0;
    this.departureEpochsInVirtualTime.clear ();
  }
  
  /** Inserts the job at the tail of the job queue.
   * 
   * @see #jobQueue
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    this.jobQueue.add (job);
  }

}
