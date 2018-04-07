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

import java.util.List;
import java.util.Map;
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.SimEntityEvent;
import org.javades.jqueues.r5.entity.SimEntityListener;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;

/** A {@link SimEntityListener} logging events on <code>System.out</code>.
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
public class StdOutSimEntityListener
implements SimEntityListener
{

  /** Returns the header used in the notifications.
   * 
   * @return {@code this.getClass ().getSimpleName ()}.
   * 
   */
  protected String getHeaderString ()
  {
    return this.getClass ().getSimpleName ();
  }
  
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("entity=" + entity + ": RESET.");
  }

  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
    System.out.print (getHeaderString () + " ");
    System.out.println ("t=" + time + ", entity=" + entity + ": UPDATE.");
  }

  @Override
  public void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, SimEntityEvent>> notifications)
  {
    System.out.print (getHeaderString () + " ");
    System.out.print ("t=" + time + ", entity=" + entity + ": STATE CHANGED:");
    if (notifications == null)
      System.out.println (" === null ====");
    else
    {
      System.out.println ("");
      for (final Map<SimEntitySimpleEventType.Member, SimEntityEvent> notification : notifications)
        if (notification == null)
          System.out.println ("  => null");
        else
          System.out.println ("  => " + notification.keySet ().iterator ().next ()
                                      + " [" + notification.values ().iterator ().next () + "]");
    }
  }

}
