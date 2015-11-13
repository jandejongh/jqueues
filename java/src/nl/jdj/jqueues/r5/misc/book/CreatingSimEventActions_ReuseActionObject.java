package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;

final class CreatingSimEventActions_ReuseActionObject
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new SimEventList ()
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
      final SimEvent e = new SimEvent ("Our Event", (double) i, null, action);
      el.add (e);
    }
    el.print ();
    el.run ();
    el.print ();
  }
  
}
