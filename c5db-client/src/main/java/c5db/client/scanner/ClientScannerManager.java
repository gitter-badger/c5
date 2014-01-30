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
 */
package c5db.client.scanner;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public enum ClientScannerManager {
  INSTANCE;

  private final ConcurrentHashMap<Long, ClientScanner> scannerMap =
      new ConcurrentHashMap<>();

  public ClientScanner getOrCreate(long scannerId)
      throws IOException {
    if (!scannerMap.containsKey(scannerId)) {
      scannerMap.put(scannerId, new ClientScanner(scannerId));
    }
    return scannerMap.get(scannerId);
  }

  public boolean hasScanner(long scannerId) {
    return scannerMap.containsKey(scannerId);
  }
}