package com.svega.moneroutils

class UInt8 : Number, Comparable<UInt8>{
    constructor()
    constructor(new: Char){value = new}
    constructor(new: Int){value = new.toChar()}
    private var value = 0.toChar()
    override fun toByte() = value.toByte()
    override fun toChar() = value
    override fun toInt() = value.toInt()
    override fun toLong() = value.toLong()
    override fun toDouble() = value.toDouble()
    override fun toFloat() = value.toFloat()
    override fun toShort() = value.toShort()
    override fun compareTo(other: UInt8) = value.compareTo(other.value)
    override fun toString() = toInt().toString()
    fun set(new: Int){
        value = (new and 0xFF).toChar()
    }
    fun set(new: Char) = set(new.toInt())
    fun get() = value
}

fun Byte.toUInt8() = UInt8(this.toChar())
fun Char.toUInt8() = UInt8(this)
fun Int.toUInt8() = UInt8(this.toChar())
fun Long.toUInt8() = UInt8(this.toChar())
fun Double.toUInt8() = UInt8(this.toChar())
fun Float.toUInt8() = UInt8(this.toChar())
fun Short.toUInt8() = UInt8(this.toChar())

fun Array<UInt8>.asString() : String{
    val sb = StringBuilder()
    for(b in this){
        sb.append(b.toChar())
    }
    return sb.toString()
}
