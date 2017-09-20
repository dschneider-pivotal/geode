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
package org.apache.geode.internal.cache;

import org.apache.geode.internal.cache.eviction.EnableLRU;
import org.apache.geode.internal.cache.eviction.LRUList;

/**
 * Internal implementation of {@link RegionMap} for regions stored in normal VM memory that maintain
 * an LRU.
 *
 * @since GemFire 3.5.1
 */
class VMLRURegionMap extends AbstractLRURegionMap {

  VMLRURegionMap(Object owner, Attributes attr, InternalRegionArguments internalRegionArgs) {
    super(internalRegionArgs);
    initialize(owner, attr, internalRegionArgs);
  }

  /**
   * A tool from the eviction controller for sizing entries and expressing limits.
   */
  private EnableLRU ccHelper;

  /** The list of nodes in LRU order */
  private LRUList lruList;

  @Override
  protected void _setCCHelper(EnableLRU ccHelper) {
    this.ccHelper = ccHelper;
  }

  @Override
  protected EnableLRU _getCCHelper() {
    return this.ccHelper;
  }

  @Override
  protected void _setLruList(LRUList lruList) {
    this.lruList = lruList;
  }

  @Override
  public LRUList _getLruList() {
    return this.lruList;
  }
}
