package nl.jdj.jqueues.r4;

/**
 *
 */
public class DefaultSimQueueListener<J extends SimJob, Q extends SimQueue>
implements SimQueueListener<J, Q>
{

  @Override
  public void update (double t, Q queue)
  {
  }

  @Override
  public void arrival (double t, J job, Q queue)
  {
  }

  @Override
  public void start (double t, J job, Q queue)
  {
  }

  @Override
  public void drop (double t, J job, Q queue)
  {
  }

  @Override
  public void revocation (double t, J job, Q queue)
  {
  }

  @Override
  public void departure (double t, J job, Q queue)
  {
  }
  
}
