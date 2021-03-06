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
object OAES {
    private const val OAES_RKEY_LEN = 4
    private const val OAES_COL_LEN = 4
    private const val OAES_ROUND_BASE = 7

    private val oaesGF8 = ubyteArrayOf(
            0x01u, 0x02u, 0x04u, 0x08u, 0x10u, 0x20u, 0x40u, 0x80u, 0x1bu, 0x36u)

    private val oaesSubByteValue = Array(16) {
        when (it) {
            // 		0,    1,    2,    3,    4,    5,    6,    7,    8,    9,    a,    b,    c,    d,    e,    f,
            0x00 -> ubyteArrayOf(0x63u, 0x7cu, 0x77u, 0x7bu, 0xf2u, 0x6bu, 0x6fu, 0xc5u, 0x30u, 0x01u, 0x67u, 0x2bu, 0xfeu, 0xd7u, 0xabu, 0x76u)
            0x01 -> ubyteArrayOf(0xcau, 0x82u, 0xc9u, 0x7du, 0xfau, 0x59u, 0x47u, 0xf0u, 0xadu, 0xd4u, 0xa2u, 0xafu, 0x9cu, 0xa4u, 0x72u, 0xc0u)
            0x02 -> ubyteArrayOf(0xb7u, 0xfdu, 0x93u, 0x26u, 0x36u, 0x3fu, 0xf7u, 0xccu, 0x34u, 0xa5u, 0xe5u, 0xf1u, 0x71u, 0xd8u, 0x31u, 0x15u)
            0x03 -> ubyteArrayOf(0x04u, 0xc7u, 0x23u, 0xc3u, 0x18u, 0x96u, 0x05u, 0x9au, 0x07u, 0x12u, 0x80u, 0xe2u, 0xebu, 0x27u, 0xb2u, 0x75u)
            0x04 -> ubyteArrayOf(0x09u, 0x83u, 0x2cu, 0x1au, 0x1bu, 0x6eu, 0x5au, 0xa0u, 0x52u, 0x3bu, 0xd6u, 0xb3u, 0x29u, 0xe3u, 0x2fu, 0x84u)
            0x05 -> ubyteArrayOf(0x53u, 0xd1u, 0x00u, 0xedu, 0x20u, 0xfcu, 0xb1u, 0x5bu, 0x6au, 0xcbu, 0xbeu, 0x39u, 0x4au, 0x4cu, 0x58u, 0xcfu)
            0x06 -> ubyteArrayOf(0xd0u, 0xefu, 0xaau, 0xfbu, 0x43u, 0x4du, 0x33u, 0x85u, 0x45u, 0xf9u, 0x02u, 0x7fu, 0x50u, 0x3cu, 0x9fu, 0xa8u)
            0x07 -> ubyteArrayOf(0x51u, 0xa3u, 0x40u, 0x8fu, 0x92u, 0x9du, 0x38u, 0xf5u, 0xbcu, 0xb6u, 0xdau, 0x21u, 0x10u, 0xffu, 0xf3u, 0xd2u)
            0x08 -> ubyteArrayOf(0xcdu, 0x0cu, 0x13u, 0xecu, 0x5fu, 0x97u, 0x44u, 0x17u, 0xc4u, 0xa7u, 0x7eu, 0x3du, 0x64u, 0x5du, 0x19u, 0x73u)
            0x09 -> ubyteArrayOf(0x60u, 0x81u, 0x4fu, 0xdcu, 0x22u, 0x2au, 0x90u, 0x88u, 0x46u, 0xeeu, 0xb8u, 0x14u, 0xdeu, 0x5eu, 0x0bu, 0xdbu)
            0x0a -> ubyteArrayOf(0xe0u, 0x32u, 0x3au, 0x0au, 0x49u, 0x06u, 0x24u, 0x5cu, 0xc2u, 0xd3u, 0xacu, 0x62u, 0x91u, 0x95u, 0xe4u, 0x79u)
            0x0b -> ubyteArrayOf(0xe7u, 0xc8u, 0x37u, 0x6du, 0x8du, 0xd5u, 0x4eu, 0xa9u, 0x6cu, 0x56u, 0xf4u, 0xeau, 0x65u, 0x7au, 0xaeu, 0x08u)
            0x0c -> ubyteArrayOf(0xbau, 0x78u, 0x25u, 0x2eu, 0x1cu, 0xa6u, 0xb4u, 0xc6u, 0xe8u, 0xddu, 0x74u, 0x1fu, 0x4bu, 0xbdu, 0x8bu, 0x8au)
            0x0d -> ubyteArrayOf(0x70u, 0x3eu, 0xb5u, 0x66u, 0x48u, 0x03u, 0xf6u, 0x0eu, 0x61u, 0x35u, 0x57u, 0xb9u, 0x86u, 0xc1u, 0x1du, 0x9eu)
            0x0e -> ubyteArrayOf(0xe1u, 0xf8u, 0x98u, 0x11u, 0x69u, 0xd9u, 0x8eu, 0x94u, 0x9bu, 0x1eu, 0x87u, 0xe9u, 0xceu, 0x55u, 0x28u, 0xdfu)
            else -> ubyteArrayOf(0x8cu, 0xa1u, 0x89u, 0x0du, 0xbfu, 0xe6u, 0x42u, 0x68u, 0x41u, 0x99u, 0x2du, 0x0fu, 0xb0u, 0x54u, 0xbbu, 0x16u)
        }
    }

    private fun oaesSubByte(array: UByteArray, off: Int) {
        var _x = array[off].toInt()
        var _y = _x
        _x = _x and 0x0f
        _y = _y and 0xf0
        _y = _y shr 4
        array[off] = oaesSubByteValue[_y][_x]
    }

    private fun oaesWordRotLeft(array: UByteArray) {
        val _temp = UByteArray(OAES_COL_LEN)

        array.copyInto(_temp, 0, 1, OAES_COL_LEN)
        _temp[OAES_COL_LEN - 1] = array[0]
        _temp.copyInto(array, 0)
    }

    private fun oaesKeyExpand(data: UByteArray): UBytePointer {
        val sp = UByteArray(240)

        data.copyInto(sp)

        val temp = UByteArray(OAES_COL_LEN)
        for (_i in 8 until 60) {
            sp.copyInto(temp, 0, (_i - 1) * OAES_RKEY_LEN, ((_i - 1) * OAES_RKEY_LEN) + OAES_COL_LEN)

            if (0 == (_i % 8)) {
                oaesWordRotLeft(temp)

                for (_j in 0 until OAES_COL_LEN)
                    oaesSubByte(temp, _j)

                temp[0] = temp[0] xor oaesGF8[_i / 8 - 1]
            } else if (4 == _i % 8) {
                for (_j in 0 until OAES_COL_LEN)
                    oaesSubByte(temp, _j)
            }

            for (_j in 0 until OAES_COL_LEN) {
                sp[_i * OAES_RKEY_LEN + _j] =
                        sp[(_i - 8) *
                                OAES_RKEY_LEN + _j] xor temp[_j]
            }
        }

        return Scratchpad.wrap(sp).getPointer()
    }

    fun oaesKeyImportData(data: UByteArray): UIntArray {
        return oaesKeyExpand(data).toUIntPointer()[0, 40]
    }
}