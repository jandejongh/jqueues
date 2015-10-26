package nl.jdj.jqueues.r4.processorsharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.TestJob;
import nl.jdj.jsimulation.r4.SimEvent;
import nl.jdj.jsimulation.r4.SimEventAction;
import nl.jdj.jsimulation.r4.SimEventList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ProcessorSharingTest
{
  
  public ProcessorSharingTest ()
  {
  }
  
  @BeforeClass
  public static void setUpClass ()
  {
  }
  
  @AfterClass
  public static void tearDownClass ()
  {
  }
  
  @Before
  public void setUp ()
  {
  }
  
  @After
  public void tearDown ()
  {
  }

  public static List<TestJob> scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    final List<TestJob> jobList = new ArrayList<>  ();
    for (int i = 1; i <= n; i++)
    {
      final TestJob j = new TestJob (reported, i);
      jobList.add (j);
      final double arrTime = i;
      eventList.add (new SimEvent ("ARRIVAL_" + i, arrTime, null, new SimEventAction ()
      {
        @Override
        public void action (final SimEvent event)
        {
          queue.arrive (j, arrTime);
        }
      }));
    }
    return jobList;
  }
  
  private final void queuePredictorPS (final Set<TestJob> jobs,
    final SimQueue queue,
    final TreeMap<Double, Boolean> queueAccessVacation)
  {
    if (queue == null || ! (queue instanceof PS))
      throw new IllegalArgumentException ();
    if (jobs == null)
      return;
    for (TestJob j : jobs)
    {
      j.predicted = true;
      j.predictedArrived = true;
      j.predictedArrivalTime = j.scheduledArrivalTime;
      j.predictedStarted = true;
      j.predictedStartTime = j.predictedArrivalTime;
      j.predictedDropped = false;
      j.predictedDropTime = Double.NaN;
      j.predictedRevoked = false;
      j.predictedRevocationTime = Double.NaN;
      j.predictedDeparted = true;
      j.predictedDepartureTime = Double.NaN;
    }
    final Set<TestJob> jobsCopy = new HashSet<> (jobs);
    if (queueAccessVacation != null)
    {
      final Set<TestJob> jobsToRemove = new HashSet ();
      for (final TestJob j : jobsCopy)
      {
        final double predictedArrivalTime = j.predictedArrivalTime;
        final Map.Entry<Double, Boolean> entry = queueAccessVacation.floorEntry (predictedArrivalTime);
        if (entry != null && entry.getValue ())
        {
          j.predictedStarted = false;
          j.predictedStartTime = Double.NaN;
          j.predictedDropped = true;
          j.predictedDropTime = j.predictedArrivalTime;
          j.predictedDeparted = false;
          j.predictedDepartureTime = Double.NaN;
          jobsToRemove.add (j);
        }
      }
      jobsCopy.removeAll (jobsToRemove);
    }
    final TreeMap<Double, Set<TestJob>> jPresent = new TreeMap ();      
    for (final TestJob j : jobsCopy)
    {
      if (! jPresent.containsKey (j.predictedArrivalTime))
        jPresent.put (j.predictedArrivalTime, new HashSet<TestJob> ());
      jPresent.get (j.predictedArrivalTime).add (j);
    }
    Set<TestJob> jPresentPrevious = null;
    for (final double tArr : jPresent.keySet ())
    {
      if (jPresentPrevious == null)
        jPresentPrevious = jPresent.get (tArr);
      else
      {
        jPresent.get (tArr).addAll (jPresentPrevious);
        jPresentPrevious = jPresent.get (tArr);
      }
    }
    if (jPresent.containsKey (Double.NEGATIVE_INFINITY))
    {
      final Set<TestJob> jobsAtNegativeInfinity = jPresent.get (Double.NEGATIVE_INFINITY);
      for (final TestJob j : jobsAtNegativeInfinity)
        if (j.getServiceTime (queue) != Double.POSITIVE_INFINITY)
          j.predictedDepartureTime = Double.NEGATIVE_INFINITY;
        else
          j.predictedDepartureTime = Double.POSITIVE_INFINITY;          
      jobsCopy.removeAll (jobsAtNegativeInfinity);
      jPresent.remove (Double.NEGATIVE_INFINITY);
    }
    if (jPresent.containsKey (Double.POSITIVE_INFINITY))
    {
      final Set<TestJob> jobsAtPositiveInfinity = jPresent.get (Double.POSITIVE_INFINITY);
      for (final TestJob j : jobsAtPositiveInfinity)
          j.predictedDepartureTime = Double.POSITIVE_INFINITY;
      jobsCopy.removeAll (jobsAtPositiveInfinity);
      jPresent.remove (Double.POSITIVE_INFINITY);
    }
    final Map<TestJob, Double> rS = new HashMap<> ();
    for (final TestJob j : jobsCopy)
      rS.put (j, j.getServiceTime (queue));
    while (! jobsCopy.isEmpty ())
    {
      final double time = jPresent.firstKey ();
      final Set<TestJob> jobSet = jPresent.get (time);
      if (! jobSet.isEmpty ())
      {        
        final int jobSetSize = jobSet.size ();
        double nextTime = jPresent.higherKey (time) != null ? jPresent.higherKey (time) : Double.POSITIVE_INFINITY;
        final TreeMap<Double, TestJob> departureTimes = new TreeMap<> ();
        for (final TestJob j : jobSet)
          departureTimes.put (time + (rS.get (j) * jobSet.size ()), j);
        if (departureTimes.firstKey () <= nextTime)
        {
          nextTime = departureTimes.firstKey ();
          final TestJob jDepart = departureTimes.get (nextTime);
          for (final Set<TestJob> stj : jPresent.values ())
            stj.remove (jDepart);
          jobsCopy.remove (jDepart);
          jDepart.predictedDepartureTime = nextTime;
          jobSet.remove (jDepart);
          if (! jPresent.containsKey (nextTime))
            jPresent.put (nextTime, new HashSet<> (jobSet));
        }
        for (final TestJob j : jobSet)
          rS.put (j, rS.get (j) - ((nextTime - time) / jobSetSize));
      }
      jPresent.pollFirstEntry ();
    }
  }
  
  private void scheduleQueueAccessVacation (SimEventList el, final SimQueue queue, double startTime, final double duration)
  {
    el.add (new SimEvent (startTime, null, new SimEventAction ()
    {
      @Override
      public void action (SimEvent event)
      {
        queue.startQueueAccessVacation (duration);
      }
    }));
  }
  
  private final double ACCURACY = 1.0E-12;
  
  /**
   * Test of PS.
   * 
   */
  @Test
  public void testPS ()
  {
    System.out.println ("==");
    System.out.println ("PS");
    System.out.println ("==");
    final SimEventList<SimEvent> el = new SimEventList<> (SimEvent.class);
    final PS queue = new PS (el);
    queue.registerStdOutSimQueueVacationListener ();
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob> jobs = scheduleJobArrivals (true, 40, el, queue);
      // Schedule vacation from 1.5 to 5.5.
      // Jobs 2, 3, 4, and 5 should be dropped.
      scheduleQueueAccessVacation (el, queue, 1.5, 4.0);
      final TreeMap<Double, Boolean> queueAccessVacationMap = new TreeMap<> ();
      queueAccessVacationMap.put (1.5, true);
      queueAccessVacationMap.put (5.5, false);
      queuePredictorPS (new HashSet<> (jobs), queue, queueAccessVacationMap);
      el.run ();
      assert el.isEmpty ();
      for (TestJob j : jobs)
        j.testPrediction (ACCURACY);
      // Test reset on the fly...
      el.reset ();
    }
  }

}
