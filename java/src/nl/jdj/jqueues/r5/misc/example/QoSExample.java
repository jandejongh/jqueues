package nl.jdj.jqueues.r5.misc.example;

import java.util.ArrayList;
import java.util.List;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.qos.HOL;
import nl.jdj.jsimulation.r5.DefaultSimEvent;
import nl.jdj.jsimulation.r5.DefaultSimEventList;
import nl.jdj.jsimulation.r5.SimEvent;
import nl.jdj.jsimulation.r5.SimEventAction;
import nl.jdj.jsimulation.r5.SimEventList;

/** Example code for the <code>qos</code> package.
 * 
 */
public final class QoSExample
{
  
  /** Prevents instantiation.
   * 
   */
  private QoSExample ()
  {
  }
  
  /** Main method.
   * 
   * @param args The command-line arguments (ignored).
   * 
   */
  public static void main (String[] args)
  {
    System.out.println ("=== EXAMPLE PROGRAM FOR THE qos PACKAGE ===");
    System.out.println ("-> Creating jobs...");
    final List<SimJob> jobList = new ArrayList<>  ();
    // Execution order should be 1, 7, 3, 2, 4, 5, 6.
    jobList.add (new DefaultExampleSimJobQoS<> (true, 1, 10.0, Double.class, Double.POSITIVE_INFINITY));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 2, 1.0, Double.class, 0.0));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 3, 1.0, Double.class, -1.0));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 4, 1.0, Double.class, 5.0));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 5, 1.0, Double.class, 5.0));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 6, 1.0, Double.class, 5.0));
    jobList.add (new DefaultExampleSimJobQoS<> (true, 7, 1.0, Double.class, Double.NEGATIVE_INFINITY));
    System.out.println ("-> Creating event list...");
    final SimEventList<SimEvent> el = new DefaultSimEventList<> (SimEvent.class);
    System.out.println ("-> Creating HOL queue...");
    final SimQueue holQueue = new HOL (el, Double.class, Double.POSITIVE_INFINITY);
    System.out.println ("-> Submitting jobs to HOL queue...");
    for (int i = 0; i < jobList.size (); i++)
    {
      final SimJob j = jobList.get (i);
      final double arrTime = i + 1;
      el.add (new DefaultSimEvent ("ARRIVAL_" + i + 1, i + 1, null, (SimEventAction) (final SimEvent event) ->
      {
        holQueue.arrive (arrTime, j);
      }));
    }
    System.out.println ("-> Executing event list...");
    el.run ();
    System.out.println ("-> Resetting event list...");
    el.reset ();
    System.out.println ("=== FINISHED ===");
  }
  
}