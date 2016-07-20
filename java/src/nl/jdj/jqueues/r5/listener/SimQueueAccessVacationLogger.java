package nl.jdj.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.SimQueueListener;

/** A {@link SimQueueListener} that logs queue-access vacations in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueue#setQueueAccessVacation
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyStartQueueAccessVacation
 * @see SimQueueListener#notifyStopQueueAccessVacation
 *
 */
public class SimQueueAccessVacationLogger
extends DefaultSimQueueListener
{
  
  private final List<Map<Double, Boolean>> qavLog = new ArrayList<> ();
  
  /** Returns the queue-access vacation log (unmodifiable).
   * 
   * <p>
   * The log is constructed as a list of non-null and unique single-entry maps,
   * corresponding to a reported state change in queue-access vacation.
   * The entry holds the time as its key and the new state of the queue-access vacation as its value.
   * 
   * <p>
   * The queue-access vacation log is automatically cleared upon a (reported) queue reset.
   * 
   * @return The queue-access vacation log (unmodifiable).
   * 
   * @see SimQueueListener#notifyResetEntity
   * 
   */
  public final List<Map<Double, Boolean>> getQueueAccessVacationLog ()
  {
    return Collections.unmodifiableList (this.qavLog);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    this.qavLog.clear ();
  }
  
  @Override
  public void notifyStartQueueAccessVacation (final double time, final SimQueue queue)
  {
    this.qavLog.add (Collections.singletonMap (time, true));
  }

  @Override
  public void notifyStopQueueAccessVacation (final double time, final SimQueue queue)
  {
    this.qavLog.add (Collections.singletonMap (time, false));
  }

}
