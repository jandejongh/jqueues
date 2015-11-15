package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class Ex00140_ScheduleSimEventsWhileRunning
{
  
  public static void main (final String[] args)
  {
    
    final SimEventList el = new DefaultSimEventList ()
    {
      @Override
      public final String toString ()
      {
        return "The Event List";
      } 
    };
    
    final SimEventAction schedulingAction = new SimEventAction ()
    {
      private int counter = 0;
      @Override
      public final void action (final SimEvent event)
      {
          System.out.println ("Event=" + event + ", time=" + event.getTime () + ".");
          counter++;
          if (counter < 10)
            // Schedule 1 second from now.
            // Use utility method on SimEventList.
            el.schedule (event.getTime () + 1, this);
          else if (counter == 10)
          {
            // Schedule now.
            el.schedule (event.getTime (), this);
            System.out.println ("Scheduled event now.");
          }
          else
          {
            // Schedule 1 second in the past -> throws exception.
            el.schedule (event.getTime () - 1, this);
            // Never reached.
            System.out.println ("Scheduled event in the past.");
          }
      }
      @Override
      public final String toString ()
      {
        return "Scheduling Action";
      }
    };
    
    el.schedule (0, schedulingAction);
    el.print ();
    el.run ();
    el.print ();
    
  }
  
}
