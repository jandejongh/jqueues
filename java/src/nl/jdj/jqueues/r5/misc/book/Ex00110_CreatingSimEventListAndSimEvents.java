package nl.jdj.jqueues.r5.misc.book;

import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventList;

final class Ex00110_CreatingSimEventListAndSimEvents
{
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final SimEvent e1 = new DefaultSimEvent (5.0);
    final SimEvent e2 = new DefaultSimEvent (10.0);
    el.add (e1);
    el.add (e2);
    el.print ();
  }
  
}
