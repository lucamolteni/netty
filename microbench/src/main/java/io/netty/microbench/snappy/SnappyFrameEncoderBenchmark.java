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
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.Snappy;
import io.netty.handler.codec.compression.SnappyFrameEncoder;
import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.Random;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class SnappyFrameEncoderBenchmark extends AbstractMicrobenchmark {
    private EmbeddedChannel embeddedChannel;

    @Param({"FAST_THREAD_LOCAL_ARRAY_FILL", "NEW_ARRAY", "FAST_THREAD_LOCAL_SPECIAL_FILL"})
    public Snappy.HashType hashType;

    @Param({"true"})
    public boolean jumboPackets;

    @Param({"100000"})
    public int contentSize;
    private ByteBuf buffer;

    @Setup(Level.Iteration)
    public void setup() {
        ByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        int bufferSizeInBytes = 100 * 1024 * 1024; // 100 MB
        buffer = allocator.buffer(bufferSizeInBytes);

        Random random = new Random(5323211032315942961L);
        byte[] randomBytes = new byte[buffer.writableBytes()];
        random.nextBytes(randomBytes);
        buffer.writeBytes(randomBytes);

        SnappyFrameEncoder encoder;
        if (jumboPackets) {
            encoder = SnappyFrameEncoder.snappyEncoderWithJumboFrames();
        } else {
            encoder = new SnappyFrameEncoder();
        }
        encoder.setHashType(hashType);

        embeddedChannel = new EmbeddedChannel(encoder);
    }

    @TearDown(Level.Trial)
    public void teardown() {
        buffer.release();
        buffer = null;
    }

    @Benchmark
    public void writeArray() {
        embeddedChannel.writeOutbound(buffer.retain());
    }
}
