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
package org.apache.geode.internal.cache.eviction;

public class GuardNode implements LRUListNode {

  private LRUListNode next;
  LRUListNode prev;

  public int getEntrySize() {
    return 0;
  }

  public LRUListNode nextLRUNode() {
    return next;
  }

  public LRUListNode prevLRUNode() {
    return prev;
  }

  public void setEvicted() {

  }

  public void setNextLRUNode(LRUListNode next) {
    this.next = next;
  }

  public void setPrevLRUNode(LRUListNode prev) {
    this.prev = prev;
  }

  public void setRecentlyUsed() {}

  public boolean testEvicted() {
    return false;
  }

  public boolean testRecentlyUsed() {
    return false;
  }

  public void unsetEvicted() {}

  public void unsetRecentlyUsed() {}

  public int updateEntrySize(EnableLRU ccHelper) {
    return 0;
  }

  public int updateEntrySize(EnableLRU ccHelper, Object value) {
    return 0;
  }

  @Override
  public boolean isInUseByTransaction() {
    return false;
  }
}
