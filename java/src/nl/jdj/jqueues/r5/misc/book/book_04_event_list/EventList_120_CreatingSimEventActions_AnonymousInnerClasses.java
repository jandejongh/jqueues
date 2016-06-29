package nl.jdj.jqueues.r5.misc.book.book_04_event_list;

import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_120_CreatingSimEventActions_AnonymousInnerClasses
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final SimEvent e =
      new DefaultSimEvent ("My First Real Event", 5.0, null, new SimEventAction ()
      {
        @Override
        public final void action (final SimEvent event)
        {
          System.out.println ("Event=" + event + ", time=" + event.getTime () + ".");
        }
        @Override
        public String toString ()
        {
          return "My First Action";
        }
      });
    el.add (e);
    el.print ();
    el.run ();
    el.print ();
  }
  
}
