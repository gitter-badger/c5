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
package ohmdb.flease;

/**
 * A ballot number in the flease system.  Unwraps and serializes to protobufs.
 * Implements comparable.
 *
 * This object is immutable.
 */
public class BallotNumber implements Comparable<BallotNumber> {

    // normally 'time_t' in milliseconds
    private final long ballotNumber;
    private final long messageNumber;
    private final long processId;
    // maybe stashed
    private final Flease.BallotNumber protobuf;

    public BallotNumber(long ballotNumber, long messageNumber, long processId) {
        this.ballotNumber = ballotNumber;
        this.messageNumber = messageNumber;
        this.processId = processId;
        this.protobuf = null;
    }

    public BallotNumber(Flease.BallotNumber fromMessage) {
        this.ballotNumber = fromMessage.getBallotNumber();
        this.messageNumber = fromMessage.getMessageNumber();
        this.processId = fromMessage.getId();
        this.protobuf = fromMessage;
    }

    public Flease.BallotNumber getMessage() {
        // Thanks to immutable objects, lets do this.
        if (protobuf != null) return protobuf;

        Flease.BallotNumber.Builder builder = Flease.BallotNumber.newBuilder();
        builder.setBallotNumber(ballotNumber)
                .setMessageNumber(messageNumber)
                .setId(processId);
        return builder.build();
    }

    /**
     * Create a new BallotNumber but with an updated message number.
     * @param newMessageNumber new object.
     * @return
     */
    public BallotNumber updateNumber(long newMessageNumber) {
        return new BallotNumber(ballotNumber, newMessageNumber, processId);
    }

    @Override
    public String toString() {
        return ballotNumber + "/" + messageNumber + "@" + processId;
    }

    @Override
    public int compareTo(BallotNumber o) {
        if (o == null) {
            // this.compareTo(null) => this > null, we are greater than.
            return 1;
        }

        // TODO compare using interval, message number and processId.
        if (ballotNumber == o.ballotNumber)
            return 0;
        else if (ballotNumber > o.ballotNumber)
            return 1;
        else
            return -1;
    }

    @Override
    public int hashCode() {
        return ((int)(ballotNumber ^ (ballotNumber >>> 32)))
                ^ ((int)(messageNumber ^ (messageNumber >>> 32)))
                ^ ((int)(processId ^ (processId >>> 32)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;

        if (other instanceof BallotNumber) {
            BallotNumber bOther = (BallotNumber)other;

            if (ballotNumber == bOther.ballotNumber
                    && messageNumber == bOther.messageNumber
                    && processId == bOther.processId)
                return true;

        }
        return false;
    }
}
