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

import org.apache.geode.StatisticsFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.internal.cache.BucketRegion;
import org.apache.geode.internal.cache.GemFireCacheImpl;
import org.apache.geode.internal.cache.InternalRegionArguments;
import org.apache.geode.internal.cache.PartitionedRegion;
import org.apache.geode.internal.cache.PlaceHolderDiskRegion;
import org.apache.geode.internal.cache.entries.AbstractRegionEntry;
import org.apache.geode.internal.cache.versions.RegionVersionVector;
import org.apache.geode.internal.i18n.LocalizedStrings;
import org.apache.geode.internal.logging.LogService;
import org.apache.geode.internal.logging.log4j.LocalizedMessage;
import org.apache.geode.internal.logging.log4j.LogMarker;
import org.apache.logging.log4j.Logger;

/**
 * LRUListWithAsyncSorting holds the lrulist, and the behavior for maintaining the list and
 * determining the next entry to be removed. Each EntriesMap that supports LRU holds one of these.
 * Evicts are always done from the head and assume that it is the least recent entry unless if is
 * being used by a transaction or is already evicted in which case it is removed from the list and
 * the next item is evicted. Adds are always done to the end of the list and should not be marked
 * recently used. An async scanner runs periodically (how often TBD), head to tail, removing entries
 * that have been recently used, marking them as not recently used, and adding them back to the
 * tail. Removes may unlink entries from the list.
 */
public class LRUListWithAsyncSorting implements LRUList {
  private static final Logger logger = LogService.getLogger();

  private BucketRegion bucketRegion = null;

  /** The last node in the LRU list after which all new nodes are added */
  protected final LRUListNode tail = new GuardNode();

  /** The starting point in the LRU list for searching for the LRU node */
  protected final LRUListNode head = new GuardNode();

  /** The object for locking this list */
  final protected Object lock = new Object();

  /** Description of the Field */
  final private LRUStatistics stats;
  /** Counter for the size of the LRU list */
  protected int size = 0;

  public LRUListWithAsyncSorting(Object region, EnableLRU ccHelper,
      InternalRegionArguments internalRegionArgs) {
    setBucketRegion(region);
    initEmptyList();
    if (this.bucketRegion != null) {
      this.stats = internalRegionArgs.getPartitionedRegion() != null
          ? internalRegionArgs.getPartitionedRegion().getEvictionController().stats : null;
    } else {
      LRUStatistics tmp = null;
      if (region instanceof PlaceHolderDiskRegion) {
        tmp = ((PlaceHolderDiskRegion) region).getPRLRUStats();
      } else if (region instanceof PartitionedRegion) {
        tmp = ((PartitionedRegion) region).getPRLRUStatsDuringInitialization(); // bug 41938
        PartitionedRegion pr = (PartitionedRegion) region;
        if (tmp != null) {
          pr.getEvictionController().stats = tmp;
        }
      }
      if (tmp == null) {
        StatisticsFactory sf = GemFireCacheImpl.getExisting("").getDistributedSystem();
        tmp = ccHelper.initStats(region, sf);
      }
      this.stats = tmp;
    }
  }

  public void setBucketRegion(Object r) {
    if (r instanceof BucketRegion) {
      this.bucketRegion = (BucketRegion) r; // see bug 41388
    }
  }

  public LRUListWithAsyncSorting(Region region, EnableLRU ccHelper,
      LRUListWithAsyncSorting oldList) {
    setBucketRegion(region);
    initEmptyList();
    if (oldList.stats == null) {
      // see bug 41388
      StatisticsFactory sf = region.getCache().getDistributedSystem();
      this.stats = ccHelper.initStats(region, sf);
    } else {
      this.stats = oldList.stats;
      if (this.bucketRegion != null) {
        this.stats.decrementCounter(this.bucketRegion.getCounter());
        this.bucketRegion.resetCounter();
      } else {
        this.stats.resetCounter();
      }
    }
  }

  @Override
  public void closeStats() {
    LRUStatistics ls = this.stats;
    if (ls != null) {
      ls.close();
    }
  }

  /**
   * Adds an lru node to the tail of the list.
   */
  @Override
  public void appendEntry(final LRUListNode aNode) {
    synchronized (this.lock) {
      if (aNode.nextLRUNode() != null) {
        // already in the list
        return;
      }

      aNode.unsetRecentlyUsed();

      if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
        logger.trace(LogMarker.LRU_CLOCK, LocalizedMessage
            .create(LocalizedStrings.NewLRUClockHand_ADDING_ANODE_TO_LRU_LIST, aNode));
      }
      aNode.setNextLRUNode(this.tail);
      this.tail.prevLRUNode().setNextLRUNode(aNode);
      aNode.setPrevLRUNode(this.tail.prevLRUNode());
      this.tail.setPrevLRUNode(aNode);

      this.size++;
    }
  }

  /**
   * Remove and return the head entry in the list
   */
  private LRUListNode getHeadEntry() {
    synchronized (lock) {
      LRUListNode aNode = this.head.nextLRUNode();
      if (aNode == this.tail) {
        // empty list
        return null;
      }

      LRUListNode next = aNode.nextLRUNode();
      this.head.setNextLRUNode(next);
      next.setPrevLRUNode(this.head);

      aNode.setNextLRUNode(null);
      aNode.setPrevLRUNode(null);
      this.size--;
      return aNode;
    }
  }


  /**
   * Remove and return the Entry that is considered least recently used.
   */
  @Override
  public LRUListNode getLRUEntry() {
    for (;;) {
      final LRUListNode aNode = getHeadEntry();

      if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
        logger.trace(LogMarker.LRU_CLOCK, "lru considering {}", aNode);
      }

      if (aNode == null) { // hit the end of the list
        return null;
      } // hit the end of the list

      // If this Entry is part of a transaction, skip it since
      // eviction should not cause commit conflicts
      synchronized (aNode) {
        if (aNode.isInUseByTransaction()) {
          if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
            logger.trace(LogMarker.LRU_CLOCK, LocalizedMessage.create(
                LocalizedStrings.NewLRUClockHand_REMOVING_TRANSACTIONAL_ENTRY_FROM_CONSIDERATION));
          }
          continue;
        }
        if (aNode.testEvicted()) {
          if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
            logger.trace(LogMarker.LRU_CLOCK,
                LocalizedMessage.create(LocalizedStrings.NewLRUClockHand_DISCARDING_EVICTED_ENTRY));
          }
          continue;
        }
      }

      if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
        logger.trace(LogMarker.LRU_CLOCK, LocalizedMessage
            .create(LocalizedStrings.NewLRUClockHand_RETURNING_UNUSED_ENTRY, aNode));
      }
      return aNode;
    } // for
  }

  public void dumpList() {
    final boolean isDebugEnabled = logger.isTraceEnabled(LogMarker.LRU_CLOCK);
    if (!isDebugEnabled) {
      return;
    }
    synchronized (lock) {
      int idx = 1;
      for (LRUListNode aNode = this.head; aNode != null; aNode = aNode.nextLRUNode()) {
        if (isDebugEnabled) {
          logger.trace(LogMarker.LRU_CLOCK, "  ({}) {}", (idx++), aNode);
        }
      }
    }
  }

  private String getAuditReport() {
    int totalNodes = 0;
    int evictedNodes = 0;
    int usedNodes = 0;
    synchronized (lock) {
      LRUListNode h = this.head;
      while (h != null) {
        totalNodes++;
        if (h.testEvicted())
          evictedNodes++;
        if (h.testRecentlyUsed())
          usedNodes++;
        h = h.nextLRUNode();
      }
    }
    StringBuilder result = new StringBuilder(128);
    result.append("LRUList Audit: listEntries = ").append(totalNodes).append(" evicted = ")
        .append(evictedNodes).append(" used = ").append(usedNodes);
    return result.toString();
  }


  @Override
  public void audit() {
    System.out.println(getAuditReport());
  }

  @Override
  public boolean unlinkEntry(LRUListNode entry) {
    if (logger.isTraceEnabled(LogMarker.LRU_CLOCK)) {
      logger.trace(LogMarker.LRU_CLOCK,
          LocalizedMessage.create(LocalizedStrings.NewLRUClockHand_UNLINKENTRY_CALLED, entry));
    }
    synchronized (lock) {
      LRUListNode next = entry.nextLRUNode();
      LRUListNode prev = entry.prevLRUNode();
      if (next == null) {
        // not in the list anymore.
        return false;
      }
      next.setPrevLRUNode(prev);
      prev.setNextLRUNode(next);
      entry.setNextLRUNode(null);
      entry.setPrevLRUNode(null);
      this.size--;
      entry.setEvicted();
    }
    stats().incDestroys();
    return true;
  }

  public void scan() {
    LRUListNode aNode;
    do {
      synchronized (lock) {
        aNode = this.head.nextLRUNode();
      }
      while (aNode != null && aNode != this.tail) {
        // TODO add testAndSetRecentlyUsed instead of having two methods.
        // No need to sync on aNode here. If the bit is set the only one to clear
        // it is us (i.e. the scan) or evict/remove code. If either of these
        // happen then this will be detected by next and prev being null.
        if (aNode.testRecentlyUsed()) {
          aNode.unsetRecentlyUsed();
          LRUListNode next;
          synchronized (lock) {
            next = aNode.nextLRUNode();
            if (next != null) {
              LRUListNode prev = aNode.prevLRUNode();
              next.setPrevLRUNode(prev);
              prev.setNextLRUNode(next);
              aNode.setNextLRUNode(this.tail);
              this.tail.prevLRUNode().setNextLRUNode(aNode);
              aNode.setPrevLRUNode(this.tail.prevLRUNode());
              this.tail.setPrevLRUNode(aNode);
            }
          }
          aNode = next;
        } else {
          synchronized (lock) {
            aNode = aNode.nextLRUNode();
          }
        }
      }
      // null indicates we tried to scan past a node that was concurrently removed.
      // In that case we need to start at the beginning.
    } while (aNode == null);
  }

  @Override
  public LRUStatistics stats() {
    return this.stats;
  }

  @Override
  public void clear(RegionVersionVector rvv) {
    if (rvv != null) {
      return; // when concurrency checks are enabled the clear operation removes entries iteratively
    }
    synchronized (this.lock) {
      if (bucketRegion != null) {
        this.stats.decrementCounter(bucketRegion.getCounter());
        bucketRegion.resetCounter();
      } else {
        this.stats.resetCounter();
      }
      initEmptyList();
    }
  }

  private void initEmptyList() {
    this.size = 0;
    this.head.setNextLRUNode(this.tail);
    this.tail.setPrevLRUNode(this.head);
  }

  @Override
  public int size() {
    synchronized (lock) {
      return size;
    }
  }
}
