/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.redis.internal.executor.string;


import java.util.List;

import org.apache.geode.redis.internal.data.RedisKey;
import org.apache.geode.redis.internal.executor.RedisResponse;
import org.apache.geode.redis.internal.netty.Command;
import org.apache.geode.redis.internal.netty.ExecutionHandlerContext;

public class GetSetExecutor extends StringExecutor {

  private static final int VALUE_INDEX = 2;

  @Override
  public RedisResponse executeCommand(Command command, ExecutionHandlerContext context) {
    List<byte[]> commandElems = command.getProcessedCommand();

    RedisKey key = command.getKey();
    byte[] newCharValue = commandElems.get(VALUE_INDEX);

    RedisStringCommands stringCommands = getRedisStringCommands(context);
    byte[] oldValueWrapper = stringCommands.getset(key, newCharValue);

    return respondBulkStrings(oldValueWrapper);
  }
}