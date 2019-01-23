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

import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.LongBuffer

@ExperimentalUnsignedTypes
class WindowsX64UnbackedAlignedScratchpad(size: Int) : Scratchpad(size) {
    private val pad: ByteBuffer = MemoryUtil.memAlignedAlloc(256, size)
    private val ipad: IntBuffer
    private val lpad: LongBuffer

    init {
        pad.order(ByteOrder.LITTLE_ENDIAN)
        ipad = pad.asIntBuffer()
        lpad = pad.asLongBuffer()
    }

    override fun close() {
        MemoryUtil.memAlignedFree(pad)
    }

    override fun getRawArray() = throw RuntimeException("Speed, baby")

    override operator fun plus(off: Int): UBytePointer {
        return UBytePointer(this, off)
    }

    override operator fun get(i: Int, size: Int): UByteArray {
        val ret = UByteArray(size)
        pad.get(ret.asByteArray(), i, size)
        return ret
    }

    override operator fun set(i: Int, arr: UByteArray) {
        pad.position(i)
        pad.put(arr.asByteArray())
    }

    override operator fun set(i: Int, b: UByte) {
        pad.put(i, b.toByte())
    }

    override operator fun get(i: Int): UByte {
        return pad.get(i).toUByte()
    }

    override fun getPointer(i: Int): UBytePointer {
        return UBytePointer(this, i, size - i)
    }

    override fun getPointer(i: Int, s: Int): UBytePointer {
        return UBytePointer(this, i, s)
    }

    override fun getSwappedUInt(i: Int): UInt = ipad.get(i / 4).toUInt()

    override fun getSwappedUInt(i: Int, size: Int): UIntArray {
        val ret = UIntArray(size)
        ipad.get(ret.asIntArray(), i / 4, size)
        return ret
    }

    override fun setSwappedUInt(offset: Int, value: UInt) {
        ipad.put(offset / 4, value.toInt())
    }

    override fun setSwappedUInt(offset: Int, value: UIntArray) {
        ipad.put(value.asIntArray(), offset / 4, value.size)
    }

    override fun getSwappedULong(i: Int): ULong = lpad.get(i / 8).toULong()

    override fun setSwappedULong(offset: Int, value: ULong) {
        lpad.put(offset / 8, value.toLong())
    }

    override fun getSwappedULong(i: Int, size: Int): ULongArray {
        val ret = ULongArray(size)
        lpad.get(ret.asLongArray(), i / 8, size)
        return ret
    }

    override fun setSwappedULong(offset: Int, value: ULongArray) {
        lpad.put(value.asLongArray(), offset / 8, value.size)
    }
}