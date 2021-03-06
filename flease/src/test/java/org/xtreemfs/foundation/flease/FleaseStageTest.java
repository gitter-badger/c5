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

/*
 * Copyright (c) 2009-2010 by Bjoern Kolbeck, Zuse Institute Berlin
 *
 * Licensed under the BSD License, see LICENSE file for details.
 *
 */

package org.xtreemfs.foundation.flease;

import junit.framework.TestCase;
import org.xtreemfs.foundation.TimeSync;
import org.xtreemfs.foundation.buffer.ASCIIString;
import org.xtreemfs.foundation.flease.comm.FleaseMessage;
import org.xtreemfs.foundation.flease.proposer.FleaseException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author bjko
 */
public class FleaseStageTest extends TestCase {

    private final FleaseConfig cfg;

    public FleaseStageTest(String testName) throws FleaseException {
        super(testName);

        //Logging.start(Logging.LEVEL_WARN, Category.all);
        TimeSync.initializeLocal(50);

        cfg = new FleaseConfig(10000, 500, 500, new InetSocketAddress(12345), "localhost:12345",5);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of createTimer method, of class FleaseStage.
     */
    public void testOpenAndGetLease() throws Exception {
        final ASCIIString CELL_ID = new ASCIIString("testcell");

        final AtomicReference<Flease> result = new AtomicReference();

        FleaseStage fs = new FleaseStage(cfg, "/tmp/xtreemfs-test/", new FleaseMessageSenderInterface() {

            @Override
            public void sendMessage(FleaseMessage message, InetSocketAddress recipient) {
                //ignore me
            }
        }, true, new FleaseViewChangeListenerInterface() {

            @Override
            public void viewIdChangeEvent(ASCIIString cellId, int viewId) {
            }
        },new FleaseStatusListener() {

            @Override
            public void statusChanged(ASCIIString cellId, Flease lease) {
                // System.out.println("state change: "+cellId+" owner="+lease.getLeaseHolder());
                synchronized (result) {
                    result.set(new Flease(cellId, lease.getLeaseHolder(), lease.getLeaseTimeout_ms(),lease.getMasterEpochNumber()));
                    result.notify();
                }
            }

            @Override
            public void leaseFailed(ASCIIString cellId, FleaseException error) {
                FleaseStageTest.fail(error.toString());
            }
        }, null);

        FleaseMessage msg = new FleaseMessage(FleaseMessage.MsgType.EVENT_RESTART);
        msg.setCellId(CELL_ID);

        fs.startAndWait();

        fs.openCell(CELL_ID, new ArrayList(),false);

        synchronized(result) {
            if (result.get() == null)
                result.wait(1000);
            if (result.get() == null)
                fail("timeout!");
        }

        assertEquals(result.get().getLeaseHolder(),cfg.getIdentity());

        FleaseFuture f = fs.closeCell(CELL_ID, false);
        f.get();

        Thread.sleep(12000);

        result.set(null);

        fs.openCell(CELL_ID, new ArrayList(), false);

        synchronized(result) {
            if (result.get() == null)
                result.wait(1000);
            if (result.get() == null)
                fail("timeout!");
        }

        assertEquals(result.get().getLeaseHolder(),cfg.getIdentity());

        fs.stopAndWait();
    }


    /**
     * Test of createTimer method, of class FleaseStage.
     */
    public void testGetState() throws Exception {
        FleaseStage fs = new FleaseStage(cfg, "/tmp/xtreemfs-test/", new FleaseMessageSenderInterface() {

            @Override
            public void sendMessage(FleaseMessage message, InetSocketAddress recipient) {
                //ignore me
            }
        }, true, new FleaseViewChangeListenerInterface() {

            @Override
            public void viewIdChangeEvent(ASCIIString cellId, int viewId) {
            }
        },new FleaseStatusListener() {

            @Override
            public void statusChanged(ASCIIString cellId, Flease lease) {
            }

            @Override
            public void leaseFailed(ASCIIString cellId, FleaseException error) {
                FleaseStageTest.fail(error.toString());
            }
        }, null);

        FleaseMessage msg = new FleaseMessage(FleaseMessage.MsgType.EVENT_RESTART);
        msg.setCellId(new ASCIIString("testcell"));

        fs.startAndWait();

        FleaseFuture f = fs.openCell(new ASCIIString("testcell"), new ArrayList(), false);
        final AtomicBoolean done = new AtomicBoolean(false);
        f.get();

        Thread.sleep(100);


        Map<ASCIIString,FleaseMessage> m = fs.getLocalState();

        // for (ASCIIString cellId : m.keySet()) {
        // System.out.println("cell "+cellId+" "+m.get(cellId));
        // }

        fs.stopAndWait();
    }

    /**
     * Test of createTimer method, of class FleaseStage.
     */
    public void testCreateTimer() throws Exception {
        FleaseStage fs = new FleaseStage(cfg, "/tmp/xtreemfs-test/", new FleaseMessageSenderInterface() {

            @Override
            public void sendMessage(FleaseMessage messages, InetSocketAddress recipient) {
                //ignore me
            }
        }, true, new FleaseViewChangeListenerInterface() {

            @Override
            public void viewIdChangeEvent(ASCIIString cellId, int viewId) {
            }
        },new FleaseStatusListener() {

            @Override
            public void statusChanged(ASCIIString cellId, Flease lease) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void leaseFailed(ASCIIString cellId, FleaseException error) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }, null);

        FleaseMessage msg = new FleaseMessage(FleaseMessage.MsgType.EVENT_RESTART);
        msg.setCellId(new ASCIIString("testcell"));

        fs.startAndWait();
        fs.createTimer(msg, TimeSync.getLocalSystemTime()+20);
        Thread.sleep(100);
        fs.stopAndWait();
    }

}
