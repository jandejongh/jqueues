package nl.jdj.jqueues.r3.mac;

/**
 *
 */
public interface DCFStateListener
{

  public void newDCFState (double time, DCF dcf, DCFState oldState, DCFState newState);
  
}
