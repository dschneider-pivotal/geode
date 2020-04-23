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

package org.apache.geode.redis.internal.executor.set;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geode.cache.Region;
import org.apache.geode.redis.internal.ByteArrayWrapper;
import org.apache.geode.redis.internal.RegionProvider;

public class GeodeRedisSetCompoundKeys implements RedisSet {

  private ByteArrayWrapper key;
  private RegionProvider regionProvider;

  public GeodeRedisSetCompoundKeys(ByteArrayWrapper key, RegionProvider regionProvider) {
    this.key = key;
    this.regionProvider = regionProvider;
  }

  private String createCompoundKey(ByteArrayWrapper key, ByteArrayWrapper member) {
    StringBuilder stringBuilder = new StringBuilder();
    return stringBuilder.append(key).append(member).toString();
  }

  private boolean containsKey(String compoundKey, ByteArrayWrapper key) {
    return compoundKey.startsWith(key.toString());
  }

  @Override
  public long sadd(Collection<ByteArrayWrapper> membersToAdd) {
    long addedCount = 0;
    for (ByteArrayWrapper memberToAdd : membersToAdd) {
      if (sadd(memberToAdd)) {
        addedCount++;
      }
    }
    return addedCount;
  }

  private boolean sadd(ByteArrayWrapper memberToAdd) {
    String compoundKey = createCompoundKey(this.key, memberToAdd);
    ByteArrayWrapper oldValue = region().putIfAbsent(compoundKey, memberToAdd);
    return oldValue == null;
  }

  @Override
  public long srem(Collection<ByteArrayWrapper> membersToRemove) {
    long removedCount = 0;
    for (ByteArrayWrapper memberToRemove : membersToRemove) {
      if (srem(memberToRemove)) {
        removedCount++;
      }
    }
    return removedCount;
  }

  private boolean srem(ByteArrayWrapper memberToRemove) {
    String compoundKey = createCompoundKey(this.key, memberToRemove);
    ByteArrayWrapper oldValue = region().remove(compoundKey);
    return oldValue != null;
  }

  @Override
  public Set<ByteArrayWrapper> members() {
    HashSet<ByteArrayWrapper> result = new HashSet<>();
    for (Map.Entry<String, ByteArrayWrapper> entry : region().entrySet()) {
      if (containsKey(entry.getKey(), this.key)) {
        result.add(entry.getValue());
      }
    }
    return result;
  }

  @Override
  public boolean del() {
    boolean result = false;
    for (String entryKey : region().keySet()) {
      if (containsKey(entryKey, this.key)) {
        boolean removedThisEntry = region().remove(entryKey) != null;
        if (removedThisEntry) {
          result = true;
        }
      }
    }
    return result;
  }

  @Override
  public boolean contains(ByteArrayWrapper member) {
    String compoundKey = createCompoundKey(this.key, member);
    return region().containsKey(compoundKey);
  }


  @SuppressWarnings("unchecked")
  Region<String, ByteArrayWrapper> region() {
    return (Region<String, ByteArrayWrapper>) regionProvider.getSetRegion();
  }
}
