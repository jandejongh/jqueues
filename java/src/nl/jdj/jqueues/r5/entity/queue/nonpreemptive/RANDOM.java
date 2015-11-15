package nl.jdj.jqueues.r5.entity.queue.nonpreemptive;

import java.util.Random;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jsimulation.r5.SimEventList;

/** The {@link RANDOM} queue serves jobs one at a time in random order.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class RANDOM<J extends SimJob, Q extends RANDOM> extends AbstractNonPreemptiveSingleServerSimQueue<J, Q>
{

  private final Random RNG;

  /** Returns the random-number generator used to sequence arriving jobs for service.
   * 
   * @return The random-number generator, non-<code>null</code>.
   * 
   */
  public final Random getRNG ()
  {
    return this.RNG;
  }
  
  /** Creates a RANDOM queue given an event list with new private {@link Random} random-number-generator.
   *
   * @param eventList The event list to use.
   *
   * @see Random
   * 
   */
  public RANDOM (final SimEventList eventList)
  {
    this (eventList, null);
  }

  /** Creates a RANDOM queue given an event list and {@link Random} random-number-generator.
   *
   * @param eventList The event list to use.
   * @param RNG The random-number-generator, if <code>null</code>, a new {@link Random} object will be created.
   *
   * @see Random
   * 
   */
  public RANDOM (final SimEventList eventList, final Random RNG)
  {
    super (eventList);
    this.RNG = ((RNG == null) ? new Random () : RNG);
  }
  
  /**  Returns a new {@link RANDOM} object on the same {@link SimEventList}.
   *
   * <p>
   * The new object has its own newly created {@link Random} RNG.
   * 
   * @return A new {@link RANDOM} object on the same {@link SimEventList}.
   * 
   * @see #getEventList
   * 
   */
  @Override
  public RANDOM<J, Q> getCopySimQueue ()
  {
    return new RANDOM<> (getEventList ());
  }
  
  /** Inserts the job at a random position the job queue.
   * 
   * @see #jobQueue
   * @see #getRNG
   * 
   */
  @Override
  protected final void insertJobInQueueUponArrival (final J job, final double time)
  {
    final int newPosition = getRNG ().nextInt (this.jobQueue.size () + 1);
    this.jobQueue.add (newPosition, job);
  }  
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }  
  
  /** Returns "RANDOM".
   * 
   * @return "RANDOM".
   * 
   */
  @Override
  public final String toStringDefault ()
  {
    return "RANDOM";
  }

}
