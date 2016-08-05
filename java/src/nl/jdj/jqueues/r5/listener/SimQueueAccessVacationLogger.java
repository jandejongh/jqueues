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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MATCH QueueAccessVacation AVAILABILITY LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Compares two queue-access-vacation logs (predicted and actual), and reports inequalities to {@link System#err}.
   * 
   * @param predictedQavLogs The predicted queue-access-vacation logs.
   * @param actualQavLogs    The actual queue-access-vacation logs.
   * @param accuracy         The allowed deviation in key values (times-of-change).
   * 
   * @return {@code true} if the two logs match.
   * 
   * @throws IllegalArgumentException If an argument is {@code null} or improperly structured.
   * 
   */
  public static boolean matchQueueAccessVacationLogs
  (final List<Map<Double, Boolean>> predictedQavLogs, final List<Map<Double, Boolean>> actualQavLogs, final double accuracy)
  {
    if (predictedQavLogs == null || actualQavLogs == null)
      throw new IllegalArgumentException ();
    boolean retVal = true;
    if (predictedQavLogs.size () != actualQavLogs.size ())
      retVal = false;
    else
      for (int i = 0; i < predictedQavLogs.size (); i++)
      {
        if (predictedQavLogs.get (i).size () != 1 || actualQavLogs.get (i).size () != 1)
          throw new IllegalArgumentException ();
        if (Math.abs (predictedQavLogs.get (i).keySet ().iterator ().next ()
                    - actualQavLogs.get (i).keySet ().iterator ().next ())
                    > accuracy)
        {
          retVal = false;
          break;
        }
        if (! predictedQavLogs.get (i).values ().iterator ().next ().equals (actualQavLogs.get (i).values ().iterator ().next ()))
        {
          retVal = false;
          break;
        }
      }
    if (! retVal)
    {
      System.err.println ("Queue-Access Vacation Logs mismatch!");
      System.err.println ("  Predicted: " + predictedQavLogs + ".");
      System.err.println ("  Actual   : " + actualQavLogs + ".");
    }
    return retVal;
  }
  
}
