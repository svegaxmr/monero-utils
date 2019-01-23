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

import com.svega.moneroutils.exceptions.MoneroException

@ExperimentalUnsignedTypes
object BinHexUtils {
    fun hexToByteArray(s: String): ByteArray {
        val len = s.length

        if (len % 2 != 0)
            throw MoneroException("Hex string has invalid length!")
        if (s.isEmpty())
            return byteArrayOf()

        val out = ByteArray(len / 2)

        var i = 0
        while (i < len) {
            val h = hexToBin(s[i])
            val l = hexToBin(s[i + 1])
            if (h == -1 || l == -1) {
                throw IllegalArgumentException(
                        "contains illegal character for hexBinary: $s")
            }

            out[i / 2] = (h * 16 + l).toByte()
            i += 2
        }

        return out
    }

    @ExperimentalUnsignedTypes
    fun hexToUByteArray(s: String): UByteArray {
        val len = s.length

        if (len % 2 != 0)
            throw MoneroException("Hex string has invalid length!")
        if (s.isEmpty())
            return ubyteArrayOf()

        val out = UByteArray(len / 2)

        var i = 0
        while (i < len) {
            val h = hexToBin(s[i])
            val l = hexToBin(s[i + 1])
            if (h == -1 || l == -1) {
                throw IllegalArgumentException(
                        "contains illegal character for hexBinary: $s")
            }

            out[i / 2] = (h * 16 + l).toUByte()
            i += 2
        }

        return out
    }

    private fun hexToBin(ch: Char): Int {
        if (ch in '0'..'9') {
            return ch - '0'
        }
        if (ch in 'A'..'F') {
            return ch - 'A' + 10
        }
        return if (ch in 'a'..'f') {
            ch - 'a' + 10
        } else -1
    }

    fun convertHexToString(hex: String) = String(hexToByteArray(hex))

    private val hexCode = "0123456789ABCDEF".toCharArray()
    private fun printHexBinary(data: ByteArray): String {
        val r = StringBuilder(data.size * 2)
        for (b in data) {
            r.append(hexCode[b.toInt() shr 4 and 0xF])
            r.append(hexCode[b.toInt() and 0xF])
        }
        return r.toString()
    }

    fun binaryToHex(bin: ByteArray) = printHexBinary(bin)

    fun ubinaryToHex(bin: UByteArray) = printHexBinary(bin.asByteArray())

    fun binaryToHex(bin: List<Byte>) = printHexBinary(bin.toByteArray())
}