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

/** A {@link SimQueueListener} that logs queue-access vacations in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueue#setQueueAccessVacation
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyStartQueueAccessVacation
 * @see SimQueueListener#notifyStopQueueAccessVacation
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
   * @param testString       An optional String identifying the test in place.
   * 
   * @return {@code true} if the two logs match.
   * 
   * @throws IllegalArgumentException If an argument is {@code null} or improperly structured.
   * 
   */
  public static boolean matchQueueAccessVacationLogs
  (final List<Map<Double, Boolean>> predictedQavLogs,
   final List<Map<Double, Boolean>> actualQavLogs,
   final double accuracy,
   final String testString)
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
      if (testString != null)
      {
        System.err.println ("  Test:");
        System.err.println (testString);
      }
      System.err.println ("  Predicted: " + predictedQavLogs + ".");
      System.err.println ("  Actual   : " + actualQavLogs + ".");
    }
    return retVal;
  }
  
}
