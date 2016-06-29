package nl.jdj.jqueues.r5.misc.book.book_04_event_list;

import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_130_CreatingSimEventActions_ReuseActionObject
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ()
    {
      @Override
      public final String toString ()
      {
        return "My Renamed Event List";
      } 
    };
    final SimEventAction action = new SimEventAction ()
    {
      @Override
      public final void action (final SimEvent event)
      {
          System.out.println ("Event=" + event + ", time=" + event.getTime () + ".");
      }
      @Override
      public final String toString ()
      {
        return "A Shared Action";
      }
    };
    for (int i = 1; i <= 10; i++)
    {
      final SimEvent e = new DefaultSimEvent ("Our Event", (double) i, null, action);
      el.add (e);
    }
    el.print ();
    el.run ();
    el.print ();
  }
  
}
