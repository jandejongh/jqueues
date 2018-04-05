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
package nl.jdj.jqueues.r5.entity.jq.job;

import nl.jdj.jqueues.r5.entity.SimEntity;
import nl.jdj.jqueues.r5.entity.SimEntityOperation;
import nl.jdj.jqueues.r5.entity.jq.SimJQOperation;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;

/** The definition of an operation on a {SimJob}.
 * 
 * @param <J>   The type of {@link SimJob}s supported.
 * @param <Q>   The type of {@link SimQueue}s supported.
 * @param <O>   The operation type.
 * @param <Req> The request type (corresponding to the operation type).
 * @param <Rep> The reply type (corresponding to the operation type).
 * 
 * @see SimEntity#getRegisteredOperations
 * @see SimEntity#doOperation
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
public interface SimJobOperation
  <J extends SimJob,
   Q extends SimQueue,
   O extends SimEntityOperation,
   Req extends SimEntityOperation.Request,
   Rep extends SimEntityOperation.Reply>
  extends SimJQOperation<J, Q, O, Req, Rep>
{
  
  /* EMPTY */
  
}
