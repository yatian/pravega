/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emc.pravega.service.server.logs.operations;

import com.emc.pravega.service.server.logs.SerializationException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Log Operation that indicates a Batch StreamSegment is merged into its parent StreamSegment.
 */
public class MergeBatchOperation extends StorageOperation {
    //region Members

    public static final byte OPERATION_TYPE = 3;
    private static final byte VERSION = 0;
    private long streamSegmentOffset;
    private long length;
    private long batchStreamSegmentId;

    //endregion

    //region Constructor

    /**
     * Creates a new instance of the MergeBatchOperation class.
     *
     * @param streamSegmentId      The Id of the Parent StreamSegment (the StreamSegment to merge into).
     * @param batchStreamSegmentId The Id of the Batch StreamSegment (the StreamSegment to be merged).
     */
    public MergeBatchOperation(long streamSegmentId, long batchStreamSegmentId) {
        super(streamSegmentId);
        this.batchStreamSegmentId = batchStreamSegmentId;
        this.length = -1;
        this.streamSegmentOffset = -1;
    }

    protected MergeBatchOperation(OperationHeader header, DataInputStream source) throws SerializationException {
        super(header, source);
    }

    //endregion

    //region MergeBatchOperation Properties

    /**
     * Gets a value indicating the Id of the Batch StreamSegment (the StreamSegment to be merged).
     *
     * @return The Id.
     */
    public long getBatchStreamSegmentId() {
        return this.batchStreamSegmentId;
    }

    /**
     * Sets the length of the Batch StreamSegment.
     *
     * @param value The length.
     */
    public void setLength(long value) {
        // No need for parameter validation here. We will check for them upon serialization.
        this.length = value;
    }

    /**
     * Sets the offset of the Target StreamSegment to merge at.
     *
     * @param value The offset.
     */
    public void setStreamSegmentOffset(long value) {
        // No need for parameter validation here. We will check for them upon serialization.
        this.streamSegmentOffset = value;
    }

    //endregion

    //region Operation Implementation

    /**
     * Gets a value indicating the Offset in the Target StreamSegment to merge at.
     *
     * @return The offset.
     */
    @Override
    public long getStreamSegmentOffset() {
        return this.streamSegmentOffset;
    }

    /**
     * Gets a value indicating the Length of the Batch StreamSegment.
     *
     * @return The length.
     */
    @Override
    public long getLength() {
        return this.length;
    }

    @Override
    protected byte getOperationType() {
        return OPERATION_TYPE;
    }

    @Override
    protected void serializeContent(DataOutputStream target) throws IOException {
        ensureSerializationCondition(this.length >= 0, "Batch StreamSegment Length has not been assigned for this entry.");
        ensureSerializationCondition(this.streamSegmentOffset >= 0, "Target StreamSegment Offset has not been assigned for this entry.");

        target.writeByte(VERSION);
        target.writeLong(getStreamSegmentId());
        target.writeLong(this.batchStreamSegmentId);
        target.writeLong(this.length);
        target.writeLong(this.streamSegmentOffset);
    }

    @Override
    protected void deserializeContent(DataInputStream source) throws IOException, SerializationException {
        byte version = readVersion(source, VERSION);
        setStreamSegmentId(source.readLong());
        this.batchStreamSegmentId = source.readLong();
        this.length = source.readLong();
        this.streamSegmentOffset = source.readLong();
    }

    @Override
    public String toString() {
        return String.format(
                "%s, StreamSegmentId = %d, Length = %s, ParentOffset = %s",
                super.toString(),
                getBatchStreamSegmentId(),
                toString(getLength(), -1),
                toString(getStreamSegmentOffset(), -1));
    }

    //endregion
}