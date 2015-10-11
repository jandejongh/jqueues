package nl.jdj.jqueues.r4.composite;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jsimulation.r4.SimEventList;

/** A Jackson queueing network.
 *
 * A Jackson queueing network, for the purposes of this software framework, is a queueing system consisting of a fixed set of
 * (other) queueing systems between which the routing of {@link SimJob}s is determined by fixed probabilities,
 * both at arrival of a job at the aggregate queue, as well as upon departure of the job at one of the
 * constituent (sub)queues.
 * 
 * @param <DJ> The delegate-job type.
 * @param <DQ> The queue-type for delegate jobs.
 * @param <J>  The job type.
 * @param <Q>  The queue type for jobs.
 * 
 */
public class BlackJacksonSimQueueNetwork
  <DJ extends AbstractSimJob, DQ extends SimQueue, J extends SimJob, Q extends BlackJacksonSimQueueNetwork>
  extends AbstractBlackSimQueueNetwork<DJ, DQ, J, Q>
  implements BlackSimQueueNetwork<DJ, DQ, J, Q>
{
  
  // XXX The internals in this class could use some further documentation...
  
  private final double[] pArrivalCum;
  
  private final double[][] pTransitionCum;
  
  private final Random rng;
  
  private static void checkPArray (final double [] pArray, final int requiredSize)
  {
    if (pArray == null || pArray.length != requiredSize)
      throw new IllegalArgumentException ();
    int pArraySum = 0;
    for (final double p : pArray)
      if (p < 0 || p > 1)
        throw new IllegalArgumentException ();
      else
        pArraySum += p;
    if (pArraySum > 1)    
      throw new IllegalArgumentException ();
  }

  private static void checkPMatrix (final double[][] pMatrix, final int requiredSize)
  {
    if (pMatrix == null || pMatrix.length != requiredSize)
      throw new IllegalArgumentException ();
    for (final double[] p_row : pMatrix)
      checkPArray (p_row, requiredSize);    
  }
  
  private int getIndex (final DQ queue)
  {
    // XXX Should be in parent class.
    if (queue == null || getQueues () == null || ! getQueues ().contains (queue))
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = getQueues ().iterator ();
    for (int q = 0; q < getQueues ().size (); q++)
      if (iterator.next () == queue)
        return q;
    throw new RuntimeException ();
  }
  
  private DQ getQueue (final int q)
  {
    // XXX Should be in parent class.
    if (q < 0 || q >= getQueues ().size ())
      throw new IllegalArgumentException ();
    final Iterator<DQ> iterator = getQueues ().iterator ();
    int i = 0;
    DQ dq = iterator.next ();
    while (i < q)
    {
      i++;
      dq = iterator.next ();
    }
    return dq;
  }
  
  private DQ draw (final double[] pArrayCum)
  {
    if (pArrayCum == null || pArrayCum.length != getQueues ().size ())
      throw new IllegalArgumentException ();
    final double sample = this.rng.nextDouble ();
    if (sample < 0 || sample >= 1)
      throw new RuntimeException ();
    for (int q = 0; q < getQueues ().size (); q++)
      if (sample < pArrayCum[q])
        return getQueue (q);
    return null;
  }
  
  /** Creates a black Jackson queueing network.
   * 
   * For brevity, <code>|Q|</code> is used as a shorthand for <code>queues.size ()</code>.
   * 
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                                the non-<code>null</code> queues
   *                                as indexed in the probability arguments.
   * @param pArrival              The arrival probabilities for each queue;
   *                                must be array of size <code>|Q|</code>.
   * @param pTransition           The transition probabilities from/to each queue;
   *                                must be square matrix of size <code>|Q|x|Q|</code>.
   * @param userRNG               An optional user-supplied random-number generator
   *                                (if absent, a new one is created for local use).
   * @param delegateSimJobFactory An optional factory for the delegate {@link SimJob}s.
   *
   * @throws IllegalArgumentException If the event list is <code>null</code>,
   *                                  the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if one or both of the probability arguments is improperly dimensioned or contains
   *                                  illegal values.
   * 
   * @see DelegateSimJobFactory
   * @see DefaultDelegateSimJobFactory
   * 
   */
  public BlackJacksonSimQueueNetwork
  (final SimEventList eventList,
    final Set<DQ> queues,
    final double[] pArrival,
    final double[][] pTransition,
    final Random userRNG,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, delegateSimJobFactory);
    checkPArray (pArrival, queues.size ());
    this.pArrivalCum = new double[queues.size ()];
    for (int e = 0; e < queues.size (); e++)
      this.pArrivalCum[e] = pArrival[e] + ((e > 0) ? this.pArrivalCum[e-1] : 0);
    checkPMatrix (pTransition, queues.size ());
    this.pTransitionCum = new double[queues.size ()][queues.size ()];
    for (int r = 0; r < queues.size (); r++)
      for (int c = 0; c < queues.size (); c++)
        this.pTransitionCum[r][c] = pTransition[r][c] + ((c > 0) ? this.pTransitionCum[r][c-1] : 0);
    this.rng = ((userRNG != null) ? userRNG : new Random ());
  }

  /** Returns the queue selected (or <code>null</code> implying an immediate departure)
   * from a probabilistic experiment governed by the <code>pArrival</code>
   * array passed in the constructor.
   * 
   * {@inheritDoc}
   * 
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getFirstQueue (final double time, final J job)
  {
    return draw (this.pArrivalCum);
  }

  /** Returns the queue selected (or <code>null</code> implying a departure)
   * from a probabilistic experiment governed by the <code>pTransition</code>
   * matrix passed in the constructor.
   * 
   * {@inheritDoc}
   * 
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code> or not a member of {@link #getQueues}.
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getNextQueue (final double time, final J job, final DQ previousQueue)
  {
    return draw (this.pTransitionCum[getIndex (previousQueue)]);
  }
  
  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void newNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.newNoWaitArmed (time, queue, noWaitArmed);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  protected final void startForSubClass (final double t, final DJ job, final DQ queue)
  {
    super.startForSubClass (t, job, queue);
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void reset ()
  {
    super.reset ();
  }

  /** Calls super method (in order to make implementation final).
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  /** Returns "Jackson[queue list]".
   * 
   * @return "Jackson[queue list]".
   * 
   */
  @Override
  public String toString ()
  {
    String string = "Jackson[";
    boolean first = true;
    for (DQ dq : getQueues ())
    {
      if (! first)
        string += ",";
      else
        first = false;
      string += dq;
    }
    string += "]";
    return string;
  }

}
