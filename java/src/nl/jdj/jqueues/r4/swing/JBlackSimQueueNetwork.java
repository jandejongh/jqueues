package nl.jdj.jqueues.r4.swing;

import java.awt.Dimension;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.composite.BlackSimQueueNetwork;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class JBlackSimQueueNetwork
extends JComponent
{
  
  public JBlackSimQueueNetwork (final SimEventList eventList, final SimQueue queue)
  {
    super ();
    setQueue (eventList, queue);
  }
  
  public final void setQueue (final SimEventList eventList, final SimQueue queue)
  {
    if (eventList == null || queue == null)
      throw new IllegalArgumentException ();
    removeAll ();
    setLayout (new BoxLayout (this, BoxLayout.LINE_AXIS));
    add (new JSimQueue (eventList, queue));
    if (queue instanceof BlackSimQueueNetwork)
    {
      for (SimQueue subQueue : (Set<SimQueue>) ((BlackSimQueueNetwork) queue).getQueues ())
        add (new JSimQueue (eventList, subQueue));
      final int components = 1 + ((BlackSimQueueNetwork) queue).getQueues ().size ();
      setMinimumSize (new Dimension (components * 120, 200));
      setPreferredSize (new Dimension (components * 120, 200));
    }
    else
    {
      setMinimumSize (new Dimension (120, 200));      
      setPreferredSize (new Dimension (120, 200));
    }
    invalidate ();
    repaint ();
  }

}
