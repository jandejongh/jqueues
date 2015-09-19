package nl.jdj.jqueues.r4;

/**
 *
 */
public class StdOutSimQueueListener<J extends SimJob, Q extends SimQueue>
implements SimQueueListener<J, Q>
{

  @Override
  public void update (double t, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": UPDATE.");
  }

  @Override
  public void arrival (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": ARRIVAL of job " + job + ".");
  }

  @Override
  public void start (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": START of job " + job + ".");
  }

  @Override
  public void drop (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DROP of job " + job + ".");
  }

  @Override
  public void revocation (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": REVOCATION of job " + job + ".");
  }

  @Override
  public void departure (double t, J job, Q queue)
  {
    System.out.println ("[StdOutSimQueueListener] t=" + t + ", queue=" + queue + ": DEPARTURE of job " + job + ".");
  }
  
}
