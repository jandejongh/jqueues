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

/** A {@link SimQueueListener} that logs server-access-credits availability in between resets.
 * 
 * @see SimQueue#resetEntity
 * @see SimQueue#setServerAccessCredits
 * @see SimQueueListener#notifyResetEntity
 * @see SimQueueListener#notifyOutOfServerAccessCredits
 * @see SimQueueListener#notifyRegainedServerAccessCredits
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

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MATCH ServerAccessCredits AVAILABILITY LOGS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  /** Compares two server-access-credits availability logs (predicted and actual), and reports inequalities to {@link System#err}.
   * 
   * @param predictedSacLogs The predicted server-access-credits availability logs.
   * @param actualSacLogs    The actual server-access-credits availability logs.
   * @param accuracy         The allowed deviation in key values (times-of-change).
   * @param testString       An optional String identifying the test in place.
   * 
   * @return {@code true} if the two logs match.
   * 
   * @throws IllegalArgumentException If an argument is {@code null} or improperly structured.
   * 
   */
  public static boolean matchServerAccessCreditsAvailabilityLogs
  (final List<Map<Double, Boolean>> predictedSacLogs,
   final List<Map<Double, Boolean>> actualSacLogs,
   final double accuracy,
   final String testString)
  {
    if (predictedSacLogs == null || actualSacLogs == null)
      throw new IllegalArgumentException ();
    boolean retVal = true;
    if (predictedSacLogs.size () != actualSacLogs.size ())
      retVal = false;
    else
      for (int i = 0; i < predictedSacLogs.size (); i++)
      {
        if (predictedSacLogs.get (i).size () != 1 || actualSacLogs.get (i).size () != 1)
          throw new IllegalArgumentException ();
        if (Math.abs (predictedSacLogs.get (i).keySet ().iterator ().next ()
                    - actualSacLogs.get (i).keySet ().iterator ().next ())
                    > accuracy)
        {
          retVal = false;
          break;
        }
        if (! predictedSacLogs.get (i).values ().iterator ().next ().equals (actualSacLogs.get (i).values ().iterator ().next ()))
        {
          retVal = false;
          break;
        }
      }
    if (! retVal)
    {
      System.err.println ("Server-Access Credits Availability Logs mismatch!");
      if (testString != null)
      {
        System.err.println ("  Test:");
        System.err.println (testString);
      }
      System.err.println ("  Predicted: " + predictedSacLogs + ".");
      System.err.println ("  Actual   : " + actualSacLogs + ".");
    }
    return retVal;
  }
  
}
