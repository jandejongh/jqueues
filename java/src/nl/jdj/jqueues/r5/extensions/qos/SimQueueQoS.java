package nl.jdj.jqueues.r5.extensions.qos;

import java.util.NavigableMap;
import java.util.Set;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;

/** A {@link SimQueue} with explicit QoS support.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * @param <P> The type used for QoS.
 * 
 */
public interface SimQueueQoS<J extends SimJob, Q extends SimQueueQoS, P extends Comparable>
extends SimEntityQoS<J, Q, P>, SimQueue<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FACTORY
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Overridden in order to restrict the return type.
   * 
   * {@inheritDoc}
   * 
   */
  @Override
  public SimQueueQoS<J, Q, P> getCopySimQueue () throws UnsupportedOperationException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // STATE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Gets the mapping QoS values onto jobs currently visiting this queue with that particular QoS value.
   *
   * <p>
   * Each {@link SimJob} present returning (valid) non-{@code null} QoS value from {@link SimJob#getQoS}
   * must be in present in only that value set,
   * and the union of these value sets must exactly match {@link #getJobs}.
   * Jobs returning {@code null} from {@link SimJob#getQoS} must be put in a value set corresponding to
   * {@link #getDefaultJobQoS}.
   * 
   * <p>
   * Most if not all concrete subclasses will impose an ordering on the value sets.
   * 
   * @return The mapping QoS values onto jobs currently visiting this queue with that particular QoS value.
   * 
   * @see #getJobs
   * @see SimJob#getQoS
   * @see #getDefaultJobQoS
   * 
   */
  public NavigableMap<P, Set<J>> getJobsQoSMap ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DEFAULT JOB QoS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Gets the default QoS value used by this queue for jobs that have {@code null} QoS value.
   * 
   * @return The default QoS value used by this queue for jobs that have {@code null} QoS value.
   * 
   * @see SimJob#getQoS
   * 
   */
  public P getDefaultJobQoS ();
  
}
