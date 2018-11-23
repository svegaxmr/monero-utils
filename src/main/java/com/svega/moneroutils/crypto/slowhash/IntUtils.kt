package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object IntUtils {
    fun hi_dword(value: ULong): ULong {
        return value shr 32
    }

    fun lo_dword(value: ULong): ULong {
        return value and 0xFFFFFFFFu
    }
    fun mul128(mult: ULong, cand: ULong): Pair<ULong, ULong> {

        val x0 = lo_dword(mult)
        val x1 = hi_dword(mult)
        val y0 = lo_dword(cand)
        val y1 = hi_dword(cand)
        val p11 = x1 * y1
        val p01 = x0 * y1
        val p10 = x1 * y0
        val p00 = x0 * y0
        // 64-bit product + two 32-bit values
        val middle = p10 + hi_dword(p00) + lo_dword(p01)
        val r_hi = p11 + hi_dword(middle) + hi_dword(p01)

        // Add LOW PART and lower half of MIDDLE PART
        return Pair(r_hi, (middle shl 32) or lo_dword(p00))
    }
}