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

package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object Blake {
    class BLKState {
        val h = UIntArray(8)
        val s = UIntArray(4)
        val t = UIntArray(2)
        var buflen = 0
        var nullt = 0
        val buf = UByteArray(64)
    }

    private fun blake256Init(S: BLKState) {
        S.h[0] = 0x6A09E667u
        S.h[1] = 0xBB67AE85u
        S.h[2] = 0x3C6EF372u
        S.h[3] = 0xA54FF53Au
        S.h[4] = 0x510E527Fu
        S.h[5] = 0x9B05688Cu
        S.h[6] = 0x1F83D9ABu
        S.h[7] = 0x5BE0CD19u
        S.nullt = 0
        S.buflen = 0
        S.t[1] = 0u
        S.t[0] = 0u
        S.s[3] = 0u
        S.s[2] = 0u
        S.s[1] = 0u
        S.s[0] = 0u
    }

    private fun ROT(x: UInt, n: Int) = (((x) shl (32 - n)) or ((x) shr (n)))

    private val sigma = Array(14) {
        when (it) {
            0 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
            1 -> intArrayOf(14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3)
            2 -> intArrayOf(11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4)
            3 -> intArrayOf(7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8)
            4 -> intArrayOf(9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13)
            5 -> intArrayOf(2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9)
            6 -> intArrayOf(12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11)
            7 -> intArrayOf(13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10)
            8 -> intArrayOf(6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5)
            9 -> intArrayOf(10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0)
            10 -> intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
            11 -> intArrayOf(14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3)
            12 -> intArrayOf(11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4)
            else -> intArrayOf(7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8)
        }
    }

    private val cst = uintArrayOf(
            0x243F6A88u, 0x85A308D3u, 0x13198A2Eu, 0x03707344u,
            0xA4093822u, 0x299F31D0u, 0x082EFA98u, 0xEC4E6C89u,
            0x452821E6u, 0x38D01377u, 0xBE5466CFu, 0x34E90C6Cu,
            0xC0AC29B7u, 0xC97C50DDu, 0x3F84D5B5u, 0xB5470917u
    )

    private fun G(v: UIntArray, m: UIntArray, a: Int, b: Int, c: Int, d: Int, e: Int, i: Int) {
        v[a] += (m[sigma[i][e]] xor cst[sigma[i][e + 1]]) + v[b]
        v[d] = ROT(v[d] xor v[a], 16)
        v[c] += v[d]
        v[b] = ROT(v[b] xor v[c], 12)
        v[a] += (m[sigma[i][e + 1]] xor cst[sigma[i][e]]) + v[b]
        v[d] = ROT(v[d] xor v[a], 8)
        v[c] += v[d]
        v[b] = ROT(v[b] xor v[c], 7)
    }

    private fun blake256Compress(S: BLKState, block: UByteArray) {
        val v = UIntArray(16)
        val m = UIntArray(16)

        for (i in 0 until 16) m[i] = U8TO32(block, i * 4)
        for (i in 0 until 8) v[i] = S.h[i]
        v[8] = S.s[0] xor 0x243F6A88u
        v[9] = S.s[1] xor 0x85A308D3u
        v[10] = S.s[2] xor 0x13198A2Eu
        v[11] = S.s[3] xor 0x03707344u
        v[12] = 0xA4093822u
        v[13] = 0x299F31D0u
        v[14] = 0x082EFA98u
        v[15] = 0xEC4E6C89u

        if (S.nullt == 0) {
            v[12] = v[12] xor S.t[0]
            v[13] = v[13] xor S.t[0]
            v[14] = v[14] xor S.t[1]
            v[15] = v[15] xor S.t[1]
        }

        for (i in 0 until 14) {
            G(v, m, 0, 4, 8, 12, 0, i)
            G(v, m, 1, 5, 9, 13, 2, i)
            G(v, m, 2, 6, 10, 14, 4, i)
            G(v, m, 3, 7, 11, 15, 6, i)
            G(v, m, 3, 4, 9, 14, 14, i)
            G(v, m, 2, 7, 8, 13, 12, i)
            G(v, m, 0, 5, 10, 15, 8, i)
            G(v, m, 1, 6, 11, 12, 10, i)
        }

        for (i in 0 until 16) S.h[i % 8] = S.h[i % 8] xor v[i]
        for (i in 0 until 8) S.h[i] = S.h[i] xor S.s[i % 4]
    }

    private fun blake256Compress(S: BLKState, block: UBytePointer) {
        blake256Compress(S, block[0, block.size()])
    }


    private fun blake256Update(S: BLKState, data_: UBytePointer, datalen_: ULong) {
        var data = data_
        var datalen = datalen_
        var left = S.buflen shr 3
        val fill = 64 - left

        if ((left != 0) && (datalen shr 3 >= fill.toUInt())) {
            data[0, fill].copyInto(S.buf, left)
            S.t[0] += 512u
            if (S.t[0] == 0u) S.t[1]++
            blake256Compress(S, S.buf)
            data = (data + fill).toUBytePointer()
            datalen -= (fill shl 3).toULong()
            left = 0
        }

        while (datalen >= 512u) {
            S.t[0] += 512u
            if (S.t[0] == 0u) S.t[1]++
            blake256Compress(S, data)
            data = (data + 64).toUBytePointer()
            datalen -= 512u
        }

        if (datalen > 0u) {
            data[0, (datalen shr 3).toInt()].copyInto(S.buf, left)
            S.buflen = (left shl 3) + datalen.toInt()
        } else {
            S.buflen = 0
        }
    }

    private fun U32TO8(p: UBytePointer, u: Int, v: UInt) {
        p[u + 0] = (v shr 24).toUByte()
        p[u + 1] = (v shr 16).toUByte()
        p[u + 2] = (v shr 8).toUByte()
        p[u + 3] = v.toUByte()
    }

    private fun U8TO32(p: UByteArray, sOff: Int) =
            (p[sOff].toUInt() shl 24) or (p[sOff + 1].toUInt() shl 16) or
                    (p[sOff + 2].toUInt() shl 8) or (p[sOff + 3].toUInt())

    private val padding: UBytePointer = {
        val p = ubyteArrayOf(
                0x80u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
                0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u
        )
        val sp = Scratchpad.wrap(p)
        sp.getPointer(0)
    }()

    private fun blake256FinalH(S: BLKState, digest: UBytePointer, pa: UByte, pb: UByte) {
        val msglen = Scratchpad.getScratchpad(8)
        val lo = S.t[0] + S.buflen.toUInt()
        var hi = S.t[1]
        if (lo < S.buflen.toUInt()) hi++
        U32TO8(msglen.getPointer(0), 0, hi)
        U32TO8(msglen.getPointer(0), 4, lo)

        if (S.buflen == 440) { /* one padding byte */
            S.t[0] -= 8u
            val sp = Scratchpad.getScratchpad(1)
            sp[0] = pa
            blake256Update(S, sp.getPointer(0), 8uL)
        } else {
            if (S.buflen < 440) { /* enough space to fill the block  */
                if (S.buflen == 0) S.nullt = 1
                S.t[0] -= (440 - S.buflen).toUInt()
                blake256Update(S, padding, (440 - S.buflen).toULong())
            } else { /* need 2 compressions */
                S.t[0] -= (512 - S.buflen).toUInt()
                blake256Update(S, padding, (512 - S.buflen).toULong())
                S.t[0] -= 440u
                blake256Update(S, (padding + 1).toUBytePointer(), 440uL)
                S.nullt = 1
            }
            val sp = Scratchpad.getScratchpad(1)
            sp[0] = pb
            blake256Update(S, sp.getPointer(0), 8uL)
            S.t[0] -= 8u
        }
        S.t[0] -= 64u
        blake256Update(S, msglen.getPointer(0), 64u)

        U32TO8(digest, 0, S.h[0])
        U32TO8(digest, 4, S.h[1])
        U32TO8(digest, 8, S.h[2])
        U32TO8(digest, 12, S.h[3])
        U32TO8(digest, 16, S.h[4])
        U32TO8(digest, 20, S.h[5])
        U32TO8(digest, 24, S.h[6])
        U32TO8(digest, 28, S.h[7])
    }

    private fun blake256Final(S: BLKState, digest: UBytePointer) {
        blake256FinalH(S, digest, 0x81u, 0x01u)
    }

    @JvmStatic
    fun blake256Hash(out: UBytePointer, din: UBytePointer, inlen: ULong) {
        val S = BLKState()
        blake256Init(S)
        blake256Update(S, din, inlen * 8u)
        blake256Final(S, out)
    }
}