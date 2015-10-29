package nl.jdj.jqueues.r4;

import nl.jdj.jsimulation.r4.SimEventListResetListener;

/** The interface common to both {@link SimJob}s and {@link SimQueue}s.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 * 
 * @see SimJob
 * @see SimQueue
 *
 */
public interface SimEntity<J extends SimJob, Q extends SimQueue>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NAME/toString
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Returns a default, type-specific name for this {@link SimEntity}.
   * 
   * <p>
   * The string is used as a fallback return value for <code>Object.toString ()</code>
   * in case the user did not set an instance-specific name
   * through {@link #setName}.
   * 
   * @return A default, type-specific name for this {@link SimEntity}.
   * 
   * @see #setName
   * 
   */
  public String toStringDefault ();
  
  /** Sets the name of this {@link SimEntity}, to be returned by subsequent calls to <code>Object.toString ()</code>.
   * 
   * @param name The new name of this job or queue; if non-<code>null</code>, the string will be supplied by subsequent calls
   *               to <code>Object.toString ()</code>; otherwise, the type-specific default will be used for that.
   * 
   * @see #toStringDefault
   * 
   */
  public void setName (String name);
 
}
