package com.svega.moneroutils.crypto.slowhash

/**
 * Unaligned accesses, but slower
 */
@ExperimentalUnsignedTypes
class UByteArrayScratchpad(data: UByteArray): Scratchpad(data.size) {
    override fun getRawArray() = pad

    constructor(size: Int): this(UByteArray(size))
    private val pad = data

    override operator fun plus(off: Int): UBytePointer{
        return UBytePointer(this, off)
    }

    override operator fun get(i: Int, size: Int): UByteArray{
        return pad.copyOfRange(i, i + size)
    }

    override operator fun set(i: Int, arr: UByteArray){
        arr.copyInto(destination = pad, destinationOffset = i)
    }

    override operator fun set(i: Int, b: UByte){
        pad[i] = b
    }

    override operator fun get(i: Int): UByte{
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
}

@ExperimentalUnsignedTypes
open class Pointer<T>(protected val scratchpad: Scratchpad, var offset: Int){
    private val size: Int
        get(): Int{
            return if(isize != -1) isize else scratchpad.size - offset
        }
    private var isize = -1
    constructor(scratchpad: Scratchpad, offset: Int, size: Int):
            this(scratchpad, offset){
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
class UBytePointer(scratchpad: Scratchpad, offset: Int, size: Int = -1): Pointer<UByte>(scratchpad, offset, size){
    override val STRIDE = 1
    operator fun get(off: Int): UByte {
        return scratchpad[offset + off]
    }
    operator fun set(c: Int, value: UByte) {
        scratchpad[offset + c] = value
    }
    operator fun get(off: Int, size: Int): UByteArray{
        return scratchpad[offset + off, size]
    }
    operator fun set(off: Int, arr: UByteArray){
        scratchpad[offset + off] = arr
    }
}

@ExperimentalUnsignedTypes
class UIntPointer(scratchpad: Scratchpad, offset: Int): Pointer<UInt>(scratchpad, offset){
    override val STRIDE = 4
    operator fun get(off: Int): UInt {
        return scratchpad.getSwappedUInt(offset + (off * STRIDE))
    }

    operator fun set(c: Int, value: UInt) {
        scratchpad.setSwappedUInt(offset + (c * STRIDE), value)
    }
}

@ExperimentalUnsignedTypes
class ULongPointer(scratchpad: Scratchpad, offset: Int): Pointer<ULong>(scratchpad, offset){
    override val STRIDE = 8
    operator fun get(off: Int): ULong {
        return scratchpad.getSwappedULong(offset + (off * STRIDE))
    }

    operator fun set(c: Int, value: ULong) {
        scratchpad.setSwappedULong(offset + (c * STRIDE), value)
    }
}