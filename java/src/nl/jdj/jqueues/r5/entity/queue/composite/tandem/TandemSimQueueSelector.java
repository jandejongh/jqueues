package nl.jdj.jqueues.r5.entity.queue.composite.tandem;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for tandem queues.
 * 
 * <p>
 * In a tandem queue, a (delegate) job visits all sub-queues once in a predetermined sequence.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 *
 */
public class TandemSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  /** Creates a {@link SimQueueSelector} for a tandem queue.
   * 
   * @param queues The queues, must be non-{@code null}.
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry.
   * 
   */
  public TandemSimQueueSelector (final Set<DQ> queues)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = new LinkedHashSet<> (queues);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<DQ> queues;  
   
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the first {@link SimQueue} to visit by a delegate {@link SimJob}.
   * 
   * @return The first {@link SimQueue} returned by an iterator over the queue set,
   *         or <code>null</code> if that set is {@code null} or empty.
   * 
   */
  @Override
  public final DQ selectFirstQueue (final double time, final J job)
  {
    return ((queues == null || queues.isEmpty ()) ? null : queues.iterator ().next ());
  }
      
  /** Returns the next {@link SimQueue} to visit by a delegate {@link SimJob}.
   * 
   * @return The next {@link SimQueue} after the <code>previousQueue</code> in an iterator
   *         over the queue set,
   *         or <code>null</code> if that set is {@code null} or if no such element exists
   *         (i.e., <code>previousQueue</code> is the last element returned from the iterator).
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code> or not a member of the queue set.
   * 
   */
  @Override
  public final DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    if (queues == null || queues.isEmpty ())
      throw new IllegalStateException ();
    if (previousQueue == null)
      throw new IllegalStateException ();
    final Iterator<DQ> iterator = queues.iterator ();
    boolean found = false;
    while (iterator.hasNext () && ! found)
      found = (iterator.next () == previousQueue);
    if (! found)
      throw new IllegalStateException ();
    return (iterator.hasNext () ? iterator.next () : null);
  }
  
}
