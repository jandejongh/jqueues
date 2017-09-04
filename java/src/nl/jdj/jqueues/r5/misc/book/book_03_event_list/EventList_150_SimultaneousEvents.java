package nl.jdj.jqueues.r5.misc.book.book_03_event_list;

import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_150_SimultaneousEvents
{
  
  private static class IndexedSimEventAction
  implements SimEventAction
  {
    
    final int index;
    
    public IndexedSimEventAction (final int index)
    {
      this.index = index;
    }
    
    @Override
    public void action (SimEvent event)
    {
      System.out.println ("Hello, I am action number " + this.index + "!");
    }

    @Override
    public String toString ()
    {
      return "Action " + index;
    }
    
  }
  
  public static void main (final String[] args)
  {
    final SimEventList el = new DefaultSimEventList ();
    for (int i = 1; i <= 10; i++)
      el.schedule (0, new IndexedSimEventAction (i), "Event " + i);
    el.print ();
    el.run ();
  }
  
}
