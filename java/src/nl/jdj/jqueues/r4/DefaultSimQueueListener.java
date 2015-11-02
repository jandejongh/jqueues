package nl.jdj.jqueues.r4;

/** A {@link SimQueueListener} having empty implementations for all required methods to meet the interface.
 * 
 * Convenience class; override only the methods you need.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueueListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimEntityListener<J, Q>
implements SimQueueListener<J, Q>
{

  /** Does nothing.
   * 
   */
  @Override
  public void notifyUpdate (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyNewNoWaitArmed (final double time, final Q queue, final boolean noWaitArmed)
  {
  }
  
  /** Does nothing.
   * 
   */
  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
  }
  
}
