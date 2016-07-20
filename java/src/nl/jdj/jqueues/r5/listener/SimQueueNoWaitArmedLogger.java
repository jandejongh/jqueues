package nl.jdj.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** A {@link SimQueueListener} that logs {@code NoWaitArmed} in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyNewNoWaitArmed
 *
 */
public class SimQueueNoWaitArmedLogger
extends DefaultSimQueueListener
{
  
  private final List<Map<Double, Boolean>> nwaLog = new ArrayList<> ();
  
  /** Returns the {@code NoWaitArmed} log (unmodifiable).
   * 
   * <p>
   * The log is constructed as a list of non-null and unique single-entry maps,
   * corresponding to a reported state change in {@code NoWaitArmed}.
   * The entry holds the time as its key and the new {@code NoWaitArmed} as its value.
   * 
   * <p>
   * The {@code NoWaitArmed} log is automatically cleared upon a (reported) queue reset.
   * 
   * @return The {@code NoWaitArmed} log (unmodifiable).
   * 
   * @see SimQueueListener#notifyResetEntity
   * 
   */
  public final List<Map<Double, Boolean>> getNoWaitArmedLog ()
  {
    return Collections.unmodifiableList (this.nwaLog);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    this.nwaLog.clear ();
  }

  @Override
  public void notifyNewNoWaitArmed (final double time, final SimQueue queue, final boolean noWaitArmed)
  {
    this.nwaLog.add (Collections.singletonMap (time, noWaitArmed));
  }
  
}
