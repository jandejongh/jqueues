package nl.jdj.jqueues.r3;

import nl.jdj.jsimulation.r4.SimEventAction;

/** Convenience class with a basic implementation of {@link SimJob}.
 *
 * This class maintains an internal reference to the {@link SimQueue} being visited.
 * All methods returning {@link SimEventAction}s return <code>null</code>.
 * The method {@link #getServiceTime} is kept abstract on purpose, as forgetting to override a default implementation is
 * considered too risky.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 */
public abstract class AbstractSimJob<J extends SimJob, Q extends SimQueue>
implements SimJob<J, Q>
{

  private Q queue = null;
  
  /** Returns the internally stored {@link SimQueue} visiting.
   * 
   * @return The internally stored {@link SimQueue} visiting.
   * 
   */
  @Override
  public Q getQueue ()
  {
    return this.queue;
  }

  /** Sets the internally stored {@link SimQueue} visiting.
   * 
   * @param queue The new {@link SimQueue} visiting, may be <code>null</code>.
   * 
   */
  @Override
  public void setQueue (Q queue) throws IllegalStateException
  {
    this.queue = queue;
  }

  @Override
  public abstract double getServiceTime (Q queue) throws IllegalArgumentException;
  
  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<J> getQueueArriveAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<J> getQueueStartAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<J> getQueueDropAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<J> getQueueRevokeAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<J> getQueueDepartAction ()
  {
    return null;
  }
  
}
