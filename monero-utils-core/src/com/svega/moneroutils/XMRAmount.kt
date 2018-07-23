package com.svega.moneroutils

data class XMRAmount(val atomicUnits: Long) {
    companion object {
        fun fromOther(inUnits: Double, division: AmountDivision) = XMRAmount((inUnits * division.multiplier).toLong())
        fun toOther(amount: XMRAmount, newDivision: AmountDivision) = (amount.atomicUnits / newDivision.multiplier)
    }
}

enum class AmountDivision(val prefix: String, val multiplier: Double){
    ATOMIC("p", 1e0),
    NANO("n", 1e3),
    MICRO("u", 1e6),
    MILLI("m", 1e9),
    WHOLE("", 1e12)
}

infix fun XMRAmount.add(other: XMRAmount) = XMRAmount(this.atomicUnits + other.atomicUnits)