package nl.jdj.jqueues.r4.mac;

/**
 *
 */
public interface DCFStateListener
{

  public void newDCFState (double time, DCF dcf, DCFState oldState, DCFState newState);
  
}
