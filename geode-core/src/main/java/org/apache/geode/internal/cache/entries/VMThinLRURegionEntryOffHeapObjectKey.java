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
package org.apache.geode.internal.cache.entries;

// DO NOT modify this class. It was generated from LeafRegionEntry.cpp



import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.apache.geode.internal.cache.RegionEntryContext;

<<<<<<< HEAD:geode-core/src/main/java/org/apache/geode/internal/cache/entries/VMThinLRURegionEntryOffHeapObjectKey.java
import org.apache.geode.internal.cache.eviction.EnableLRU;
import org.apache.geode.internal.cache.eviction.LRUListNode;
import org.apache.geode.internal.cache.persistence.DiskRecoveryStore;
=======
import org.apache.geode.internal.cache.lru.NewLRUClockHand;
>>>>>>> dfbd9e438... initial wip:geode-core/src/main/java/org/apache/geode/internal/cache/VMThinLRURegionEntryOffHeapObjectKey.java

import org.apache.geode.internal.cache.Token;
import org.apache.geode.internal.offheap.OffHeapRegionEntryHelper;
import org.apache.geode.internal.offheap.annotations.Released;
import org.apache.geode.internal.offheap.annotations.Retained;
import org.apache.geode.internal.offheap.annotations.Unretained;

import org.apache.geode.internal.util.concurrent.CustomEntryConcurrentHashMap.HashEntry;

/*
 * macros whose definition changes this class:
 *
 * disk: DISK lru: LRU stats: STATS versioned: VERSIONED offheap: OFFHEAP
 *
 * One of the following key macros must be defined:
 *
 * key object: KEY_OBJECT key int: KEY_INT key long: KEY_LONG key uuid: KEY_UUID key string1:
 * KEY_STRING1 key string2: KEY_STRING2
 */

/**
 * Do not modify this class. It was generated. Instead modify LeafRegionEntry.cpp and then run
 * ./dev-tools/generateRegionEntryClasses.sh (it must be run from the top level directory).
 */
public class VMThinLRURegionEntryOffHeapObjectKey extends VMThinLRURegionEntryOffHeap {

  // --------------------------------------- common fields ----------------------------------------

  private static final AtomicLongFieldUpdater<VMThinLRURegionEntryOffHeapObjectKey> LAST_MODIFIED_UPDATER =
      AtomicLongFieldUpdater.newUpdater(VMThinLRURegionEntryOffHeapObjectKey.class, "lastModified");

  protected int hash;

  private HashEntry<Object, Object> nextEntry;

  @SuppressWarnings("unused")
  private volatile long lastModified;



  // --------------------------------------- offheap fields ---------------------------------------

  /**
   * All access done using OFF_HEAP_ADDRESS_UPDATER so it is used even though the compiler can not
   * tell it is.
   */
  @SuppressWarnings("unused")
  @Retained
  @Released
  private volatile long offHeapAddress;
  /**
   * I needed to add this because I wanted clear to call setValue which normally can only be called
   * while the re is synced. But if I sync in that code it causes a lock ordering deadlock with the
   * disk regions because they also get a rw lock in clear. Some hardware platforms do not support
   * CAS on a long. If gemfire is run on one of those the AtomicLongFieldUpdater does a sync on the
   * RegionEntry and we will once again be deadlocked. I don't know if we support any of the
   * hardware platforms that do not have a 64bit CAS. If we do then we can expect deadlocks on disk
   * regions.
   */
  private static final AtomicLongFieldUpdater<VMThinLRURegionEntryOffHeapObjectKey> OFF_HEAP_ADDRESS_UPDATER =
      AtomicLongFieldUpdater.newUpdater(VMThinLRURegionEntryOffHeapObjectKey.class,
          "offHeapAddress");


  // ----------------------------------------- key code -------------------------------------------
  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp


  private final Object key;


  public VMThinLRURegionEntryOffHeapObjectKey(final RegionEntryContext context, final Object key,

      @Retained

      final Object value



  ) {
    super(context,



        value

    );
    // DO NOT modify this class. It was generated from LeafRegionEntry.cpp



    this.key = key;

  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp


  @Override
  public Token getValueAsToken() {
    return OffHeapRegionEntryHelper.getValueAsToken(this);
  }

  @Override
  protected Object getValueField() {
    return OffHeapRegionEntryHelper._getValue(this);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override

  @Unretained
  protected void setValueField(@Unretained final Object value) {



    OffHeapRegionEntryHelper.setValue(this, value);
  }

  @Override

  @Retained

  public Object getValueRetain(final RegionEntryContext context, final boolean decompress) {
    return OffHeapRegionEntryHelper._getValueRetain(this, decompress, context);
  }

  @Override
  public long getAddress() {
    return OFF_HEAP_ADDRESS_UPDATER.get(this);
  }

  @Override
  public boolean setAddress(final long expectedAddress, long newAddress) {
    return OFF_HEAP_ADDRESS_UPDATER.compareAndSet(this, expectedAddress, newAddress);
  }

  @Override

  @Released

  public void release() {
    OffHeapRegionEntryHelper.releaseEntry(this);
  }

  @Override
  public void returnToPool() {
    // never implemented
  }


  @Override
  protected long getLastModifiedField() {
    return LAST_MODIFIED_UPDATER.get(this);
  }

  @Override
  protected boolean compareAndSetLastModifiedField(final long expectedValue, final long newValue) {
    return LAST_MODIFIED_UPDATER.compareAndSet(this, expectedValue, newValue);
  }

  @Override
  public int getEntryHash() {
    return this.hash;
  }

  @Override
  protected void setEntryHash(final int hash) {
    this.hash = hash;
  }

  @Override
  public HashEntry<Object, Object> getNextEntry() {
    return this.nextEntry;
  }

  @Override
  public void setNextEntry(final HashEntry<Object, Object> nextEntry) {
    this.nextEntry = nextEntry;
  }



  // --------------------------------------- eviction code ----------------------------------------
  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public void setDelayedDiskId(final DiskRecoveryStore diskRecoveryStore) {



    // nothing needed for LRUs with no disk

  }

  @Override
  public synchronized int updateEntrySize(final EnableLRU capacityController) {
    // 1: getValue ok w/o incing refcount because we are synced and only getting the size
    return updateEntrySize(capacityController, getValue());
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public synchronized int updateEntrySize(final EnableLRU capacityController, final Object value) {
    int oldSize = getEntrySize();
    int newSize = capacityController.entrySize(getKeyForSizing(), value);
    setEntrySize(newSize);
    int delta = newSize - oldSize;
    return delta;
  }

  @Override
  public boolean testRecentlyUsed() {
    return areAnyBitsSet(RECENTLY_USED);
  }

  @Override
  public void setRecentlyUsed() {
    setBits(RECENTLY_USED);
  }

  @Override
  public void unsetRecentlyUsed() {
    clearBits(~RECENTLY_USED);
  }

  @Override
  public boolean testEvicted() {
    return areAnyBitsSet(EVICTED);
  }

  @Override
  public void setEvicted() {
    setBits(EVICTED);
  }

  @Override
  public void unsetEvicted() {
    clearBits(~EVICTED);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  private LRUListNode nextLRU;
<<<<<<< HEAD:geode-core/src/main/java/org/apache/geode/internal/cache/entries/VMThinLRURegionEntryOffHeapObjectKey.java
  private LRUListNode previousLRU;
  private int size;

  @Override
  public void setNextLRUNode(final LRUListNode nextLRU) {
    this.nextLRU = nextLRU;
  }

  @Override
=======
  private LRUListNode prevLRU;
  private int size;

  public void setNextLRUNode(LRUListNode next) {
    this.nextLRU = next;
  }

>>>>>>> dfbd9e438... initial wip:geode-core/src/main/java/org/apache/geode/internal/cache/VMThinLRURegionEntryOffHeapObjectKey.java
  public LRUListNode nextLRUNode() {
    return this.nextLRU;
  }

<<<<<<< HEAD:geode-core/src/main/java/org/apache/geode/internal/cache/entries/VMThinLRURegionEntryOffHeapObjectKey.java
  @Override
  public void setPrevLRUNode(final LRUListNode previousLRU) {
    this.previousLRU = previousLRU;
  }

  @Override
  public LRUListNode prevLRUNode() {
    return this.previousLRU;
=======
  public void setPrevLRUNode(LRUListNode prev) {
    this.prevLRU = prev;
  }

  public LRUListNode prevLRUNode() {
    return this.prevLRU;
>>>>>>> dfbd9e438... initial wip:geode-core/src/main/java/org/apache/geode/internal/cache/VMThinLRURegionEntryOffHeapObjectKey.java
  }

  @Override
  public int getEntrySize() {
    return this.size;
  }

  protected void setEntrySize(final int size) {
    this.size = size;
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public Object getKeyForSizing() {

    // default implementation.
    return getKey();



  }



  // ----------------------------------------- key code -------------------------------------------
  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp


  @Override
  public Object getKey() {
    return this.key;
  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp
}

