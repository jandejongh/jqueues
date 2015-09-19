package nl.jdj.jqueues.r4.mac;

  
/** Possible DCF States (simplified).
 *
 * <ul>
 * <li>{@link #IDLE_INIT}:    Nothing in queue, MEDIUM and NAV idle, initial state.
 * <li>{@link #IDLE_SHARP}:   Nothing in queue, MEDIUM and NAV idle, IFS expired.
 * <li>{@link #IDLE_IFS}:     Nothing in queue, MEDIUM and NAV idle, awaiting IFS expiration.
 * <li>{@link #IDLE_BACKOFF}: Nothing in queue, MEDIUM and NAV idle, IFS expired, awaiting back-off.
 * <li>{@link #IDLE_RX}:      Nothing in queue, MEDIUM busy, receiving.
 * <li>{@link #IDLE_NAV}:     Nothing in queue, MEDIUM idle but awaiting NAV expiration.
 * <li>{@link #BUSY_IFS}:     Non-empty queue,  MEDIUM and NAV idle, awaiting IFS expiration.
 * <li>{@link #BUSY_BACKOFF}: Non-empty queue,  MEDIUM and NAV idle, IFS expired, backing off.
 * <li>{@link #BUSY_RX}:      Non-empty queue,  MEDIUM busy, receiving.
 * <li>{@link #BUSY_NAV}:     Non-empty queue,  MEDIUM idle but awaiting NAV expiration.
 * <li>{@link #BUSY_TX}:      Transmitting.
 * </ul>
 *
 */  
public enum DCFState
{
  IDLE_INIT,
  IDLE_SHARP,
  IDLE_IFS,
  IDLE_BACKOFF,
  IDLE_RX,
  IDLE_NAV,
  BUSY_IFS,
  BUSY_BACKOFF,
  BUSY_RX,
  BUSY_NAV,
  BUSY_TX
}
