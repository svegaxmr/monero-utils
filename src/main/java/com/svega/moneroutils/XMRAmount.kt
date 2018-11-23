package com.svega.moneroutils

class XMRAmount(amount: Long) {
    var atomicUnits = amount
        private set
    companion object {
        @JvmStatic
        fun fromOther(inUnits: Double, division: AmountDivision) = XMRAmount((inUnits * division.multiplier).toLong())
        @JvmStatic
        fun toOther(amount: XMRAmount, newDivision: AmountDivision) = (amount.atomicUnits / newDivision.multiplier)
    }
    fun toOther(newDivision: AmountDivision) = (atomicUnits / newDivision.multiplier)
    operator fun plus(other: XMRAmount): XMRAmount{
        return XMRAmount(atomicUnits + other.atomicUnits)
    }
    operator fun minus(other: XMRAmount): XMRAmount{
        return XMRAmount(atomicUnits - other.atomicUnits)
    }

    fun toString(division: AmountDivision): String {
        return "${atomicUnits / division.multiplier} ${division.prefix}XMR"
    }

    override fun toString() = toString(AmountDivision.WHOLE)
}

enum class AmountDivision(val prefix: String, val multiplier: Double){
    ATOMIC("p", 1e0),
    NANO("n", 1e3),
    MICRO("u", 1e6),
    MILLI("m", 1e9),
    WHOLE("", 1e12)
}