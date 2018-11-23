package com.svega.moneroutils.crypto.slowhash

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.LongBuffer

/**
 * Aligned accesses, but faster
 */
@ExperimentalUnsignedTypes
class ByteBufferScratchpad(data: UByteArray): Scratchpad(data.size) {
    constructor(size: Int): this(UByteArray(size))
    private val ppad: UByteArray = data
    private val pad: ByteBuffer = ByteBuffer.wrap(data.asByteArray())
    private val ipad: IntBuffer
    private val lpad: LongBuffer
    init{
        pad.order(ByteOrder.LITTLE_ENDIAN)
        ipad = pad.asIntBuffer()
        lpad = pad.asLongBuffer()
    }
    override fun getRawArray() = ppad

    override operator fun plus(off: Int): UBytePointer{
        return UBytePointer(this, off)
    }

    override operator fun get(i: Int, size: Int): UByteArray{
        //val ret = UByteArray(size)
        //ppad.copyInto(destination = ret, startIndex = i, endIndex = i + size)

        return ppad.copyOfRange(i, i + size)
    }

    override operator fun set(i: Int, arr: UByteArray){
        arr.copyInto(destination = ppad, destinationOffset = i)
    }

    override operator fun set(i: Int, b: UByte){
        ppad[i] = b
    }

    override operator fun get(i: Int): UByte{
        return ppad[i]
    }

    override fun getPointer(i: Int): UBytePointer {
        return UBytePointer(this, i, size - i)
    }

    override fun getPointer(i: Int, s: Int): UBytePointer {
        return UBytePointer(this, i, s)
    }

    override fun getSwappedUInt(i: Int): UInt = ipad.get(i / 4).toUInt()

    override fun setSwappedUInt(offset: Int, value: UInt) {
        ipad.put(offset / 4, value.toInt())
    }

    override fun getSwappedULong(i: Int): ULong = lpad.get(i / 8).toULong()

    override fun setSwappedULong(offset: Int, value: ULong) {
        lpad.put(offset / 8, value.toLong())
    }
}