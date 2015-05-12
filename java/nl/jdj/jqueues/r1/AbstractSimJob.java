package nl.jdj.jqueues.r1;

import nl.jdj.jsimulation.r2.SimEventAction;

/** Convenience class with a basic implementation of {@link SimJob}.
 *
 * This class maintains an internal reference to the {@link SimQueue} being visited.
 * All methods returning {@link SimEventAction}s return <code>null</code>.
 * The method {@link #getServiceTime} is kept abstract on purpose, as forgetting to override a default implementation is
 * considered too risky.
 * 
 */
public abstract class AbstractSimJob
implements SimJob
{

  private SimQueue queue = null;
  
  /** Returns the internally stored {@link SimQueue} visiting.
   * 
   * @return The internally stored {@link SimQueue} visiting.
   * 
   */
  @Override
  public SimQueue getQueue ()
  {
    return this.queue;
  }

  /** Sets the internally stored {@link SimQueue} visiting.
   * 
   * @param queue The new {@link SimQueue} visiting, may be <code>null</code>.
   * 
   */
  @Override
  public void setQueue (SimQueue queue) throws IllegalStateException
  {
    this.queue = queue;
  }

  @Override
  public abstract double getServiceTime (SimQueue queue) throws IllegalArgumentException;
  
  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<SimJob> getQueueArriveAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<SimJob> getQueueRevokeAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<SimJob> getQueueStartAction ()
  {
    return null;
  }

  /** Returns <code>null</code>.
   * 
   * @return <code>null</code>.
   * 
   */
  @Override
  public SimEventAction<SimJob> getQueueDepartAction ()
  {
    return null;
  }
  
}
