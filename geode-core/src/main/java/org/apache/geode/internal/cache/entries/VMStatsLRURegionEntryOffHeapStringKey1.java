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



import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.apache.geode.internal.cache.RegionEntryContext;

import org.apache.geode.internal.cache.lru.EnableLRU;

import org.apache.geode.internal.InternalStatisticsDisabledException;

import org.apache.geode.internal.cache.LocalRegion;
import org.apache.geode.internal.cache.lru.LRUClockNode;
import org.apache.geode.internal.cache.lru.NewLRUClockHand;

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
public class VMStatsLRURegionEntryOffHeapStringKey1 extends VMStatsLRURegionEntryOffHeap {

  public VMStatsLRURegionEntryOffHeapStringKey1(final RegionEntryContext context, final String key,

      @Retained

      final Object value

      , final boolean byteEncode

  ) {
    super(context,



        value

    );
    // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

    // caller has already confirmed that key.length <= MAX_INLINE_STRING_KEY
    long tempBits1 = 0L;
    if (byteEncode) {
      for (int i = key.length() - 1; i >= 0; i--) {
        // Note: we know each byte is <= 0x7f so the "& 0xff" is not needed. But I added it in to
        // keep findbugs happy.
        tempBits1 |= (byte) key.charAt(i) & 0xff;
        tempBits1 <<= 8;
      }
      tempBits1 |= 1 << 6;
    } else {
      for (int i = key.length() - 1; i >= 0; i--) {
        tempBits1 |= key.charAt(i);
        tempBits1 <<= 16;
      }
    }
    tempBits1 |= key.length();
    this.bits1 = tempBits1;

  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // common code
  protected int hash;
  private HashEntry<Object, Object> next;
  @SuppressWarnings("unused")
  private volatile long lastModified;
  private static final AtomicLongFieldUpdater<VMStatsLRURegionEntryOffHeapStringKey1> lastModifiedUpdater =
      AtomicLongFieldUpdater.newUpdater(VMStatsLRURegionEntryOffHeapStringKey1.class,
          "lastModified");

  /**
   * All access done using offHeapAddressUpdater so it is used even though the compiler can not tell
   * it is.
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
   * re and we will once again be deadlocked. I don't know if we support any of the hardware
   * platforms that do not have a 64bit CAS. If we do then we can expect deadlocks on disk regions.
   */
  private final static AtomicLongFieldUpdater<VMStatsLRURegionEntryOffHeapStringKey1> offHeapAddressUpdater =
      AtomicLongFieldUpdater.newUpdater(VMStatsLRURegionEntryOffHeapStringKey1.class,
          "offHeapAddress");

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
    return offHeapAddressUpdater.get(this);
  }

  @Override
  public boolean setAddress(final long expectedAddress, long newAddress) {
    return offHeapAddressUpdater.compareAndSet(this, expectedAddress, newAddress);
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


  protected long getLastModifiedField() {
    return lastModifiedUpdater.get(this);
  }

  protected boolean compareAndSetLastModifiedField(final long expectedValue, final long newValue) {
    return lastModifiedUpdater.compareAndSet(this, expectedValue, newValue);
  }

  @Override
  public int getEntryHash() {
    return this.hash;
  }

  protected void setEntryHash(final int hash) {
    this.hash = hash;
  }

  @Override
  public HashEntry<Object, Object> getNextEntry() {
    return this.next;
  }

  @Override
  public void setNextEntry(final HashEntry<Object, Object> next) {
    this.next = next;
  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // lru code

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

  private LRUClockNode nextLRU;
  private LRUClockNode previousLRU;
  private int size;

  @Override
  public void setNextLRUNode(final LRUClockNode nextLRU) {
    this.nextLRU = nextLRU;
  }

  @Override
  public LRUClockNode nextLRUNode() {
    return this.nextLRU;
  }

  @Override
  public void setPrevLRUNode(final LRUClockNode previousLRU) {
    this.previousLRU = previousLRU;
  }

  @Override
  public LRUClockNode prevLRUNode() {
    return this.previousLRU;
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



    // inline keys always report null for sizing since the size comes from the entry size
    return null;

  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // stats code

  @Override
  public void updateStatsForGet(final boolean isHit, final long time) {
    setLastAccessed(time);
    if (isHit) {
      incrementHitCount();
    } else {
      incrementMissCount();
    }
  }

  @Override
  protected void setLastModifiedAndAccessedTimes(final long lastModified, final long lastAccessed) {
    setLastModified(lastModified);
    if (!DISABLE_ACCESS_TIME_UPDATE_ON_PUT) {
      setLastAccessed(lastAccessed);
    }
  }

  private volatile long lastAccessed;
  private volatile int hitCount;
  private volatile int missCount;

  private static final AtomicIntegerFieldUpdater<VMStatsLRURegionEntryOffHeapStringKey1> hitCountUpdater =
      AtomicIntegerFieldUpdater.newUpdater(VMStatsLRURegionEntryOffHeapStringKey1.class,
          "hitCount");

  private static final AtomicIntegerFieldUpdater<VMStatsLRURegionEntryOffHeapStringKey1> missCountUpdater =
      AtomicIntegerFieldUpdater.newUpdater(VMStatsLRURegionEntryOffHeapStringKey1.class,
          "missCount");

  @Override
  public long getLastAccessed() throws InternalStatisticsDisabledException {
    return this.lastAccessed;
  }

  @Override
  public void setLastAccessed(finallong lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  @Override
  public long getHitCount() throws InternalStatisticsDisabledException {
    return this.hitCount & 0xFFFFFFFFL;
  }

  @Override
  public long getMissCount() throws InternalStatisticsDisabledException {
    return this.missCount & 0xFFFFFFFFL;
  }

  private void incrementHitCount() {
    hitCountUpdater.incrementAndGet(this);
  }

  private void incrementMissCount() {
    missCountUpdater.incrementAndGet(this);
  }

  @Override
  public void resetCounts() throws InternalStatisticsDisabledException {
    hitCountUpdater.set(this, 0);
    missCountUpdater.set(this, 0);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public void txDidDestroy(long timeStamp) {
    setLastModified(timeStamp);
    setLastAccessed(timeStamp);
    this.hitCount = 0;
    this.missCount = 0;
  }

  @Override
  public boolean hasStats() {
    return true;
  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  // key code


  private final long bits1;

  private int getKeyLength() {
    return (int) (this.bits1 & 0x003fL);
  }

  private int getEncoding() {
    // 0 means encoded as char
    // 1 means encoded as bytes that are all <= 0x7f;
    return (int) (this.bits1 >> 6) & 0x03;
  }

  @Override
  public Object getKey() {
    int keyLength = getKeyLength();
    char[] chars = new char[keyLength];
    long tempBits1 = this.bits1;

    if (getEncoding() == 1) {
      for (int i = 0; i < keyLength; i++) {
        tempBits1 >>= 8;
        chars[i] = (char) (tempBits1 & 0x00ff);
      }
    } else {
      for (int i = 0; i < keyLength; i++) {
        tempBits1 >>= 16;
        chars[i] = (char) (tempBits1 & 0x00FFff);
      }
    }

    return new String(chars);
  }

  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp

  @Override
  public boolean isKeyEqual(final Object key) {
    if (key instanceof String) {
      String stringKey = (String) key;
      int keyLength = getKeyLength();
      if (stringKey.length() == keyLength) {
        long tempBits1 = this.bits1;

        if (getEncoding() == 1) {
          for (int i = 0; i < keyLength; i++) {
            tempBits1 >>= 8;
            char character = (char) (tempBits1 & 0x00ff);
            if (stringKey.charAt(i) != character) {
              return false;
            }
          }

        } else {
          for (int i = 0; i < keyLength; i++) {
            tempBits1 >>= 16;
            char character = (char) (tempBits1 & 0x00FFff);
            if (stringKey.charAt(i) != character) {
              return false;
            }
          }
        }

        return true;
      }
    }
    return false;
  }



  // DO NOT modify this class. It was generated from LeafRegionEntry.cpp
}

