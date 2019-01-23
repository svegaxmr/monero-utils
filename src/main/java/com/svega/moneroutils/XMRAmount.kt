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
    operator fun plus(other: XMRAmount): XMRAmount {
        return XMRAmount(atomicUnits + other.atomicUnits)
    }

    operator fun minus(other: XMRAmount): XMRAmount {
        return XMRAmount(atomicUnits - other.atomicUnits)
    }

    fun toString(division: AmountDivision): String {
        return "${atomicUnits / division.multiplier} ${division.prefix}XMR"
    }

    override fun toString() = toString(AmountDivision.WHOLE)
}

enum class AmountDivision(val prefix: String, val multiplier: Double) {
    ATOMIC("p", 1e0),
    NANO("n", 1e3),
    MICRO("u", 1e6),
    MILLI("m", 1e9),
    WHOLE("", 1e12)
}