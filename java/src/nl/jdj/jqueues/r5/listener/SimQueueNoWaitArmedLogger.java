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
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MATCH NoWaitArmed LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Compares two {@code NoWaitArmed} logs (predicted and actual), and reports inequalities to {@link System#err}.
   * 
   * @param predictedNwaLogs The predicted {@code NoWaitArmed} logs.
   * @param actualNwaLogs    The actual {@code NoWaitArmed} logs.
   * 
   * @return {@code true} if the two logs match.
   * 
   * @throws IllegalArgumentException If an argument is {@code null} or improperly structured.
   * 
   */
  public static boolean matchNoWaitArmedLogs
  (final List<Map<Double, Boolean>> predictedNwaLogs, final List<Map<Double, Boolean>> actualNwaLogs)
  {
    if (predictedNwaLogs == null || actualNwaLogs == null)
      throw new IllegalArgumentException ();
    boolean retVal = true;
    if (predictedNwaLogs.size () != actualNwaLogs.size ())
      retVal = false;
    else
      for (int i = 0; i < predictedNwaLogs.size (); i++)
      {
        if (predictedNwaLogs.get (i).size () != 1 || actualNwaLogs.get (i).size () != 1)
          throw new IllegalArgumentException ();
        if (! predictedNwaLogs.get (i).keySet ().iterator ().next ().equals (actualNwaLogs.get (i).keySet ().iterator ().next ()))
        {
          retVal = false;
          break;
        }
        if (! predictedNwaLogs.get (i).values ().iterator ().next ().equals (actualNwaLogs.get (i).values ().iterator ().next ()))
        {
          retVal = false;
          break;
        }
      }
    if (! retVal)
    {
      System.err.println ("NoWaitArmed Logs mismatch!");
      System.err.println ("  Predicted: " + predictedNwaLogs + ".");
      System.err.println ("  Actual   : " + actualNwaLogs + ".");
    }
    return retVal;
  }
  
}
