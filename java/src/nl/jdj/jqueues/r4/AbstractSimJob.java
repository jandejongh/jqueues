package nl.jdj.jqueues.r4;

import nl.jdj.jsimulation.r4.SimEventAction;

/** Convenience class with a basic (yet still abstract) implementation of {@link SimJob}.
 *
 * This class maintains an internal reference to the {@link SimQueue} being visited.
 * All methods returning {@link SimEventAction}s return <code>null</code>.
 * The method {@link #getServiceTime} is kept abstract on purpose, as forgetting to override a default implementation is
 * considered too risky.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see DefaultSimJob
 * 
 */
public abstract class AbstractSimJob<J extends SimJob, Q extends SimQueue>
extends AbstractSimEntity<J, Q>
implements SimJob<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Creates a new {@link SimJob} with given name.
   * 
   * @param name The name of the job, may be <code>null</code>.
   * 
   * @see #setName
   * 
   */
  public AbstractSimJob (final String name)
  {
    super (name);
  }
    
  /** Creates a new {@link SimJob} with <code>null</code> (initial) name.
   * 
   * @see #setName
   * 
   */
  public AbstractSimJob ()
  {
    this (null);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private Q queue = null;
  
  /** Returns the internally stored {@link SimQueue} visiting.
   * 
   * @return The internally stored {@link SimQueue} visiting.
   * 
   */
  @Override
  public final Q getQueue ()
  {
    return this.queue;
  }

  /** Sets the internally stored {@link SimQueue} visiting.
   * 
   * @param queue The new {@link SimQueue} visiting, may be <code>null</code>.
   * 
   */
  @Override
  public final void setQueue (Q queue) throws IllegalStateException
  {
    this.queue = queue;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVICE TIME
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  @Override
  public abstract double getServiceTime (Q queue) throws IllegalArgumentException;
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE ACTIONS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
