package nl.jdj.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** A {@link SimQueueListener} that logs server-access-credits availability in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueue#setServerAccessCredits
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyOutOfServerAccessCredits
 * @see SimQueueListener#notifyRegainedServerAccessCredits
 *
 */
public class SimQueueServerAccessCreditsAvailabilityLogger
extends DefaultSimQueueListener
{
  
  private final List<Map<Double, Boolean>> sacLog = new ArrayList<> ();
  
  /** Returns the server-access-credits availability log (unmodifiable).
   * 
   * <p>
   * The log is constructed as a list of non-null and unique single-entry maps,
   * corresponding to a reported state change in server-access-credits availability.
   * The entry holds the time as its key and the new state of the server-access-credits availability as its value.
   * 
   * <p>
   * The server-access-credits availability log is automatically cleared upon a (reported) queue reset.
   * 
   * @return The server-access-credits availability log (unmodifiable).
   * 
   * @see SimQueueListener#notifyResetEntity
   * 
   */
  public final List<Map<Double, Boolean>> getServerAccessCreditsAvailabilityLog ()
  {
    return Collections.unmodifiableList (this.sacLog);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    this.sacLog.clear ();
  }
  
  @Override
  public void notifyRegainedServerAccessCredits (final double time, final SimQueue queue)
  {
    this.sacLog.add (Collections.singletonMap (time, true));
  }

  @Override
  public void notifyOutOfServerAccessCredits (final double time, final SimQueue queue)
  {
    this.sacLog.add (Collections.singletonMap (time, false));
  }

}
