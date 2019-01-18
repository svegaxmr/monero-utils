package com.svega.moneroutils

import com.svega.moneroutils.BinHexUtils.binaryToHex
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer

fun ByteBuffer.getVarLong(): Long {
    var tmp = this.get().toLong()
    if (tmp >= 0) {
        return tmp
    }
    var result = tmp and 0x7f
    tmp = this.get().toLong()
    if (tmp >= 0) {
        result = result or (tmp shl 7)
    } else {
        result = result or (tmp and 0x7f shl 7)
        tmp = this.get().toLong()
        if (tmp >= 0) {
            result = result or (tmp shl 14)
        } else {
            result = result or (tmp and 0x7f shl 14)
            tmp = this.get().toLong()
            if (tmp >= 0) {
                result = result or (tmp shl 21)
            } else {
                result = result or (tmp and 0x7f shl 21)
                tmp = this.get().toLong()
                if (tmp >= 0) {
                    result = result or (tmp shl 28)
                } else {
                    result = result or (tmp and 0x7f shl 28)
                    tmp = this.get().toLong()
                    if (tmp >= 0) {
                        result = result or (tmp shl 35)
                    } else {
                        result = result or (tmp and 0x7f shl 35)
                        tmp = this.get().toLong()
                        if (tmp >= 0) {
                            result = result or (tmp shl 42)
                        } else {
                            result = result or (tmp and 0x7f shl 42)
                            tmp = this.get().toLong()
                            if (tmp >= 0) {
                                result = result or (tmp shl 49)
                            } else {
                                result = result or (tmp and 0x7f shl 49)
                                tmp = this.get().toLong()
                                if (tmp >= 0) {
                                    result = result or (tmp shl 56)
                                } else {
                                    result = result or (tmp and 0x7f shl 56)
                                    result = result or (this.get().toLong() shl 63)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return result
}

fun ByteBuffer.getVarInt(): Int {
    var tmp: Int = this.get().toInt()
    if ((tmp) >= 0) {
        return tmp
    }
    var result = tmp and 0x7f
    tmp = this.get().toInt()
    if (tmp >= 0) {
        result = result or (tmp shl 7)
    } else {
        result = result or (tmp and 0x7f shl 7)
        tmp = this.get().toInt()
        if (tmp >= 0) {
            result = result or (tmp shl 14)
        } else {
            result = result or (tmp and 0x7f shl 14)
            tmp = this.get().toInt()
            if ((tmp) >= 0) {
                result = result or (tmp shl 21)
            } else {
                result = result or (tmp and 0x7f shl 21)
                tmp = this.get().toInt()
                result = result or (tmp shl 28)
                while (tmp < 0) {
                    // We get into this loop only in the case of overflow.
                    // By doing this, we can call getVarInt() instead of
                    // getVarLong() when we only need an int.
                    tmp = this.get().toInt()
                }
            }
        }
    }
    return result
}

@ExperimentalUnsignedTypes
fun ByteBuffer.printNextBytes(n: Int){
    mark()
    val one = ByteArray(Math.min(n, remaining()))
    get(one)
    reset()
    println("next is ${binaryToHex(one)}")
}

fun ByteBuffer.readNewBuffer(): ByteBuffer{
    val temp = ByteArray(getVarInt())
    get(temp)
    return ByteBuffer.wrap(temp)
}

fun DataOutputStream.writeVarInt(v_: Int){
    var v = v_
    while (true) {
        val bits = v and 0x7f
        v = v ushr 7
        if (v == 0) {
            write(bits)
            return
        }
        write(bits or 0x80)
    }
}

fun DataOutputStream.writeVarLong(v_: Long){
    var v = v_
    while (true) {
        val bits = v.toInt() and 0x7f
        v = v ushr 7
        if (v == 0L) {
            write(bits)
            return
        }
        write(bits or 0x80)
    }
}

fun Int.varIntData(): ByteArray {
    val b = ByteArrayOutputStream()
    var v = this
    while (true) {
        val bits = v and 0x7f
        v = v ushr 7
        if (v == 0) {
            b.write(bits)
            return b.toByteArray()
        }
        b.write(bits or 0x80)
    }
}

fun SWAP32(x: Int) = ((((x) and 0x000000ff) shl 24) or (((x) and 0x0000ff00) shl 8)
        or (((x) and 0x00ff0000) shr 8) or (((x.toLong()) and 0xff000000L) ushr 24).toInt())

@ExperimentalUnsignedTypes
fun SWAP32(x: UInt): UInt = ((( (x) and 0x000000ffu) shl 24) or (( (x) and 0x0000ff00u) shl  8)
        or (( (x) and 0x00ff0000u) shr  8) or (( (x) and 0xff000000u) shr 24))

fun SWAP64(x: Long) : Long = ((( (x) and 0x00000000000000ff) shl 56) or  (( (x) and 0x000000000000ff00) shl 40) or  (( (x) and 0x0000000000ff0000) shl 24) or  (( (x) and 0x00000000ff000000) shl  8) or  (( (x) and 0x000000ff00000000) ushr  8) or  (( (x) and 0x0000ff0000000000) ushr 24) or  (( (x) and 0x00ff000000000000) ushr 40) or  (( (x.toBigInteger()).and(BigInteger("ff00000000000000", 16))).toLong() ushr 56))

@ExperimentalUnsignedTypes
fun SWAP64(x: ULong) : ULong = ((( (x) and 0x00000000000000ffu) shl 56) or  (( (x) and 0x000000000000ff00u) shl 40) or  (( (x) and 0x0000000000ff0000u) shl 24) or  (( (x) and 0x00000000ff000000u) shl  8) or  (( (x) and 0x000000ff00000000u) shr  8) or  (( (x) and 0x0000ff0000000000u) shr 24) or  (( (x) and 0x00ff000000000000u) shr 40) or  (( (x) and 0xff00000000000000u) shr 56))

fun Int.toByteArray(): ByteArray {
    val ib = ByteBuffer.allocate(4)
    ib.asIntBuffer().put(this)
    return ib.flip().array()
}

@ExperimentalUnsignedTypes
fun ULong.toDouble(): Double{
    return (this and (1uL shl 63).inv()).toLong().toDouble() + if((this and (1uL shl 63)) != 0uL) 9223372036854775808.0 else 0.0
}

@ExperimentalUnsignedTypes
fun ByteBuffer.getSwappedULong(longOffset: Int): ULong{
    val i = longOffset * 8
    return this[i].toULong() or
            (this[i + 1].toULong() shl 8) or
            (this[i + 2].toULong() shl 16) or
            (this[i + 3].toULong() shl 24) or
            (this[i + 4].toULong() shl 32) or
            (this[i + 5].toULong() shl 40) or
            (this[i + 6].toULong() shl 48) or
            (this[i + 7].toULong() shl 56)
}

@ExperimentalUnsignedTypes
fun UByteArray.getSwappedULong(longOffset: Int): ULong{
    val i = longOffset * 8
    return this[i].toULong() or
            (this[i + 1].toULong() shl 8) or
            (this[i + 2].toULong() shl 16) or
            (this[i + 3].toULong() shl 24) or
            (this[i + 4].toULong() shl 32) or
            (this[i + 5].toULong() shl 40) or
            (this[i + 6].toULong() shl 48) or
            (this[i + 7].toULong() shl 56)
}

@ExperimentalUnsignedTypes
fun UByteArray.getSwappedUInt(intOffset: Int): UInt{
    val i = intOffset * 4
    return this[i].toUInt() or
            (this[i + 1].toUInt() shl 8) or
            (this[i + 2].toUInt() shl 16) or
            (this[i + 3].toUInt() shl 24)
}

@ExperimentalUnsignedTypes
fun ULong.bits(): Int{
    var ts = this
    var bits = 0
    while(ts != 0uL){
        ++bits
        ts = ts shr 1
    }
    return bits
}

@ExperimentalUnsignedTypes
fun ULong.bytes(): Int = (bits() / 8) + if(bits() % 8 == 0) 0 else 1

@ExperimentalUnsignedTypes
fun ULong.toUByteArray(): UByteArray{
    val pad = UByteArray(8)
    pad[7] = this.toUByte()
    pad[6] = (this shr 8).toUByte()
    pad[5] = (this shr 16).toUByte()
    pad[4] = (this shr 24).toUByte()
    pad[3] = (this shr 32).toUByte()
    pad[2] = (this shr 40).toUByte()
    pad[1] = (this shr 48).toUByte()
    pad[0] = (this shr 56).toUByte()
    val ret = UByteArray(bytes().coerceAtLeast(1))
    pad.copyInto(ret, 0, 8 - ret.size, 8)
    return ret
}

@ExperimentalUnsignedTypes
infix fun UByteArray.concat(b: UByteArray): UByteArray{
    val ret = UByteArray(size + b.size)
    this.copyInto(ret)
    b.copyInto(ret, size)
    return ret
}