package nl.jdj.jqueues.r5.misc.book.book_010_guided_tour;

import java.util.HashMap;
import java.util.Map;
import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.listener.DefaultSimQueueListener;

public class JobSojournTimeListenerWithReset
extends DefaultSimQueueListener
{

  private final Map<SimJob, Double> jobArrTimes = new HashMap<> ();

  private int jobsPassed = 0;
  private double cumJobSojournTime = 0;

  @Override
  public void notifyResetEntity (SimEntity entity)
  {
    super.notifyResetEntity (entity);
    this.jobArrTimes.clear ();
    this.jobsPassed = 0;
    this.cumJobSojournTime = 0;
  }
    
  @Override
  public void notifyArrival (double time, SimJob job, SimQueue queue)
  {
    if (this.jobArrTimes.containsKey (job))
      throw new IllegalStateException ();
    this.jobArrTimes.put (job, time);
  }
  
  @Override
  public void notifyDeparture (double time, SimJob job, SimQueue queue)
  {
    if (! this.jobArrTimes.containsKey (job))
      throw new IllegalStateException ();
    final double jobSojournTime = time - this.jobArrTimes.get (job);
    if (jobSojournTime < 0)
      throw new IllegalStateException ();
    this.jobArrTimes.remove (job);
    this.jobsPassed++;
    this.cumJobSojournTime += jobSojournTime;
  }

  @Override
  public void notifyDrop (double time, SimJob job, SimQueue queue)
  {
    notifyDeparture (time, job, queue);
  }
  
  public double getAvgSojournTime ()
  {
    if (this.jobsPassed == 0)
      return Double.NaN;
    return this.cumJobSojournTime / this.jobsPassed;
  }
  
}
