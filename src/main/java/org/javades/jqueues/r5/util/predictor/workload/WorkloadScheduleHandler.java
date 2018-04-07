/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.util.predictor.workload;

import java.util.Map;
import java.util.Set;
import org.javades.jqueues.r5.entity.SimEntitySimpleEventType;
import org.javades.jqueues.r5.entity.jq.SimJQEvent;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.extensions.gate.SimQueueGateEvent;
import org.javades.jqueues.r5.extensions.gate.SimQueuePredictor_GATE;
import org.javades.jqueues.r5.extensions.gate.SimQueueWithGate;

/** A handler for specific {@link SimJQEvent} types as extension to a {@link DefaultWorkloadSchedule}.
 * 
 * <p>
 * The {@link DefaultWorkloadSchedule} deals with the basic external {@link SimQueue} events:
 * queue-access vacations, arrivals, revocations, and server-access credits.
 * However, it fails with a {@link WorkloadScheduleInvalidException} if it finds events in its input that it does not know about.
 * 
 * <p>
 * The handler-extension mechanism through {@link DefaultWorkloadSchedule#registerHandler} allows
 * the registration of a handler, i.c., a {@link WorkloadScheduleHandler}, that manages
 * a specific subset of {@link SimJQEvent} types (classes).
 * An example of such an event is the {@link SimQueueGateEvent} for {@link SimQueueWithGate}s.
 * The extension mechanism is particularly useful for such state extensions to a {@link SimQueue} with
 * accompanying {@link SimJQEvent}s, especially for queues with multiple state extensions
 * because (1) the complexity of {@link DefaultWorkloadSchedule} makes it unattractive for inheritance, and
 * (2) the resulting subclasses would become increasingly complex given the fact that Java does not
 * fully support multiple inheritance of implementation.
 * 
 * @see DefaultWorkloadSchedule
 * @see SimQueuePredictor_GATE
 *
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public interface WorkloadScheduleHandler
{

  /** Returns the name of the handler.
   * 
   * <p>
   * The handler name must be unique within the realm of the {@link DefaultWorkloadSchedule} at which this
   * handler registers. For {@link SimQueue} state extensions, the convention is to use
   * the interface name appended with "Handler", like, "SimQueueHandler" and "SimQueueWithGateHandler".
   * 
   * @return The name of the handler (must remain fixed during the handler's lifetime).
   * 
   */
  public String getHandlerName ();
  
  /** Gets the set of {@link SimJQEvent}s of this handler, and the {@link SimEntitySimpleEventType} members onto which they
   *  map.
   * 
   * <p>
   * The key-set should contains unique {@link SimJQEvent}s within the realm of the {@link DefaultWorkloadSchedule}
   * at which this handler registers.
   * Likewise, the values should be unique.
   * 
   * @return The set of {@link SimJQEvent}s of this handler, and the {@link SimEntitySimpleEventType} members onto which they
   *         map.
   * 
   */
  public Map<Class<? extends SimJQEvent>, SimEntitySimpleEventType.Member> getEventMap ();
  
  /** Whether or not this object needs to scan the {@link DefaultWorkloadSchedule} upon registration.
   * 
   * @return True if this handler needs to scan the host {@link DefaultWorkloadSchedule} upon registration.
   * 
   * @see DefaultWorkloadSchedule#registerHandler
   * @see #scan
   * 
   */
  public boolean needsScan ();
  
  /** Scans the host {@link DefaultWorkloadSchedule} and builds internal data structures.
   * 
   * <p>
   * Called by our host {@link DefaultWorkloadSchedule} <i>only</i> upon registration,
   * and if we ask for it {@link #needsScan}.
   * 
   * @param workloadSchedule The host at which we registered.
   * 
   * @return The {@link SimJQEvent}s processed by this handler,
   *         in a set with no particular member ordering; may be {@code null} or empty.
   * 
   * @throws WorkloadScheduleException If the workload is invalid or ambiguous.
   * 
   * @see DefaultWorkloadSchedule#registerHandler
   * @see #scan
   * 
   */
  public Set<SimJQEvent> scan (final DefaultWorkloadSchedule workloadSchedule)
  throws WorkloadScheduleException;

}
