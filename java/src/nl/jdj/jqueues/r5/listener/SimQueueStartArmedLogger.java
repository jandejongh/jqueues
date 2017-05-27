package nl.jdj.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A {@link SimQueueListener} that logs {@code StartArmed} in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyNewStartArmed
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public class SimQueueStartArmedLogger
extends DefaultSimQueueListener
{
  
  private final List<Map<Double, Boolean>> staLog = new ArrayList<> ();
  
  /** Returns the {@code StartArmed} log (unmodifiable).
   * 
   * <p>
   * The log is constructed as a list of non-null and unique single-entry maps,
   * corresponding to a reported state change in {@code StartArmed}.
   * The entry holds the time as its key and the new {@code StartArmed} as its value.
   * 
   * <p>
   * The {@code StartArmed} log is automatically cleared upon a (reported) queue reset.
   * 
   * @return The {@code StartArmed} log (unmodifiable).
   * 
   * @see SimQueueListener#notifyResetEntity
   * 
   */
  public final List<Map<Double, Boolean>> getStartArmedLog ()
  {
    return Collections.unmodifiableList (this.staLog);
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimQueueListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    this.staLog.clear ();
  }

  @Override
  public void notifyNewStartArmed (final double time, final SimQueue queue, final boolean startArmed)
  {
    this.staLog.add (Collections.singletonMap (time, startArmed));
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MATCH StartArmed LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Compares two {@code StartArmed} logs (predicted and actual), and reports inequalities to {@link System#err}.
   * 
   * @param predictedStaLogs The predicted {@code StartArmed} logs.
   * @param actualStaLogs    The actual {@code StartArmed} logs.
   * @param accuracy         The allowed deviation in key values (times-of-change).
   * @param testString       An optional String identifying the test in place.
   * 
   * @return {@code true} if the two logs match.
   * 
   * @throws IllegalArgumentException If an argument is {@code null} or improperly structured.
   * 
   */
  public static boolean matchStartArmedLogs
  (final List<Map<Double, Boolean>> predictedStaLogs,
   final List<Map<Double, Boolean>> actualStaLogs,
   final double accuracy,
   final String testString)
  {
    if (predictedStaLogs == null || actualStaLogs == null)
      throw new IllegalArgumentException ();
    boolean retVal = true;
    if (predictedStaLogs.size () != actualStaLogs.size ())
      retVal = false;
    else
      for (int i = 0; i < predictedStaLogs.size (); i++)
      {
        if (predictedStaLogs.get (i).size () != 1 || actualStaLogs.get (i).size () != 1)
          throw new IllegalArgumentException ();
        if (Math.abs (predictedStaLogs.get (i).keySet ().iterator ().next ()
                    - actualStaLogs.get (i).keySet ().iterator ().next ())
                    > accuracy)
        {
          retVal = false;
          break;
        }
        if (! predictedStaLogs.get (i).values ().iterator ().next ().equals (actualStaLogs.get (i).values ().iterator ().next ()))
        {
          retVal = false;
          break;
        }
      }
    if (! retVal)
    {
      System.err.println ("StartArmed Logs mismatch!");
      if (testString != null)
      {
        System.err.println ("  Test:");
        System.err.println (testString);
      }
      System.err.println ("  Predicted: " + predictedStaLogs + ".");
      System.err.println ("  Actual   : " + actualStaLogs + ".");
    }
    return retVal;
  }
  
}
