package nl.jdj.jqueues.r4.swing.example;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import nl.jdj.jqueues.r4.AbstractSimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.composite.BlackProbabilisticFeedbackSimQueue;
import nl.jdj.jqueues.r4.composite.BlackTandemSimQueue;
import nl.jdj.jqueues.r4.nonpreemptive.FCFS;
import nl.jdj.jqueues.r4.nonpreemptive.IS;
import nl.jdj.jqueues.r4.nonpreemptive.RANDOM;
import nl.jdj.jqueues.r4.swing.JBlackSimQueueNetwork;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
import nl.jdj.jsimulation.r4.swing.JSimEventList;

/**
 *
 */
public final class Main
{
  
  /** Inhibits instantiation.
   * 
   */
  private Main ()
  {
  }
  
  /** Starts the main program.
   * 
   * @param args The command line arguments.
   * 
   */
  public static void main (final String[] args)
  {
    SwingUtilities.invokeLater (
      new Runnable ()
      {

        public void run ()
        {
          final JFrame frame = new JFrame ("JSimQueue and JSimEventList demonstration.");
          frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
          final JPanel topPanel = new JPanel ();
          topPanel.setLayout (new BoxLayout (topPanel, BoxLayout.PAGE_AXIS));
          frame.getContentPane ().add (topPanel);
          final SimEventList eventList = new SimEventList (SimEvent.class);
          final JSimEventList jSimEventList = new JSimEventList (eventList);
          final SimQueue is1 = new IS (eventList);
          final SimQueue fcfs1 = new FCFS (eventList);
          final SimQueue random1 = new RANDOM (eventList);
          final Set<SimQueue> set = new LinkedHashSet<> ();
          set.add (is1);
          set.add (fcfs1);
          set.add (random1);
          // final SimQueue queue = new BlackProbabilisticFeedbackSimQueue (eventList, new IS (eventList), 0.5, null, null);
          final SimQueue queue = new BlackTandemSimQueue (eventList, set, null);
          final JBlackSimQueueNetwork jQueue = new JBlackSimQueueNetwork (eventList, queue);
          topPanel.add (jQueue);
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
          buttonPanel.add (new JButton (new AbstractAction ("Arr[Now]")
          {
            @Override
            public final void actionPerformed (ActionEvent ae)
            {
              queue.arrive (new AbstractSimJob ()
              {
                @Override
                public double getServiceTime (SimQueue queue) throws IllegalArgumentException
                {
                  return 10.0;
                }
              }, eventList.getTime ());
              jSimEventList.eventListChangedNotification ();              
            }
          }));
          buttonPanel.add (new JButton (new AbstractAction ("Arr[Sched]")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.scheduleNow (new SimEventAction ()
              {
                @Override
                public void action (SimEvent event)
                {
                  queue.arrive (new AbstractSimJob ()
                  {
                    @Override
                    public double getServiceTime (SimQueue queue) throws IllegalArgumentException
                    {
                      return 10.0;
                    }
                  }, eventList.getTime ());
                } 
              });
              jSimEventList.eventListChangedNotification ();
            }
          }));
          buttonPanel.add (new JButton (new AbstractAction ("Step")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.runSingleStep ();
              jSimEventList.eventListChangedNotification ();
            }
          }));
          buttonPanel.add (new JButton (new AbstractAction ("Run")
          {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
              eventList.run ();
              // jSimEventList.eventListChangedNotification ();
            }
          }));
          topPanel.add (buttonPanel);
          topPanel.add (jSimEventList);
          frame.setMinimumSize (new Dimension (400, 400));
          frame.pack ();
          frame.setLocationRelativeTo (null);
          frame.setVisible (true);
        }
      });
  }
  
}