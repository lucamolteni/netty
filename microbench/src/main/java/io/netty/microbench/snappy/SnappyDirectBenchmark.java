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
import java.util.Random;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class SnappyDirectBenchmark extends AbstractMicrobenchmark {

    @Param({"FAST_THREAD_LOCAL_ARRAY_FILL", "FAST_THREAD_LOCAL_ARRAY_FILL_OLD", "NEW_ARRAY"})
    public Snappy.HashType hashType;
    private ByteBuf buffer;
    private Snappy snappy;
    private ByteBuf in;
    private ByteBuf out;

    @Setup(Level.Iteration)
    public void setup() throws UnsupportedEncodingException {
        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        int bufferSizeInBytes = 16383; // 100 bytes
        buffer = allocator.buffer(bufferSizeInBytes);

        snappy = new Snappy();
        snappy.setHashType(hashType);

        Random random = new Random(5323211032315942961L);
        byte[] randomBytes = new byte[buffer.writableBytes()];
        random.nextBytes(randomBytes);
        buffer.writeBytes(randomBytes);

        in = Unpooled.wrappedBuffer(randomBytes);
        out = Unpooled.buffer();
    }

    @TearDown(Level.Trial)
    public void teardown() {
        buffer.release();
        buffer = null;
        out.release();
        out = null;
    }

    @Benchmark
    public ByteBuf encode() {
        // Make it run inside a FastThread
        snappy.encode(in, out, in.readableBytes());
        return out;
    }
}
