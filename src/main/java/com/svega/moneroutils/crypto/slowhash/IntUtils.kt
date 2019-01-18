package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object IntUtils {
    fun hiDword(value: ULong): ULong {
        return value shr 32
    }

    fun loDword(value: ULong): ULong {
        return value and 0xFFFFFFFFu
    }

    fun mul128(mult: ULong, cand: ULong): Pair<ULong, ULong> {

        val x0 = loDword(mult)
        val x1 = hiDword(mult)
        val y0 = loDword(cand)
        val y1 = hiDword(cand)
        val p11 = x1 * y1
        val p01 = x0 * y1
        val p10 = x1 * y0
        val p00 = x0 * y0
        // 64-bit product + two 32-bit values
        val middle = p10 + hiDword(p00) + loDword(p01)
        val r_hi = p11 + hiDword(middle) + hiDword(p01)

        // Add LOW PART and lower half of MIDDLE PART
        return Pair(r_hi, (middle shl 32) or loDword(p00))
    }
}