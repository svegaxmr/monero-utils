package com.svega.moneroutils.crypto.slowhash

/**
 * Unaligned accesses, but slower
 */
@ExperimentalUnsignedTypes
class UByteArrayScratchpad(data: UByteArray) : Scratchpad(data.size) {
    constructor(size: Int) : this(UByteArray(size))

    private val pad = data
    override fun getRawArray() = pad

    override operator fun plus(off: Int): UBytePointer {
        return UBytePointer(this, off)
    }

    override operator fun get(i: Int, size: Int): UByteArray {
        return pad.copyOfRange(i, i + size)
    }

    override operator fun set(i: Int, arr: UByteArray) {
        arr.copyInto(destination = pad, destinationOffset = i)
    }

    override operator fun set(i: Int, b: UByte) {
        pad[i] = b
    }

    override operator fun get(i: Int): UByte {
        return pad[i]
    }

    override fun getPointer(i: Int): UBytePointer {
        return UBytePointer(this, i, size - i)
    }

    override fun getPointer(i: Int, s: Int): UBytePointer {
        return UBytePointer(this, i, s)
    }

    override fun getSwappedUInt(i: Int): UInt =
            pad[i].toUInt() or
                    (pad[i + 1].toUInt() shl 8) or
                    (pad[i + 2].toUInt() shl 16) or
                    (pad[i + 3].toUInt() shl 24)

    override fun setSwappedUInt(offset: Int, value: UInt) {
        pad[offset] = value.toUByte()
        pad[offset + 1] = (value shr 8).toUByte()
        pad[offset + 2] = (value shr 16).toUByte()
        pad[offset + 3] = (value shr 24).toUByte()
    }

    override fun getSwappedULong(i: Int) =
            pad[i].toULong() or
                    (pad[i + 1].toULong() shl 8) or
                    (pad[i + 2].toULong() shl 16) or
                    (pad[i + 3].toULong() shl 24) or
                    (pad[i + 4].toULong() shl 32) or
                    (pad[i + 5].toULong() shl 40) or
                    (pad[i + 6].toULong() shl 48) or
                    (pad[i + 7].toULong() shl 56)

    override fun setSwappedULong(offset: Int, value: ULong) {
        pad[offset] = value.toUByte()
        pad[offset + 1] = (value shr 8).toUByte()
        pad[offset + 2] = (value shr 16).toUByte()
        pad[offset + 3] = (value shr 24).toUByte()
        pad[offset + 4] = (value shr 32).toUByte()
        pad[offset + 5] = (value shr 40).toUByte()
        pad[offset + 6] = (value shr 48).toUByte()
        pad[offset + 7] = (value shr 56).toUByte()
    }

    override fun getSwappedUInt(i: Int, size: Int): UIntArray {
        return UIntArray(size) { x -> getSwappedUInt(i + (x * 4)) }
    }

    override fun setSwappedUInt(offset: Int, value: UIntArray) {
        value.forEachIndexed { index: Int, uintVal: UInt ->
            setSwappedUInt(offset + (index * 4), uintVal)
        }
    }

    override fun getSwappedULong(i: Int, size: Int): ULongArray {
        return ULongArray(size) { x -> getSwappedULong(i + (x * 8)) }
    }

    override fun setSwappedULong(offset: Int, value: ULongArray) {
        value.forEachIndexed { index: Int, ulongVal: ULong ->
            setSwappedULong(offset + (index * 8), ulongVal)
        }
    }
}