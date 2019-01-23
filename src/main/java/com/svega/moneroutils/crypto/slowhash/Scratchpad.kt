/*
 * Copyright (c) 2018-2019, Sergio Vega
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.svega.moneroutils.crypto.slowhash

import java.nio.ByteOrder

@ExperimentalUnsignedTypes
abstract class Scratchpad(val size: Int) : AutoCloseable {
    abstract operator fun plus(off: Int): UBytePointer
    abstract operator fun get(i: Int, size: Int): UByteArray
    abstract operator fun set(i: Int, arr: UByteArray)
    abstract operator fun set(i: Int, b: UByte)
    abstract operator fun get(i: Int): UByte
    abstract fun getPointer(i: Int = 0): UBytePointer
    abstract fun getPointer(i: Int, s: Int): UBytePointer
    abstract fun getSwappedUInt(i: Int): UInt
    abstract fun setSwappedUInt(offset: Int, value: UInt)
    abstract fun getSwappedUInt(i: Int, size: Int): UIntArray
    abstract fun setSwappedUInt(offset: Int, value: UIntArray)
    abstract fun getSwappedULong(i: Int): ULong
    abstract fun setSwappedULong(offset: Int, value: ULong)
    abstract fun getSwappedULong(i: Int, size: Int): ULongArray
    abstract fun setSwappedULong(offset: Int, value: ULongArray)
    abstract fun getRawArray(): UByteArray
    override fun close() {}

    companion object {
        @JvmStatic
        fun getScratchpad(size: Int): Scratchpad {
            if ((size == 0) or (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN))
                return UByteArrayScratchpad(size)
            return ByteBufferScratchpad(size)
        }

        @JvmStatic
        fun wrap(data: UByteArray): Scratchpad {
            if (data.isEmpty() or (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN))
                return UByteArrayScratchpad(data)
            return ByteBufferScratchpad(data)
        }

        @JvmStatic
        fun getUnbackedScratchpad(memory: Int): Scratchpad {
            if ((memory == 0) or (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN))
                return UByteArrayScratchpad(memory)
            if (System.getProperty("os.name").contains("windows", ignoreCase = true) and
                    System.getProperty("os.arch").contains("64", ignoreCase = true) and
                    isClass("org.lwjgl.system.MemoryUtil"))
                return WindowsX64UnbackedAlignedScratchpad(memory)
            return getScratchpad(memory)
        }

        private fun isClass(className: String): Boolean {
            return try {
                Class.forName(className)
                true
            } catch (e: ClassNotFoundException) {
                false
            }

        }
    }
}