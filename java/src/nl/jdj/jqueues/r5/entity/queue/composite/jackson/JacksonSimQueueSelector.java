package nl.jdj.jqueues.r5.entity.queue.composite.jackson;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.AbstractSimQueueComposite;
import nl.jdj.jqueues.r5.entity.queue.composite.SimQueueSelector;

/** A {@link SimQueueSelector} for Jackson networks.
 * 
 * <p>
 * A Jackson queueing network, for the purposes of this software framework, is a queueing system consisting of a fixed set of
 * (other) queueing systems between which the routing of {@link SimJob}s is determined by fixed probabilities,
 * both at arrival of a job at the aggregate queue, as well as upon departure of the job at one of the
 * constituent (sub)queues.
 * 
 * @param <J>  The job type.
 * @param <DQ> The queue-type for delegate jobs.
 *
 */
public class JacksonSimQueueSelector<J extends SimJob, DQ extends SimQueue>
implements SimQueueSelector<J, DQ>
{

  /** Creates a {@link SimQueueSelector} for a Jackson network.
   * 
   * @param queues        The queues, must be non-{@code null}.
   * @param pdfArrival    The arrival probabilities for each queue (pdf);
   *                        must be array of size <code>|Q|</code>.
   *                      Entries must be between zero and unity and their sum should not exceed unity.
   *                      The "remainder" denotes the probability that a job departs immediately upon arrival.
   *                      This argument is copied.
   * @param pdfTransition The transition probabilities from/to each queue (pdf for each (source) queue);
   *                        must be square matrix of size <code>|Q|x|Q|</code>.
   *                      Entries must be between zero and unity and each row sum should not exceed unity.
   *                      The "remainder" in a row denotes the probability that a job departs after a visit to the
   *                        corresponding queue.
   *                      This argument is (deep-)copied.
   * @param userRNG       An optional user-supplied random-number generator
   *                        (if absent, a new one is created for local use).
   * 
   * @throws IllegalArgumentException If the <code>queues</code> argument is <code>null</code>,
   *                                  or if it contains a <code>null</code> entry,
   *                                  or if one or both of the probability arguments is improperly dimensioned or contains
   *                                  illegal values.
   * 
   */
  public JacksonSimQueueSelector
   (final Set<DQ> queues,
    final double[] pdfArrival,
    final double[][] pdfTransition,
    final Random userRNG)
  {
    if (queues == null || queues.contains (null))
      throw new IllegalArgumentException ();
    this.queues = new LinkedHashSet<> (queues);
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final Set<DQ> queues;  
   
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PDF UTILITY METHODS
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ARRIVAL AND TRANSITION PROBABILITIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** The arrival probabilities (as probability distribution function).
   * 
   */
  private final double[] pdfArrival;
  
  /** Gets the arrival probabilities (as probability distribution function).
   * 
   * @return The arrival probabilities (as probability distribution function).
   * 
   */
  public final double[] getPdfArrival ()
  {
    return this.pdfArrival;
  }
  
  /** The arrival probabilities (as cumulative distribution function).
   * 
   */
  private final double[] cdfArrival;
  
  /** The transition probabilities for each (source) queue in turn (as probability distribution function).
   * 
   */
  private final double[][] pdfTransition;
  
  /** Gets the transition probabilities for each (source) queue in turn (as probability distribution function).
   * 
   * @return The transition probabilities for each (source) queue in turn (as probability distribution function).
   * 
   */
  public final double[][] getPdfTransition ()
  {
    return this.pdfTransition;
  }
  
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
    if (cdfArray == null || cdfArray.length != this.queues.size ())
      throw new IllegalArgumentException ();
    final double sample = this.rng.nextDouble ();
    if (sample < 0 || sample >= 1)
      throw new RuntimeException ();
    for (int q = 0; q < this.queues.size (); q++)
      if (sample < cdfArray[q])
        return AbstractSimQueueComposite.getQueue (this.queues, q);
    return null;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueSelector
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns the queue selected (or <code>null</code> implying an immediate departure)
   * from a probabilistic experiment governed by the <code>pdfArrival</code>
   * array passed in the constructor.
   * 
   * @see #draw
   * 
   */
  @Override
  public DQ selectFirstQueue (final double time, final J job)
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
  public DQ selectNextQueue (final double time, final J job, final DQ previousQueue)
  {
    return draw (this.cdfTransition[AbstractSimQueueComposite.getIndex (this.queues, previousQueue)]);
  }
  
}
