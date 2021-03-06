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
object AESB {
    private val WPOLY = 0x011bu.toUByte()

    //#define state_in(y,x) si(y,x,0); si(y,x,1); si(y,x,2); si(y,x,3)
    private fun stateIn(y: UIntArray, x: UIntPointer) {
        y[0] = x[0]
        y[1] = x[1]
        y[2] = x[2]
        y[3] = x[3]
    }

    //#define state_out(y,x)  so(y,x,0); so(y,x,1); so(y,x,2); so(y,x,3) //sets y to 4 uint32s
    private fun stateOut(y: UIntPointer, x: UIntArray) {
        y[0] = x[0]
        y[1] = x[1]
        y[2] = x[2]
        y[3] = x[3]
    }

    private fun doubleRound(y: UIntArray, x: UIntArray, k: UIntArray, off: Int) {
        y[0] = k[0 + off] xor fourTables(x, 0)
        y[1] = k[1 + off] xor fourTables(x, 1)
        y[2] = k[2 + off] xor fourTables(x, 2)
        y[3] = k[3 + off] xor fourTables(x, 3)
        x[0] = k[4 + off] xor fourTables(y, 0)
        x[1] = k[5 + off] xor fourTables(y, 1)
        x[2] = k[6 + off] xor fourTables(y, 2)
        x[3] = k[7 + off] xor fourTables(y, 3)
    }

    private fun doubleRound(y: UIntArray, x: UIntArray, k: UIntArray, off: Int, xOff: Int) {
        y[0] = k[0 + off] xor fourTables(x, 0, xOff)
        y[1] = k[1 + off] xor fourTables(x, 1, xOff)
        y[2] = k[2 + off] xor fourTables(x, 2, xOff)
        y[3] = k[3 + off] xor fourTables(x, 3, xOff)
        x[0 + xOff] = k[4 + off] xor fourTables(y, 0)
        x[1 + xOff] = k[5 + off] xor fourTables(y, 1)
        x[2 + xOff] = k[6 + off] xor fourTables(y, 2)
        x[3 + xOff] = k[7 + off] xor fourTables(y, 3)
    }

    /**
    #define f2(x)   ((x<<1) ^ (((x>>7) & 1) * WPOLY))
    #define f4(x)   ((x<<2) ^
    (((x>>6) & 1) * WPOLY) ^
    (((x>>6) & 2) * WPOLY))
    #define f8(x)   ((x<<3) ^ (((x>>5) & 1) * WPOLY) ^ (((x>>5) & 2) * WPOLY) ^ (((x>>5) & 4) * WPOLY))
    #define f3(x)   (f2(x) ^ x)
    #define f9(x)   (f8(x) ^ x)
    #define fb(x)   (f8(x) ^ f2(x) ^ x)
    #define fd(x)   (f8(x) ^ f4(x) ^ x)
    #define fe(x)   (f8(x) ^ f4(x) ^ f2(x))
     */
    private fun f2(x: UByte): UByte =
            ((x.toUInt() shl 1).and(0xFFu) xor
                    (((x.toUInt() shr 7) and 1u) * WPOLY)).toUByte()

    private fun f3(x: UByte): UByte = (f2(x).toUInt() xor x.toUInt()).toUByte()

    //#define bytes2word(b0, b1, b2, b3) (((uint32_t)(b3) << 24) | \
    //    ((uint32_t)(b2) << 16) | ((uint32_t)(b1) << 8) | (b0))
    private fun bytes2word(b0: UByte, b1: UByte, b2: UByte, b3: UByte): UInt {
        return b0.toUInt() or
                b1.toUInt().shl(8) or
                b2.toUInt().shl(16) or
                b3.toUInt().shl(24)
    }

    /**
    #define u0(p)   bytes2word(f2(p), p, p, f3(p))
    #define u1(p)   bytes2word(f3(p), f2(p), p, p)
    #define u2(p)   bytes2word(p, f3(p), f2(p), p)
    #define u3(p)   bytes2word(p, p, f3(p), f2(p))
     */
    private val u0 = { p: UByte -> bytes2word(f2(p), p, p, f3(p)) }
    private val u1 = { p: UByte -> bytes2word(f3(p), f2(p), p, p) }
    private val u2 = { p: UByte -> bytes2word(p, f3(p), f2(p), p) }
    private val u3 = { p: UByte -> bytes2word(p, p, f3(p), f2(p)) }

    private inline fun sbData(w: (UByte) -> UInt) = uintArrayOf(
            w(0x63u), w(0x7cu), w(0x77u), w(0x7bu), w(0xf2u), w(0x6bu), w(0x6fu), w(0xc5u),
            w(0x30u), w(0x01u), w(0x67u), w(0x2bu), w(0xfeu), w(0xd7u), w(0xabu), w(0x76u),
            w(0xcau), w(0x82u), w(0xc9u), w(0x7du), w(0xfau), w(0x59u), w(0x47u), w(0xf0u),
            w(0xadu), w(0xd4u), w(0xa2u), w(0xafu), w(0x9cu), w(0xa4u), w(0x72u), w(0xc0u),
            w(0xb7u), w(0xfdu), w(0x93u), w(0x26u), w(0x36u), w(0x3fu), w(0xf7u), w(0xccu),
            w(0x34u), w(0xa5u), w(0xe5u), w(0xf1u), w(0x71u), w(0xd8u), w(0x31u), w(0x15u),
            w(0x04u), w(0xc7u), w(0x23u), w(0xc3u), w(0x18u), w(0x96u), w(0x05u), w(0x9au),
            w(0x07u), w(0x12u), w(0x80u), w(0xe2u), w(0xebu), w(0x27u), w(0xb2u), w(0x75u),
            w(0x09u), w(0x83u), w(0x2cu), w(0x1au), w(0x1bu), w(0x6eu), w(0x5au), w(0xa0u),
            w(0x52u), w(0x3bu), w(0xd6u), w(0xb3u), w(0x29u), w(0xe3u), w(0x2fu), w(0x84u),
            w(0x53u), w(0xd1u), w(0x00u), w(0xedu), w(0x20u), w(0xfcu), w(0xb1u), w(0x5bu),
            w(0x6au), w(0xcbu), w(0xbeu), w(0x39u), w(0x4au), w(0x4cu), w(0x58u), w(0xcfu),
            w(0xd0u), w(0xefu), w(0xaau), w(0xfbu), w(0x43u), w(0x4du), w(0x33u), w(0x85u),
            w(0x45u), w(0xf9u), w(0x02u), w(0x7fu), w(0x50u), w(0x3cu), w(0x9fu), w(0xa8u),
            w(0x51u), w(0xa3u), w(0x40u), w(0x8fu), w(0x92u), w(0x9du), w(0x38u), w(0xf5u),
            w(0xbcu), w(0xb6u), w(0xdau), w(0x21u), w(0x10u), w(0xffu), w(0xf3u), w(0xd2u),
            w(0xcdu), w(0x0cu), w(0x13u), w(0xecu), w(0x5fu), w(0x97u), w(0x44u), w(0x17u),
            w(0xc4u), w(0xa7u), w(0x7eu), w(0x3du), w(0x64u), w(0x5du), w(0x19u), w(0x73u),
            w(0x60u), w(0x81u), w(0x4fu), w(0xdcu), w(0x22u), w(0x2au), w(0x90u), w(0x88u),
            w(0x46u), w(0xeeu), w(0xb8u), w(0x14u), w(0xdeu), w(0x5eu), w(0x0bu), w(0xdbu),
            w(0xe0u), w(0x32u), w(0x3au), w(0x0au), w(0x49u), w(0x06u), w(0x24u), w(0x5cu),
            w(0xc2u), w(0xd3u), w(0xacu), w(0x62u), w(0x91u), w(0x95u), w(0xe4u), w(0x79u),
            w(0xe7u), w(0xc8u), w(0x37u), w(0x6du), w(0x8du), w(0xd5u), w(0x4eu), w(0xa9u),
            w(0x6cu), w(0x56u), w(0xf4u), w(0xeau), w(0x65u), w(0x7au), w(0xaeu), w(0x08u),
            w(0xbau), w(0x78u), w(0x25u), w(0x2eu), w(0x1cu), w(0xa6u), w(0xb4u), w(0xc6u),
            w(0xe8u), w(0xddu), w(0x74u), w(0x1fu), w(0x4bu), w(0xbdu), w(0x8bu), w(0x8au),
            w(0x70u), w(0x3eu), w(0xb5u), w(0x66u), w(0x48u), w(0x03u), w(0xf6u), w(0x0eu),
            w(0x61u), w(0x35u), w(0x57u), w(0xb9u), w(0x86u), w(0xc1u), w(0x1du), w(0x9eu),
            w(0xe1u), w(0xf8u), w(0x98u), w(0x11u), w(0x69u), w(0xd9u), w(0x8eu), w(0x94u),
            w(0x9bu), w(0x1eu), w(0x87u), w(0xe9u), w(0xceu), w(0x55u), w(0x28u), w(0xdfu),
            w(0x8cu), w(0xa1u), w(0x89u), w(0x0du), w(0xbfu), w(0xe6u), w(0x42u), w(0x68u),
            w(0x41u), w(0x99u), w(0x2du), w(0x0fu), w(0xb0u), w(0x54u), w(0xbbu), w(0x16u))

    private val t_fn = Array(4) { i ->
        when (i) {
            0 -> sbData(u0)
            1 -> sbData(u1)
            2 -> sbData(u2)
            else -> sbData(u3)
        }
    }

    /**
     * four_tables(x,t_fn,fwd_var,rf1,c))
     *
    #define four_tables(x,tab,vf,rf,c) \
    (tab[0][bval(vf(x,0,c),rf(0,c))] \
    ^ tab[1][bval(vf(x,1,c),rf(1,c))] \
    ^ tab[2][bval(vf(x,2,c),rf(2,c))] \
    ^ tab[3][bval(vf(x,3,c),rf(3,c))])

    y: UIntArray, x: UIntArray, k: UIntPointer, c: Int
     */

    private fun fourTables(x: UIntArray, c: Int, t: Int = 0): UInt {
        return t_fn[0][x[(c % 4) + t].toInt() and 0xFF] xor
                t_fn[1][(x[((c + 1) % 4) + t] shr 8).toInt() and 0xFF] xor
                t_fn[2][(x[((c + 2) % 4) + t] shr 16).toInt() and 0xFF] xor
                t_fn[3][(x[((c + 3) % 4) + t] shr 24).toInt() and 0xFF]
    }

    @JvmStatic
    fun aesbSingleRound(pin: UIntPointer, expandedKey: ULongArray) {
        val b0 = UIntArray(4)
        stateIn(b0, pin)

        pin[0] = IntUtils.loDword(expandedKey[0]).toUInt() xor fourTables(b0, 0)
        pin[1] = IntUtils.hiDword(expandedKey[0]).toUInt() xor fourTables(b0, 1)
        pin[2] = IntUtils.loDword(expandedKey[1]).toUInt() xor fourTables(b0, 2)
        pin[3] = IntUtils.hiDword(expandedKey[1]).toUInt() xor fourTables(b0, 3)
    }

    @JvmStatic
    fun aesbPseudoRound(pin: UBytePointer, pout: UBytePointer, kp: UIntArray) {
        val b0 = UIntArray(4)
        val b1 = UIntArray(4)
        stateIn(b0, pin.toUIntPointer())

        doubleRound(b1, b0, kp, 0)
        doubleRound(b1, b0, kp, 8)
        doubleRound(b1, b0, kp, 16)
        doubleRound(b1, b0, kp, 24)
        doubleRound(b1, b0, kp, 32)

        stateOut(pout.toUIntPointer(), b0)
    }

    @JvmStatic
    fun aesbPseudoRound(pin: UIntPointer, kp: UIntArray) {
        val b0 = UIntArray(4)
        val b1 = UIntArray(4)
        stateIn(b0, pin)

        doubleRound(b1, b0, kp, 0)
        doubleRound(b1, b0, kp, 8)
        doubleRound(b1, b0, kp, 16)
        doubleRound(b1, b0, kp, 24)
        doubleRound(b1, b0, kp, 32)

        stateOut(pin, b0)
    }

    @JvmStatic
    fun aesbPseudoRound(pin: UIntArray, kp: UIntArray, jOff: Int) {
        val b1 = UIntArray(4)

        doubleRound(b1, pin, kp, 0, jOff)
        doubleRound(b1, pin, kp, 8, jOff)
        doubleRound(b1, pin, kp, 16, jOff)
        doubleRound(b1, pin, kp, 24, jOff)
        doubleRound(b1, pin, kp, 32, jOff)
    }
}