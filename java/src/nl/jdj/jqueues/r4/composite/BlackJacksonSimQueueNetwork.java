package nl.jdj.jqueues.r4.composite;

import java.util.Arrays;
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Checks a probability-distribution array for dimension and values.
   * 
   * @param pdfArray The pdf array.
   * @param requiredSize The required size of the array.
   * 
   * @throws IllegalArgumentException If the array is <code>null</code> or not of the required size,
   *                                  or any of its members is strictly negative or exceeds unity,
   *                                  or the sum of its members exceeds unity.
   * 
   */
  protected static void checkPdfArray (final double [] pdfArray, final int requiredSize)
  {
    if (pdfArray == null || pdfArray.length != requiredSize)
      throw new IllegalArgumentException ();
    int pArraySum = 0;
    for (final double p : pdfArray)
      if (p < 0 || p > 1)
        throw new IllegalArgumentException ();
      else
        pArraySum += p;
    if (pArraySum > 1)    
      throw new IllegalArgumentException ();
  }

  /** Checks a probability-distribution matrix for dimension and values.
   * 
   * @param pdfMatrix The pdf matrix.
   * @param requiredSize The required (one-dimensional) size of the array.
   * 
   * @throws IllegalArgumentException If the matrix is <code>null</code> or not square with the required size,
   *                                  or any of its members is strictly negative or exceeds unity,
   *                                  or one or more of its row sums exceeds unity.
   * 
   */
  protected static void checkPdfMatrix (final double[][] pdfMatrix, final int requiredSize)
  {
    if (pdfMatrix == null || pdfMatrix.length != requiredSize)
      throw new IllegalArgumentException ();
    for (final double[] p_row : pdfMatrix)
      checkPdfArray (p_row, requiredSize);    
  }
  
  /** Creates a black Jackson queueing network.
   * 
   * For brevity, <code>|Q|</code> is used as a shorthand for <code>queues.size ()</code>.
   * 
   * @param eventList             The event list to use.
   * @param queues                The queues, an iteration over the set must return (deterministically)
   *                                the non-<code>null</code> queues
   *                                as indexed in the probability arguments.
   * @param pdfArrival            The arrival probabilities for each queue (pdf);
   *                                must be array of size <code>|Q|</code>.
   *                              Entries must be between zero and unity and their sum should not exceed unity.
   *                              The "remainder" denotes the probability that a job departs immediately upon arrival.
   *                              This argument is copied for future use in the constructor.
   * @param pdfTransition         The transition probabilities from/to each queue (pdf for each (source) queue);
   *                                must be square matrix of size <code>|Q|x|Q|</code>.
   *                              Entries must be between zero and unity and each row sum should not exceed unity.
   *                              The "remainder" in a row denotes the probability that a job departs after a visit to the
   *                                corresponding queue.
   *                              This argument is (deep-)copied for future use in the constructor.
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
    final double[] pdfArrival,
    final double[][] pdfTransition,
    final Random userRNG,
    final DelegateSimJobFactory delegateSimJobFactory)
  {
    super (eventList, queues, delegateSimJobFactory);
    checkPdfArray (pdfArrival, queues.size ());
    this.pdfArrival = Arrays.copyOf (pdfArrival, pdfArrival.length);
    this.cdfArrival = new double[queues.size ()];
    for (int e = 0; e < queues.size (); e++)
      this.cdfArrival[e] = this.pdfArrival[e] + ((e > 0) ? this.cdfArrival[e-1] : 0);
    checkPdfMatrix (pdfTransition, queues.size ());
    this.pdfTransition = new double[queues.size ()][];
    for (int r = 0; r < queues.size (); r++)
      this.pdfTransition[r] = Arrays.copyOf (pdfTransition[r], queues.size ());
    this.cdfTransition = new double[queues.size ()][queues.size ()];
    for (int r = 0; r < queues.size (); r++)
      for (int c = 0; c < queues.size (); c++)
        this.cdfTransition[r][c] = this.pdfTransition[r][c] + ((c > 0) ? this.cdfTransition[r][c-1] : 0);
    this.rng = ((userRNG != null) ? userRNG : new Random ());
  }

  /** Returns a new {@link BlackJacksonSimQueueNetwork} object on the same {@link SimEventList} with copies of the sub-queues and
   *  probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @return A new {@link BlackJacksonSimQueueNetwork} object on the same {@link SimEventList} with copies of the sub-queues and
   *  probability arguments, a new RNG, and the same delegate-job factory.
   * 
   * @throws UnsupportedOperationException If the encapsulated queues could not be copied through {@link SimQueue#getCopySimQueue}.
   * 
   * @see #getEventList
   * @see #getCopySubSimQueues
   * @see #getDelegateSimJobFactory
   * 
   */
  @Override
  public BlackJacksonSimQueueNetwork<DJ, DQ, J, Q> getCopySimQueue ()
  {
    final Set<DQ> queuesCopy = getCopySubSimQueues ();
    return new BlackJacksonSimQueueNetwork<>
      (getEventList (), queuesCopy, this.pdfArrival, this.pdfTransition, null, getDelegateSimJobFactory ());
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // AbstractBlackSimQueueNetwork
  // ABSTRACT METHODS FOR (SUB-)QUEUE SELECTION IN SUBCLASSES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Selects a (sub-)queue by drawing from {@link #getRNG} and respecting the given cumulative distribution function,
   * with an entry for each (sub-)queue.
   * 
   * @param cdfArray The cumulative distribution function for queue selection, with an entry for each (sub-)queue.
   * 
   * @return The selected queue, may be <code>null</code>.
   * 
   * @see #getRNG
   * 
   */
  private DQ draw (final double[] cdfArray)
  {
    if (cdfArray == null || cdfArray.length != getQueues ().size ())
      throw new IllegalArgumentException ();
    final double sample = this.rng.nextDouble ();
    if (sample < 0 || sample >= 1)
      throw new RuntimeException ();
    for (int q = 0; q < getQueues ().size (); q++)
      if (sample < cdfArray[q])
        return getQueue (q);
    return null;
  }
  
  /** Returns the queue selected (or <code>null</code> implying an immediate departure)
   * from a probabilistic experiment governed by the <code>pdfArrival</code>
   * array passed in the constructor.
   * 
   * @see #draw
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getFirstQueue (final double time, final J job)
  {
    return draw (this.cdfArrival);
  }

  /** Returns the queue selected (or <code>null</code> implying a departure)
   * from a probabilistic experiment governed by the <code>pdfTransition</code>
   * matrix passed in the constructor.
   * 
   * @throws IllegalStateException If the previous queue argument is <code>null</code> or not a member of {@link #getQueues}.
   * 
   * @see #draw
   * 
   */
  @Override
  protected final SimQueue<DJ, DQ> getNextQueue (final double time, final J job, final DQ previousQueue)
  {
    return draw (this.cdfTransition[getIndex (previousQueue)]);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns "Jackson[queue list]".
   * 
   * @return "Jackson[queue list]".
   * 
   */
  @Override
  public final String toStringDefault ()
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL AND TRANSITION PROBABILITIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The arrival probabilities (as probability distribution function).
   * 
   */
  private final double[] pdfArrival;
  
  /** The arrival probabilities (as cumulative distribution function).
   * 
   */
  private final double[] cdfArrival;
  
  /** The transition probabilities for each (source) queue in turn (as probability distribution function).
   * 
   */
  private final double[][] pdfTransition;
  
  /** The transition probabilities for each (source) queue in turn (as cumulative distribution function).
   * 
   */
  private final double[][] cdfTransition;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RANDOM NUMBER GENERATOR
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The Random Number Generator used for arrivals and transitions.
   * 
   */
  private final Random rng;
  
  /** Returns the Random Number Generator used for arrivals and transitions.
   * 
   * @return The Random Number Generator used for arrivals and transitions (non-<code>null</code>).
   * 
   */
  public final Random getRNG ()
  {
    return this.rng;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DROP DESTINATION QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final DQ getDropDestinationQueue (final double t, final DJ job, final DQ queue)
  {
    return super.getDropDestinationQueue (t, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowDelegateJobRevocations
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowDelegateJobRevocations ()
  {
    return false;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // allowSubQueueAccessVacationChanges
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns {@code false}.
   * 
   * @return {@code false}.
   * 
   */
  @Override
  protected final boolean getAllowSubQueueAccessVacationChanges ()
  {
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // RESET
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method and clear the administration of visits.
   * 
   */
  @Override
  public final void resetEntitySubClass ()
  {
    super.resetEntitySubClass ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // UPDATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void update (final double time)
  {
    super.update (time);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // noWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final boolean isNoWaitArmed ()
  {
    return super.isNoWaitArmed ();
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // startForSubClass
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  protected final void startForSubClass (final double time, final DJ job, final DQ queue)
  {
    super.startForSubClass (time, job, queue);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // notifyNewNoWaitArmed
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Calls super method (in order to make implementation final).
   * 
   */
  @Override
  public final void notifyNewNoWaitArmed (final double time, final DQ queue, final boolean noWaitArmed)
  {
    super.notifyNewNoWaitArmed (time, queue, noWaitArmed);
  }

}
