package nl.jdj.jqueues.r4;

/** A partial implementation of the common part of a {@link SimJob} and a {@link SimQueue}.
 * 
 * <p>
 * Currently, this class takes care of naming jobs and queues.
 * 
 * <p>
 * For a more complete (though still partial) implementations, see {@link AbstractSimQueue} and {@link AbstractSimJob}.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public abstract class AbstractSimEntity<J extends SimJob, Q extends SimQueue>
implements SimEntity<J, Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME, toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private String name = null;

  @Override
  public final void setName (final String name)
  {
    this.name = name;
  }
  
  /** Returns "AbstractSimEntity".
   * 
   * @return "AbstractSimEntity".
   * 
   */
  @Override
  public String toStringDefault ()
  {
    return "AbstractSimEntity";
  }
  
  /** Returns the internally stored user-supplied name, if non-<code>null</code>, or the type specific default.
   * 
   * @return The internally stored user-supplied name, if non-<code>null</code>, or the type specific default.
   * 
   * @see #setName
   * @see #toStringDefault
   * 
   */
  @Override
  public final String toString ()
  {
    if (this.name != null)
      return this.name;
    else
      return toStringDefault ();
  }
  
}
