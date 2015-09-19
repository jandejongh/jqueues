package nl.jdj.jqueues.r4;

/** A convenience implementation (all methods are empty) of {@link SimQueueVacationListener}.
 *
 * @see DefaultSimQueueListener
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public class DefaultSimQueueVacationListener<J extends SimJob, Q extends SimQueue>
extends DefaultSimQueueListener<J, Q>
implements SimQueueVacationListener<J, Q>
{

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyStartQueueAccessVacation (double t, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyStopQueueAccessVacation (double t, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyOutOfServerAccessCredits (double t, Q queue)
  {
  }

  /**
   * {@inheritDoc}
   * 
   * Does nothing.
   * 
   */
  @Override
  public void notifyRegainedServerAccessCredits (double t, Q queue)
  {
  }
  
}
