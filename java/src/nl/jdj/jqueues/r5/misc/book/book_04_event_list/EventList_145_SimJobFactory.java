package nl.jdj.jqueues.r5.misc.book.book_04_event_list;

import java.math.BigInteger;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

final class EventList_145_SimJobFactory
{

  interface MySimEvent
  extends SimEvent
  {
    BigInteger getSeqNumber ();
  }
  
  static class DefaultMySimEvent
  extends DefaultSimEvent
  implements MySimEvent
  {

    private static BigInteger
      NEXT_SEQUENCE_NUMBER = BigInteger.ZERO;
    
    private final BigInteger seqNumber;
    
    @Override
    public final BigInteger getSeqNumber ()
    {
      return this.seqNumber;
    }

    public DefaultMySimEvent
      (final String name,
       final double time,
       final SimEventAction action)
    {
      super (name, time, null, action);
      this.seqNumber = NEXT_SEQUENCE_NUMBER;
      NEXT_SEQUENCE_NUMBER =
        NEXT_SEQUENCE_NUMBER.add (BigInteger.ONE);
    }
    
  }
  
  public static void main (final String[] args)
  {
    final SimEventList<MySimEvent> el =
      new DefaultSimEventList (MySimEvent.class);
    el.add (new DefaultMySimEvent
      ("MySimEvent instance", 5.0, null));
    el.setSimEventFactory (
      (final String name, final double time, final SimEventAction eventAction)
        -> new DefaultMySimEvent (name, time, eventAction));
    el.schedule (10.0, (SimEventAction) null);
    el.print ();
  }
  
}
