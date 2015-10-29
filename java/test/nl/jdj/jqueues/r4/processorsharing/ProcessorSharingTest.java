package nl.jdj.jqueues.r4.processorsharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.TestJob1;
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

  public static List<TestJob1> scheduleJobArrivals
  (final boolean reported, final int n, final SimEventList eventList, final SimQueue queue)
  {
    final List<TestJob1> jobList = new ArrayList<>  ();
    for (int i = 1; i <= n; i++)
    {
      final TestJob1 j = new TestJob1 (reported, i);
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
  
  private void scheduleQueueAccessVacation
  (final SimEventList el, final SimQueue queue, final double startTime, final double duration)
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
  
  private void scheduleServerAccessCredits
  (final SimEventList el, final SimQueue queue, final double time, final int credits)
  {
    el.add (new SimEvent (time, null, new SimEventAction ()
    {
      @Override
      public void action (SimEvent event)
      {
        queue.setServerAccessCredits (credits);
      }
    }));
  }
  
  private final void queuePredictorPS (final Set<TestJob1> jobs,
    final PS queue,
    final TreeMap<Double, Boolean> queueAccessVacationMap,
    final TreeMap<Double, Integer> serverAccessCreditsMap)
  {
    if (queue == null || ! (queue instanceof PS))
      throw new IllegalArgumentException ();
    if (jobs == null)
      return;
    for (TestJob1 j : jobs)
    {
      j.predicted = true;
      j.predictedArrived = true;
      j.predictedArrivalTime = j.scheduledArrivalTime;
      j.predictedStarted = false;
      j.predictedStartTime = Double.NaN;
      j.predictedDropped = false;
      j.predictedDropTime = Double.NaN;
      j.predictedRevoked = false;
      j.predictedRevocationTime = Double.NaN;
      j.predictedDeparted = false;
      j.predictedDepartureTime = Double.NaN;
    }
    final Set<TestJob1> jobsCopy = new HashSet<> (jobs);
    if (queueAccessVacationMap != null)
    {
      final Set<TestJob1> jobsToRemove = new HashSet ();
      for (final TestJob1 j : jobsCopy)
      {
        final double predictedArrivalTime = j.predictedArrivalTime;
        final Map.Entry<Double, Boolean> entry = queueAccessVacationMap.floorEntry (predictedArrivalTime);
        if (entry != null && entry.getValue ())
        {
          j.predictedDropped = true;
          j.predictedDropTime = j.predictedArrivalTime;
          jobsToRemove.add (j);
        }
      }
      jobsCopy.removeAll (jobsToRemove);
    }
    final TreeMap<Double, Set<TestJob1>> arrivals = new TreeMap<> ();      
    for (final TestJob1 j : jobsCopy)
    {
      if (! arrivals.containsKey (j.predictedArrivalTime))
        arrivals.put (j.predictedArrivalTime, new HashSet<> ());
      arrivals.get (j.predictedArrivalTime).add (j);
    }
    final TreeMap<Double, Integer> serverAccessCreditsMapCopy =
      ((serverAccessCreditsMap != null) ? new TreeMap<> (serverAccessCreditsMap) : new TreeMap<> ());
    final TreeMap<Double, Set<TestJob1>> starts = new TreeMap<> ();
    for (final double arrivalTime : arrivals.keySet ())
    {
      final Set<TestJob1> arrivals_t = arrivals.get (arrivalTime);
      for (TestJob1 j : arrivals_t)
      {
        final Map.Entry<Double, Integer> entry = serverAccessCreditsMapCopy.floorEntry (arrivalTime);
        final int serverAccessCredits = ((entry != null) ? entry.getValue () : Integer.MAX_VALUE);
        if (serverAccessCredits > 0)
        {
          j.predictedStarted = true;
          j.predictedStartTime = arrivalTime;
          if (! starts.containsKey (j.predictedStartTime))
            starts.put (j.predictedStartTime, new LinkedHashSet<> ());
          starts.get (j.predictedStartTime).add (j);
          if (serverAccessCredits < Integer.MAX_VALUE)
            serverAccessCreditsMapCopy.put (arrivalTime, serverAccessCredits - 1);
        }
        else
        {
          Map.Entry<Double, Integer> nextEntry = serverAccessCreditsMapCopy.higherEntry (arrivalTime);
          while (nextEntry != null && nextEntry.getValue () == 0)
            nextEntry = serverAccessCreditsMapCopy.higherEntry (nextEntry.getKey ());
          if (nextEntry != null)
          {
            j.predictedStarted = true;
            j.predictedStartTime = nextEntry.getKey ();
            if (! starts.containsKey (j.predictedStartTime))
              starts.put (j.predictedStartTime, new LinkedHashSet<> ());
            starts.get (j.predictedStartTime).add (j);
            if (nextEntry.getValue () < Integer.MAX_VALUE)
              serverAccessCreditsMapCopy.put (nextEntry.getKey (), nextEntry.getValue () - 1);
          }
          else
            jobsCopy.remove (j);
        }
      }
    }
    final TreeMap<Double, Set<TestJob1>> running = new TreeMap<> ();
    for (final double tStart : starts.keySet ())
    {
      final boolean runningIsEmpty = running.isEmpty ();
      running.put (tStart, new LinkedHashSet<> ());
      if (! runningIsEmpty)
        running.get (tStart).addAll (running.lowerEntry (tStart).getValue ());
      running.get (tStart).addAll (starts.get (tStart));
    }
    if (running.containsKey (Double.NEGATIVE_INFINITY))
    {
      final Set<TestJob1> jobsAtNegativeInfinity = running.get (Double.NEGATIVE_INFINITY);
      for (final TestJob1 j : jobsAtNegativeInfinity)
      {
        j.predictedDeparted = true;
        if (j.getServiceTime (queue) != Double.POSITIVE_INFINITY)
          j.predictedDepartureTime = Double.NEGATIVE_INFINITY;
        else
          j.predictedDepartureTime = Double.POSITIVE_INFINITY;
      }
      jobsCopy.removeAll (jobsAtNegativeInfinity);
      running.remove (Double.NEGATIVE_INFINITY);
    }
    if (running.containsKey (Double.POSITIVE_INFINITY))
    {
      final Set<TestJob1> jobsAtPositiveInfinity = running.get (Double.POSITIVE_INFINITY);
      for (final TestJob1 j : jobsAtPositiveInfinity)
      {
        j.predictedDeparted = true;
        j.predictedDepartureTime = Double.POSITIVE_INFINITY;
      }
      jobsCopy.removeAll (jobsAtPositiveInfinity);
      running.remove (Double.POSITIVE_INFINITY);
    }
    final Map<TestJob1, Double> rS = new HashMap<> ();
    for (final TestJob1 j : jobsCopy)
      rS.put (j, j.getServiceTime (queue));
    while (! jobsCopy.isEmpty ())
    {
      final double time = running.firstKey ();
      final Set<TestJob1> jobSet = running.get (time);
      if (! jobSet.isEmpty ())
      {        
        final int jobSetSize = jobSet.size ();
        double nextTime = running.higherKey (time) != null ? running.higherKey (time) : Double.POSITIVE_INFINITY;
        final TreeMap<Double, TestJob1> departureTimes = new TreeMap<> ();
        for (final TestJob1 j : jobSet)
          departureTimes.put (time + (rS.get (j) * jobSet.size ()), j);
        if (departureTimes.firstKey () <= nextTime)
        {
          nextTime = departureTimes.firstKey ();
          final TestJob1 jDepart = departureTimes.get (nextTime);
          for (final Set<TestJob1> stj : running.values ())
            stj.remove (jDepart);
          jobsCopy.remove (jDepart);
          jDepart.predictedDeparted = true;
          jDepart.predictedDepartureTime = nextTime;
          jobSet.remove (jDepart);
          if (! running.containsKey (nextTime))
            running.put (nextTime, new HashSet<> (jobSet));
        }
        for (final TestJob1 j : jobSet)
          rS.put (j, rS.get (j) - ((nextTime - time) / jobSetSize));
      }
      running.pollFirstEntry ();
    }
  }
  
  private final boolean quiet = true;
  
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
    if (! quiet)
      queue.registerStdOutSimQueueVacationListener ();
    for (int i = 0; i <= 1; i++)
    {
      System.out.println ("===== PASS " + i + " =====");
      final List<TestJob1> jobs = scheduleJobArrivals (! this.quiet, 40, el, queue);
      // Schedule vacation from 1.5 to 5.5.
      // Jobs 2, 3, 4, and 5 should be dropped.
      scheduleQueueAccessVacation (el, queue, 1.5, 4.0);
      final TreeMap<Double, Boolean> queueAccessVacationMap = new TreeMap<> ();
      queueAccessVacationMap.put (1.5, true);
      queueAccessVacationMap.put (5.5, false);
      // Schedule server access credits exhaustion at t=7.5, set to 1 at t=9.5, and set (back) to infinity at t=10.5.
      // Affects jobs 8, 9 and 10.
      // Start of job 8 is deferred until t=9.5.
      // Start of jobs 9 and 10 is deferred until t=10.5.
      // Schedule server-access credits exhaustion at t=39.5, causing all jobs from 40 onwards to wait indefinitely.
      scheduleServerAccessCredits (el, queue, 7.5, 0);
      scheduleServerAccessCredits (el, queue, 9.5, 1);
      scheduleServerAccessCredits (el, queue, 10.5, Integer.MAX_VALUE);
      scheduleServerAccessCredits (el, queue, 39.5, 0);
      final TreeMap<Double, Integer> serverAccessCreditsMap = new TreeMap<> ();
      serverAccessCreditsMap.put (7.5, 0);
      serverAccessCreditsMap.put (9.5, 1);
      serverAccessCreditsMap.put (10.5, Integer.MAX_VALUE);
      serverAccessCreditsMap.put (39.5, 0);
      queuePredictorPS (new HashSet<> (jobs), queue, queueAccessVacationMap, serverAccessCreditsMap);
      el.run ();
      assert el.isEmpty ();
      for (TestJob1 j : jobs)
        j.testPrediction (ACCURACY);
      // Test reset on the fly...
      el.reset ();
    }
  }

}
