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
package org.javades.jqueues.r5.misc.example;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.javades.jqueues.r5.entity.jq.job.AbstractSimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.composite.ctandem2.CTandem2;
import org.javades.jqueues.r5.entity.jq.queue.nonpreemptive.FCFS;
import org.javades.jqueues.r5.util.swing.JSimQueueComposite;
import org.javades.jqueues.r5.util.swing.JSimQueueCreationDialog;
import org.javades.jsimulation.r5.DefaultSimEvent;
import org.javades.jsimulation.r5.DefaultSimEventList;
import org.javades.jsimulation.r5.SimEvent;
import org.javades.jsimulation.r5.SimEventAction;
import org.javades.jsimulation.r5.SimEventList;
import org.javades.jsimulation.r5.SimEventListListener;
import org.javades.jsimulation.r5.swing.JSimEventList;

/** Example code for <code>util.swing</code>.
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
public final class SwingExample
{
  
  /** Inhibits instantiation.
   * 
   */
  private SwingExample ()
  {
  }
  
  /** Starts the main program.
   * 
   * @param args The command line arguments.
   * 
   */
  public static void main (final String[] args)
  {
    SwingUtilities.invokeLater (new Runnable ()
      {
        
        private JFrame frame;
        
        private SimEventList eventList;

        private SimQueue queue;
        
        private JSimQueueComposite jQueue;
          
        private SimQueue getQueue ()
        {
          return this.queue;
        }
        
        private void setQueue (final SimQueue queue)
        {
          //final boolean isQueueAccessVacation = queue.isQueueAccessVacation ();
          //final int serverAccessCredits = queue.getServerAccessCredits ();
          //this.eventList.reset ();
          this.queue = queue;
          //if (isQueueAccessVacation)
          //  this.queue.startQueueAccessVacation ();
          //else
          //  this.queue.stopQueueAccessVacation ();
          //this.queue.setServerAccessCredits (serverAccessCredits);
          jQueue.setQueue (this.eventList, this.queue);
          frame.invalidate ();
          // frame.repaint ();
          frame.pack ();
        }
        
        @Override
        public void run ()
        {
          frame = new JFrame ("JSimQueue and JSimEventList demonstration.");
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          final JPanel topPanel = new JPanel ();
          topPanel.setLayout (new BoxLayout (topPanel, BoxLayout.PAGE_AXIS));
          frame.getContentPane ().add (topPanel);
          this.eventList = new DefaultSimEventList (DefaultSimEvent.class);
          final JLabel timeLabel = new JLabel ("Time: " + this.eventList.getTime ());
          this.eventList.addListener (new SimEventListListener ()
          {
            @Override
            public final void notifyEventListReset (final SimEventList eventList)
            {
              timeLabel.setText ("Time: " + eventList.getTime ());
            }
            @Override
            public final void notifyEventListUpdate (final SimEventList eventList, final double time)
            {
              timeLabel.setText ("Time: " + time);
            }
            @Override
            public final void notifyEventListEmpty (final SimEventList eventList, final double time)
            {
              /* EMPTY */
            }
          });
          topPanel.add (Box.createRigidArea (new Dimension (0, 10)));
          topPanel.add (timeLabel);
          final JSimEventList jSimEventList = new JSimEventList (this.eventList);
          // final SimQueue is1 = new IS (this.eventList);
          final SimQueue fcfs1 = new FCFS (this.eventList);
          final SimQueue fcfs2 = new FCFS (this.eventList);
          // final SimQueue random1 = new RANDOM (this.eventList);
          // final Set<SimQueue> set = new LinkedHashSet<> ();
          // set.add (is1);
          // set.add (fcfs1);
          // set.add (random1);
          // this.queue = new Tandem (this.eventList, set, null);
          this.queue = new CTandem2 (this.eventList, fcfs1, fcfs2, null);
          this.jQueue = new JSimQueueComposite (this.eventList, this.queue);
          topPanel.add (Box.createRigidArea (new Dimension (0, 10)));
          topPanel.add (this.jQueue);
          final JPanel buttonPanel = new JPanel ();
          buttonPanel.setLayout (new BoxLayout (buttonPanel, BoxLayout.LINE_AXIS));
          buttonPanel.add (new JButton (new AbstractAction ("Reset")
          {
            @Override
            public final void actionPerformed (ActionEvent ae)
            {
              eventList.reset ();
            }
          }));
          buttonPanel.add (Box.createRigidArea (new Dimension (10, 0)));
          buttonPanel.add (new JButton (new AbstractAction ("New Queue")
          {
            @Override
            public final void actionPerformed (ActionEvent ae)
            {
              final JSimQueueCreationDialog dialog = new JSimQueueCreationDialog (frame, eventList, getQueue ());
              dialog.setVisible (true);
              if (dialog.getCreatedQueue () != null && dialog.getCreatedQueue () != getQueue ())
                setQueue (dialog.getCreatedQueue ());
              dialog.dispose ();
            }
          }));
          buttonPanel.add (Box.createRigidArea (new Dimension (10, 0)));
          buttonPanel.add (new JButton (new AbstractAction ("Arr[Now]")
          {
            @Override
            public final void actionPerformed (ActionEvent ae)
            {
              getQueue ().arrive (eventList.getTime (), new AbstractSimJob (null, null)
              {
                @Override
                public double getServiceTime (SimQueue queue) throws IllegalArgumentException
                {
                  return 10.0;
                }
              });
              jSimEventList.eventListChangedNotification ();              
            }
          }));
          buttonPanel.add (Box.createRigidArea (new Dimension (10, 0)));
          buttonPanel.add (new JButton (new AbstractAction ("Arr[Sched]")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.scheduleNow ((SimEventAction) (SimEvent event) ->
              {
                getQueue ().arrive (eventList.getTime (), new AbstractSimJob (null, null)
                {
                  @Override
                  public double getServiceTime (SimQueue queue1) throws IllegalArgumentException
                  {
                    return 10.0;
                  }
                }); 
              });
              jSimEventList.eventListChangedNotification ();
            }
          }));
          buttonPanel.add (Box.createRigidArea (new Dimension (10, 0)));
          buttonPanel.add (new JButton (new AbstractAction ("Step")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.runSingleStep ();
              jSimEventList.eventListChangedNotification ();
            }
          }));
          buttonPanel.add (Box.createRigidArea (new Dimension (10, 0)));
          buttonPanel.add (new JButton (new AbstractAction ("Run")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.run ();
              // jSimEventList.eventListChangedNotification ();
            }
          }));
          buttonPanel.setAlignmentY (0.5f);
          topPanel.add (Box.createRigidArea (new Dimension (0, 10)));
          topPanel.add (buttonPanel);
          topPanel.add (Box.createRigidArea (new Dimension (0, 10)));
          topPanel.add (jSimEventList);
          topPanel.add (Box.createRigidArea (new Dimension (0, 10)));
          frame.pack ();
          frame.setLocationRelativeTo (null);
          frame.setVisible (true);
        }
      });
  }
  
}
