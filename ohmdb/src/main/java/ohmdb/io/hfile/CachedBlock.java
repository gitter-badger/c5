/*
 * Copyright (C) 2013  Ohm Data
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  This file incorporates work covered by the following copyright and
 *  permission notice:
 */

/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ohmdb.io.hfile;

import ohmdb.io.HeapSize;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.ClassSize;

/**
 * Represents an entry in the {@link LruBlockCache}.
 *
 * <p>Makes the block memory-aware with {@link HeapSize} and Comparable
 * to sort by access time for the LRU.  It also takes care of priority by
 * either instantiating as in-memory or handling the transition from single
 * to multiple access.
 */
public class CachedBlock implements HeapSize, Comparable<CachedBlock> {

  public final static long PER_BLOCK_OVERHEAD = ClassSize.align(
    ClassSize.OBJECT + (3 * ClassSize.REFERENCE) + (2 * Bytes.SIZEOF_LONG) +
    ClassSize.STRING + ClassSize.BYTE_BUFFER);

  static enum BlockPriority {
    /**
     * Accessed a single time (used for scan-resistance)
     */
    SINGLE,
    /**
     * Accessed multiple times
     */
    MULTI,
    /**
     * Block from in-memory store
     */
    MEMORY
  };

  private final BlockCacheKey cacheKey;
  private final Cacheable buf;
  private volatile long accessTime;
  private long size;
  private BlockPriority priority;

  public CachedBlock(BlockCacheKey cacheKey, Cacheable buf, long accessTime) {
    this(cacheKey, buf, accessTime, false);
  }

  public CachedBlock(BlockCacheKey cacheKey, Cacheable buf, long accessTime,
      boolean inMemory) {
    this.cacheKey = cacheKey;
    this.buf = buf;
    this.accessTime = accessTime;
    // We approximate the size of this class by the size of its name string
    // plus the size of its byte buffer plus the overhead associated with all
    // the base classes. We also include the base class
    // sizes in the PER_BLOCK_OVERHEAD variable rather than align()ing them with
    // their buffer lengths. This variable is used elsewhere in unit tests.
    this.size = ClassSize.align(cacheKey.heapSize())
        + ClassSize.align(buf.heapSize()) + PER_BLOCK_OVERHEAD;
    if(inMemory) {
      this.priority = BlockPriority.MEMORY;
    } else {
      this.priority = BlockPriority.SINGLE;
    }
  }

  /**
   * Block has been accessed.  Update its local access time.
   */
  public void access(long accessTime) {
    this.accessTime = accessTime;
    if(this.priority == BlockPriority.SINGLE) {
      this.priority = BlockPriority.MULTI;
    }
  }

  public long heapSize() {
    return size;
  }

  @Override
  public int compareTo(CachedBlock that) {
    if(this.accessTime == that.accessTime) return 0;
    return this.accessTime < that.accessTime ? 1 : -1;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    CachedBlock other = (CachedBlock) obj;
    return compareTo(other) == 0;
  }

  public Cacheable getBuffer() {
    return this.buf;
  }

  public BlockCacheKey getCacheKey() {
    return this.cacheKey;
  }

  public BlockPriority getPriority() {
    return this.priority;
  }
}
