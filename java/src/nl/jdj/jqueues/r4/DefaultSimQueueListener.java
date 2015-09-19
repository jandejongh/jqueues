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
  public void update (double t, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void arrival (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void start (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void drop (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void revocation (double t, J job, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void departure (double t, J job, Q queue)
  {
  }
  
}
