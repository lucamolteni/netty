/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.microbench.snappy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.compression.Snappy;
import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
public class SnappyDirectBenchmark extends AbstractMicrobenchmark {

    @Param()
    public Snappy.HashType hashType;
    private ByteBuf buffer;
    private Snappy snappy;
    private ByteBuf in;
    private ByteBuf out;

    @Param({"16383", "1024", "128"})
    private int bufferSizeInBytes;

    @AuxCounters
    @State(Scope.Thread)
    public static class AllocationMetrics {
        public long fillBytes;

        public long compressedOutputSize;

        public long encodeCount;
    }

    @Setup
    public void setup() throws UnsupportedEncodingException {
        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        buffer = allocator.buffer(bufferSizeInBytes);

        snappy = new Snappy();
        snappy.setHashType(hashType);

//        Random random = new Random(5323211032315942961L);
        byte[] pseudoRandomBytes = new byte[buffer.writableBytes()];
//        random.nextBytes(randomBytes);
        Arrays.fill(pseudoRandomBytes, (byte) 1);
        buffer.writeBytes(pseudoRandomBytes);

        in = Unpooled.wrappedBuffer(pseudoRandomBytes);
        out = Unpooled.directBuffer();
    }

    @Benchmark
    public ByteBuf encode(AllocationMetrics allocationMetrics) {
        // Make it run inside a FastThread
        int length = in.readableBytes();
        snappy.encode(in, out, length);
        allocationMetrics.fillBytes = snappy.allocatedBytes;
        in.resetReaderIndex();
        allocationMetrics.compressedOutputSize += out.readableBytes();
        out.setIndex(0, 0);

        allocationMetrics.encodeCount++;
        return out;
    }


    @TearDown(Level.Trial)
    public void teardown() {
        buffer.release();
        buffer = null;
        out.release();
        out = null;
    }
}
