/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.core.indexstore.blockletindex;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.carbondata.core.cache.Cache;
import org.apache.carbondata.core.cache.CacheProvider;
import org.apache.carbondata.core.cache.CacheType;
import org.apache.carbondata.core.datamap.DataMapDistributable;
import org.apache.carbondata.core.datamap.Segment;
import org.apache.carbondata.core.datamap.dev.DataMap;
import org.apache.carbondata.core.indexstore.TableBlockIndexUniqueIdentifier;
import org.apache.carbondata.core.memory.MemoryException;
import org.apache.carbondata.core.metadata.AbsoluteTableIdentifier;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Before;
import org.junit.Test;

public class TestBlockletDataMapFactory {

  private AbsoluteTableIdentifier absoluteTableIdentifier;

  private BlockletDataMapFactory blockletDataMapFactory;

  private TableBlockIndexUniqueIdentifier tableBlockIndexUniqueIdentifier;

  private Cache<TableBlockIndexUniqueIdentifier, DataMap> cache;

  @Before public void setUp() {
    blockletDataMapFactory = new BlockletDataMapFactory();
    blockletDataMapFactory.init(absoluteTableIdentifier, "dataMapName");
    tableBlockIndexUniqueIdentifier =
        new TableBlockIndexUniqueIdentifier("/opt/store/default/carbon_table/Fact/Part0/Segment_0",
            "0_batchno0-0-1521012756709.carbonindex", null, "0");
    cache = CacheProvider.getInstance().createCache(CacheType.DRIVER_BLOCKLET_DATAMAP);
  }

  @Test public void addDataMapToCache()
      throws IOException, MemoryException, NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    BlockletDataMap dataMap = new BlockletDataMap();
    dataMap.setTableBlockUniqueIdentifier(tableBlockIndexUniqueIdentifier);
    Method method =
        BlockletDataMapFactory.class.getDeclaredMethod("cache", DataMap.class);
    method.setAccessible(true);
    method.invoke(blockletDataMapFactory, dataMap);
    DataMap result = cache.getIfPresent(tableBlockIndexUniqueIdentifier);
    assert null != result;
  }

  @Test public void getValidDistributables() throws IOException {
    BlockletDataMapDistributable blockletDataMapDistributable = new BlockletDataMapDistributable(
        "/opt/store/default/carbon_table/Fact/Part0/Segment_0/0_batchno0-0-1521012756709.carbonindex", null);
    Segment segment = new Segment("0", null);
    blockletDataMapDistributable.setSegment(segment);
    BlockletDataMapDistributable blockletDataMapDistributable1 = new BlockletDataMapDistributable(
        "/opt/store/default/carbon_table/Fact/Part0/Segment_0/1521012756710.carbonindexmerge", null);
    blockletDataMapDistributable1.setSegment(segment);
    List<DataMapDistributable> dataMapDistributables = new ArrayList<>(2);
    dataMapDistributables.add(blockletDataMapDistributable);
    dataMapDistributables.add(blockletDataMapDistributable1);
    new MockUp<BlockletDataMapFactory>() {
      @Mock Set<TableBlockIndexUniqueIdentifier> getTableBlockIndexUniqueIdentifiers(
          Segment segment) {
        TableBlockIndexUniqueIdentifier tableBlockIndexUniqueIdentifier1 =
            new TableBlockIndexUniqueIdentifier(
                "/opt/store/default/carbon_table/Fact/Part0/Segment_0",
                "0_batchno0-0-1521012756701.carbonindex", "1521012756710.carbonindexmerge", "0");
        TableBlockIndexUniqueIdentifier tableBlockIndexUniqueIdentifier2 =
            new TableBlockIndexUniqueIdentifier(
                "/opt/store/default/carbon_table/Fact/Part0/Segment_0",
                "0_batchno0-0-1521012756702.carbonindex", "1521012756710.carbonindexmerge", "0");
        Set<TableBlockIndexUniqueIdentifier> tableBlockIndexUniqueIdentifiers = new HashSet<>(3);
        tableBlockIndexUniqueIdentifiers.add(tableBlockIndexUniqueIdentifier);
        tableBlockIndexUniqueIdentifiers.add(tableBlockIndexUniqueIdentifier1);
        tableBlockIndexUniqueIdentifiers.add(tableBlockIndexUniqueIdentifier2);
        return tableBlockIndexUniqueIdentifiers;
      }
    };
    List<DataMapDistributable> validDistributables =
        blockletDataMapFactory.getAllUncachedDistributables(dataMapDistributables);
    assert 1 == validDistributables.size();
  }
}