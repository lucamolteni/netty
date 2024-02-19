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

import io.netty.microbench.util.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
public class ArrayFillBenchmark extends AbstractMicrobenchmark {


    public short[] data;

    @Setup(Level.Trial)
    public void setup() {
        data = new short[2 ^ 14];
    }

    @Benchmark
    public short[] benchmarkFastThreadLocalArrayFill() {
        Arrays.fill(data, (short) 0);
        return data;
    }


    @Benchmark
    public short[] benchmarkFastThreadLocalSpecialFill() {
        int len = data.length;

        if (len > 0){
            data[0] = (short) 0;
        }

        //Value of i will be [1, 2, 4, 8, 16, 32, ..., len]
        for (int i = 1; i < len; i += i) {
            System.arraycopy(data, 0, data, i, Math.min((len - i), i));
        }
        return data;
    }

}
