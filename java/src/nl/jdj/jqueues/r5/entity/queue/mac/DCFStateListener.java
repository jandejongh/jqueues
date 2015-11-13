package nl.jdj.jqueues.r5.entity.queue.mac;

/**
 *
 */
public interface DCFStateListener
{

  public void newDCFState (double time, DCF dcf, DCFState oldState, DCFState newState);
  
}
