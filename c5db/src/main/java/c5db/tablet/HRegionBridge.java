/*
 * Copyright (C) 2014  Ohm Data
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
 */
package c5db.tablet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.wal.HLog;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Bridge between the (complex) HRegion and the rest of c5.
 *
 * Provides an abstraction and test point, and lessons in how to abstract
 * and extract HRegion functionality.
 */
public class HRegionBridge implements IRegion {

  public static class HRegionBridgeCreator implements IRegion.Creator {

    @Override
    public IRegion getHRegion(Path basePath,
                              HRegionInfo regionInfo,
                              HTableDescriptor tableDescriptor,
                              HLog log,
                              Configuration conf) throws IOException {
      return new HRegionBridge(HRegion.openHRegion(
          new org.apache.hadoop.fs.Path(basePath.toString()),
          regionInfo,
          tableDescriptor,
          log,
          conf,
          null, null));
    }
  }


  final HRegion theRegion;

  public HRegionBridge(final HRegion theRegion) {
    this.theRegion = theRegion;
  }
}