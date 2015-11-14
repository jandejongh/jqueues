package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventList;

final class Ex00110_CreatingSimEventListAndSimEvents
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new SimEventList ();
    final SimEvent e1 = new SimEvent (5.0);
    final SimEvent e2 = new SimEvent (10.0);
    el.add (e1);
    el.add (e2);
    el.print ();
  }
  
}
