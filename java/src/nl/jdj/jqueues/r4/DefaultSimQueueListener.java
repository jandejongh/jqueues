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
implements SimQueueListener<J, Q>
{

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyReset (final double oldTime, final Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyUpdate (double t, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyArrival (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyStart (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyDrop (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyNewNoWaitArmed (double t, Q queue, boolean noWaitArmed)
  {
  }
  
}
