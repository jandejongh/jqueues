package nl.jdj.jqueues.r4.book;

import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

final class CreatingSimEventActions_AnonymousInnerClasses
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new SimEventList ();
    final SimEvent e =
      new SimEvent ("My First Real Event", 5.0, null, new SimEventAction ()
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
