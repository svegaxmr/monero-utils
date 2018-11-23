package com.svega.moneroutils.crypto.slowhash

import java.nio.ByteOrder

@ExperimentalUnsignedTypes
abstract class Scratchpad(val size: Int) {
    abstract operator fun plus(off: Int): UBytePointer
    abstract operator fun get(i: Int, size: Int): UByteArray
    abstract operator fun set(i: Int, arr: UByteArray)
    abstract operator fun set(i: Int, b: UByte)
    abstract operator fun get(i: Int): UByte
    abstract fun getPointer(i: Int = 0): UBytePointer
    abstract fun getPointer(i: Int, s: Int): UBytePointer
    abstract fun getSwappedUInt(i: Int): UInt
    abstract fun setSwappedUInt(offset: Int, value: UInt)
    abstract fun getSwappedULong(i: Int): ULong
    abstract fun setSwappedULong(offset: Int, value: ULong)
    abstract fun getRawArray(): UByteArray
    companion object {
        @JvmStatic
        fun getScratchpad(size: Int) : Scratchpad{
            if((size == 0) or (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN))
                return UByteArrayScratchpad(size)
            return ByteBufferScratchpad(size)
        }
        @JvmStatic
        fun wrap(data: UByteArray): Scratchpad{
            if(data.isEmpty() or (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN))
                return UByteArrayScratchpad(data)
            return ByteBufferScratchpad(data)
        }
    }
}