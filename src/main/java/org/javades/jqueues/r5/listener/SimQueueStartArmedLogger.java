/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueListener;
import org.javades.jqueues.r5.util.predictor.SimQueuePredictor;

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
   * <p>
   * In case of a mismatch,
   * the method re-attempts with a compacted version (dealing with simultaneous notifications)
   * of the <i>predicted</i> logs,
   * because some {@link SimQueuePredictor} implementations
   * cannot always provide atomic {@code StartArmed} notifications.
   * A warning on {@code System.err} is given in case
   * erroneous notifications are found in the predicted logs.
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
    if ((! retVal) && compactStartArmedLogs (predictedStaLogs))
    {
      System.err.println ("Predictor StartArmed Logs Compaction; check predictor!");
      return matchStartArmedLogs (predictedStaLogs, actualStaLogs, accuracy, testString);
    }
    else if (! retVal)
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
  
  private static boolean compactStartArmedLogs (final List<Map<Double, Boolean>> staLogs)
  {
    if (staLogs == null)
      throw new IllegalArgumentException ();
    if (staLogs.size () == 1)
      return false;
    boolean changed = false;
    boolean changing = true;
    while (changing)
    {
      changing = false;
      for (int i = 0; i < staLogs.size () - 1; i++)
      {
        final double time_i = staLogs.get (i).keySet ().iterator ().next ();
        final double time_ip1 = staLogs.get (i+1).keySet ().iterator ().next ();
        if (time_i == time_ip1)
        {
          final boolean sta_i = staLogs.get (i).get (time_i);
          final boolean sta_ip1 = staLogs.get (i+1).get (time_ip1);
          if (sta_ip1 == sta_i)
            staLogs.remove (i + 1);
          else
          {
            staLogs.remove (i+1);
            staLogs.remove (i);
          }
          changed = true;
          changing = true;
          break;
        }
      }
    }
    return changed;
  }
  
}
