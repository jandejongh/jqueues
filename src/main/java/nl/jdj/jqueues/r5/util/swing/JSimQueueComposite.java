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
package nl.jdj.jqueues.r5.util.swing;

import java.awt.Dimension;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jsimulation.r5.SimEventList;

/** A Swing component for a {@link SimQueueComposite}.
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
public class JSimQueueComposite
extends JComponent
{
  
  public JSimQueueComposite (final SimEventList eventList, final SimQueue queue)
  {
    super ();
    setQueue (eventList, queue);
  }
  
  public final void setQueue (final SimEventList eventList, final SimQueue queue)
  {
    if (eventList == null || queue == null)
      throw new IllegalArgumentException ();
    removeAll ();
    setLayout (new BoxLayout (this, BoxLayout.LINE_AXIS));
    add (new JSimQueue (eventList, queue));
    if (queue instanceof SimQueueComposite)
    {
      for (SimQueue subQueue : (Set<SimQueue>) ((SimQueueComposite) queue).getQueues ())
        add (new JSimQueue (eventList, subQueue));
      final int components = 1 + ((SimQueueComposite) queue).getQueues ().size ();
      setMinimumSize (new Dimension (components * 120, 200));
      setPreferredSize (new Dimension (components * 120, 200));
    }
    else
    {
      setMinimumSize (new Dimension (120, 200));      
      setPreferredSize (new Dimension (120, 200));
    }
    invalidate ();
    repaint ();
  }

}
