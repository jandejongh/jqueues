package nl.jdj.jqueues.r5.misc.book.book_04_event_list;

import nl.jdj.jsimulation.r5.AbstractSimTimer;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_170_SimTimer
{

  private static class MyTimer
  extends AbstractSimTimer
  {

    @Override
    public final void expireAction
    (final double time)
    {
      System.out.println ("t=" + time + ": Timer expired!");
    }
    
  }
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    final MyTimer myTimer = new MyTimer ();
    // Progress event list until t=10.
    // Note that AbstractSimTimer does not support t=-\infty.
    el.runUntil (10.0, true, true);
    myTimer.schedule (5, el);
    el.print ();
    el.run ();
  }
  
}
