package nl.jdj.jqueues.r5.misc.book.book_04_event_list;

import nl.jdj.jsimulation.r5.DefaultSimEventList_IOEL;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_160_SimultaneousEvents_IOEL
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
    final SimEventList el = new DefaultSimEventList_IOEL ();
    for (int i = 1; i <= 10; i++)
      el.schedule (0, new IndexedSimEventAction (i), "Event " + i);
    el.print ();
    el.run ();
  }
  
}
