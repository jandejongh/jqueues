package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class Ex00120_CreatingSimEventActions_AnonymousInnerClasses
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
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
