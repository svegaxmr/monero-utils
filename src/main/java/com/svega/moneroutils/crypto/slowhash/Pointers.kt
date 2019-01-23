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

@ExperimentalUnsignedTypes
open class Pointer<T>(protected val scratchpad: Scratchpad, var offset: Int) {
    private val size: Int
        get(): Int {
            return if (isize != -1) isize else scratchpad.size - offset
        }
    private var isize = -1

    constructor(scratchpad: Scratchpad, offset: Int, size: Int) :
            this(scratchpad, offset) {
        this.isize = size
    }

    protected open val STRIDE = 1
    operator fun plus(i: Int) = Pointer<T>(scratchpad, offset + (i * STRIDE), size() - (i * STRIDE))
    operator fun times(i: Int) = Pointer<T>(scratchpad, offset * (i * STRIDE), size() - (i * STRIDE))
    operator fun minus(i: Int) = Pointer<T>(scratchpad, offset - (i * STRIDE), size() - (i * STRIDE))

    fun toUBytePointer() = UBytePointer(scratchpad, offset)
    fun toUIntPointer() = UIntPointer(scratchpad, offset)
    fun toULongPointer() = ULongPointer(scratchpad, offset)

    open fun size(): Int = size

    companion object {
        val NULL_POINTER = UByteArrayScratchpad(0).getPointer(0)
    }
}

@ExperimentalUnsignedTypes
class UBytePointer(scratchpad: Scratchpad, offset: Int, size: Int = -1) : Pointer<UByte>(scratchpad, offset, size) {
    override val STRIDE = 1
    operator fun get(off: Int): UByte {
        return scratchpad[offset + off]
    }

    operator fun set(c: Int, value: UByte) {
        scratchpad[offset + c] = value
    }

    operator fun get(off: Int, size: Int): UByteArray {
        return scratchpad[offset + off, size]
    }

    operator fun set(off: Int, arr: UByteArray) {
        scratchpad[offset + off] = arr
    }
}

@ExperimentalUnsignedTypes
class UIntPointer(scratchpad: Scratchpad, offset: Int) : Pointer<UInt>(scratchpad, offset) {
    override val STRIDE = 4
    operator fun get(off: Int): UInt {
        return scratchpad.getSwappedUInt(offset + (off * STRIDE))
    }

    operator fun set(c: Int, value: UInt) {
        scratchpad.setSwappedUInt(offset + (c * STRIDE), value)
    }

    operator fun get(off: Int, size: Int): UIntArray {
        return scratchpad.getSwappedUInt(offset + (off * STRIDE), size)
    }

    operator fun set(off: Int, arr: UIntArray) {
        scratchpad.setSwappedUInt(offset + (off * STRIDE), arr)
    }
}

@ExperimentalUnsignedTypes
class ULongPointer(scratchpad: Scratchpad, offset: Int) : Pointer<ULong>(scratchpad, offset) {
    override val STRIDE = 8
    operator fun get(off: Int): ULong {
        return scratchpad.getSwappedULong(offset + (off * STRIDE))
    }

    operator fun set(c: Int, value: ULong) {
        scratchpad.setSwappedULong(offset + (c * STRIDE), value)
    }
}