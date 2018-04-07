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

import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueListener;

/** A {@link SimQueueListener} logging events on <code>System.out</code>.
 * 
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
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
public class StdOutSimQueueListener<J extends SimJob, Q extends SimQueue>
extends StdOutSimJQListener<J, Q> 
implements SimQueueListener<J, Q>
{

  @Override
  public void notifyStartQueueAccessVacation (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": START OF QUEUE-ACCESS VACATION.");
    }
  }

  @Override
  public void notifyStopQueueAccessVacation (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": END OF QUEUE-ACCESS VACATION.");
    }
  }

  @Override
  public void notifyOutOfServerAccessCredits (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": OUT OF SERVER-ACCESS CREDITS.");
    }
  }

  @Override
  public void notifyRegainedServerAccessCredits (final double time, final Q queue)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": REGAINED SERVER-ACCESS CREDITS.");
    }
  }
  
  @Override
  public void notifyNewStartArmed (final double time, final Q queue, final boolean startArmed)
  {
    if (! isOnlyResetsAndUpdatesAndStateChanges ())
    {
      System.out.print (getHeaderString () + " ");
      System.out.println ("t=" + time + ", queue=" + queue + ": START_ARMED -> " + startArmed + ".");
    }
  }
  
}
