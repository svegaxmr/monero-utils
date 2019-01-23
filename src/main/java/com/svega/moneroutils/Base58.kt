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

import com.svega.moneroutils.BinHexUtils.hexToUByteArray
import com.svega.moneroutils.exceptions.Base58DecodeException
import com.svega.moneroutils.exceptions.Base58EncodeException
import java.math.BigInteger

@ExperimentalUnsignedTypes
object Base58 {
    @Suppress("MemberVisibilityCanBePrivate")
    const val alphabetStr = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val alphabet = alphabetStr.toByteArray().asUByteArray()
    private val encodedBlockSizes = intArrayOf(0, 2, 3, 5, 6, 7, 9, 10, 11)
    private val alphabetSize = alphabet.size
    private const val fullBlockSize = 8
    private const val fullEncodedBlockSize = 11
    private val twoPow8 = 256u
    private val UINT64_MAX = BigInteger("2").pow(64)

    @Throws(Base58EncodeException::class)
    private fun uint8BufToUInt64(data: UByteArray): ULong {
        if (data.isEmpty() || data.size > 8) {
            throw Base58EncodeException("Invalid input length ${data.size}")
        }
        var res = 0uL
        var i = 0
        for (c in 9 - data.size until 9) {
            when (c == 1) {
                true -> {
                    res += data[i++].toULong()
                }
                false -> {
                    res *= twoPow8
                    res += data[i++].toULong()
                }
            }
        }
        return res
    }

    @Throws(Base58DecodeException::class)
    private fun uint64ToUInt8Buf(num: ULong, size: Int): UByteArray {
        if (size < 1 || size > 8) {
            throw Base58DecodeException("Invalid input length")
        }
        var numByteArray = num.toUByteArray()
        if (numByteArray[0] > 127u) {
            numByteArray = UByteArray(numByteArray.size + 1) {
                when (it) {
                    0 -> 0u
                    else -> numByteArray[it - 1]
                }
            }
        }
        val res = UByteArray(size)
        numByteArray.copyInto(res,
                size - if (numByteArray.size <= size) numByteArray.size else size,
                if (numByteArray.size <= size) 0 else numByteArray.size - size,
                (if (numByteArray.size <= size) 0 else numByteArray.size - size) + (if (numByteArray.size <= size) numByteArray.size else size))
        return res
    }

    @Throws(Base58EncodeException::class)
    private fun encodeBlock(data: UByteArray, buf: UByteArray, index: Int): UByteArray {
        if (data.isEmpty() || data.size > fullEncodedBlockSize) {
            throw Base58EncodeException("Invalid block length: ${data.size}")
        }
        var num = uint8BufToUInt64(data)
        var i = encodedBlockSizes[data.size] - 1
        // while num > 0
        var lastNum = num
        while ((lastNum >= num) and (num > 0uL)) {
            lastNum = num
            val remainder = num % alphabetSize.toULong()
            num = num / alphabetSize.toUInt()
            // remainder = num % alphabetSize
            // num = num / alphabet_size
            buf[index + i] = alphabet[remainder.toInt()]
            i--
        }
        return buf
    }

    /**
     * Encodes [hex] into its Monero Base58 representation
     * @param hex A hexadecimal string to encode
     * @return The Monero Base58 representation of [hex]
     * @throws Base58EncodeException If something goes wrong the the encode process
     */
    @Throws(Base58EncodeException::class)
    fun encode(hex: String): String {
        val data = hexToUByteArray(hex)
        if (data.isEmpty()) {
            return ""
        }
        val fullBlockCount = Math.floor(data.size.toDouble() / fullBlockSize).toInt()
        val sizeOfLastBlock = data.size % fullBlockSize
        val resSize = (fullBlockCount * fullEncodedBlockSize + encodedBlockSizes[sizeOfLastBlock])
        var res = UByteArray(resSize)
        for (i in 0 until resSize) {
            res[i] = alphabet[0]
        }
        for (i in 0 until fullBlockCount) {
            res = encodeBlock(data.copyOfRange(i * fullBlockSize, i * fullBlockSize + fullBlockSize), res, i * fullEncodedBlockSize)
        }
        if (sizeOfLastBlock > 0) {
            res = encodeBlock(data.copyOfRange(fullBlockCount * fullBlockSize, fullBlockCount * fullBlockSize + sizeOfLastBlock), res, fullBlockCount * fullEncodedBlockSize)
        }
        return String(res.toByteArray())
    }

    @Throws(Base58DecodeException::class)
    private fun decodeBlock(data: UByteArray, buf: UByteArray, index: Int): UByteArray {
        if (data.isEmpty() || data.size > fullEncodedBlockSize) {
            throw Base58DecodeException("Invalid block length: ${data.size}")
        }
        val resSize = encodedBlockSizes.indexOf(data.size)
        if (resSize <= 0) {
            throw Base58DecodeException("Invalid block size ${data.size}")
        }
        var resNum = BigInteger.ZERO
        var order = BigInteger.ONE
        for (i in data.size - 1 downTo 0) {
            val digit = alphabet.indexOf(data[i])
            if (digit < 0) {
                throw Base58DecodeException("${data[i]} is an invalid symbol!")
            }
            val product = order.multiply(digit.toBigInteger()).add(resNum)
            if (product > UINT64_MAX) {
                throw Base58DecodeException("${digit.toBigInteger()} times $resNum results in an overflow")
            }
            resNum = product
            order = order.multiply(alphabetSize.toBigInteger())
        }
        if (resSize < fullBlockSize && (BigInteger("2").pow(8 * resSize) < resNum)) {
            throw Base58DecodeException("Overflow")
        }
        uint64ToUInt8Buf(resNum.toLong().toULong(), resSize).copyInto(buf, index)
        return buf
    }

    /**
     * Takes the Monero Base58 representation and returns a [UByteArray] with the data
     * @param base58 The Monero Base58 data
     * @returns The data in byte form
     * @throws Base58DecodeException If an exception occurs in the decoding of [base58]
     */
    @Throws(Base58DecodeException::class)
    fun decode(base58: String): UByteArray {
        val enc = base58.toByteArray().asUByteArray()
        if (enc.isEmpty()) {
            return UByteArray(0)
        }
        val fullBlockCount = Math.floor(enc.size.toDouble() / fullEncodedBlockSize).toInt()
        val lastBlockSize = enc.size % fullEncodedBlockSize
        val lastDecodedBlockSize = encodedBlockSizes.indexOf(lastBlockSize)
        if (lastDecodedBlockSize < 0) {
            throw Base58DecodeException("Invalid encoding length $lastBlockSize")
        }
        val dataSize = fullBlockCount * fullBlockSize + lastDecodedBlockSize
        var data = UByteArray(dataSize)
        for (i in 0 until fullBlockCount) {
            data = decodeBlock(enc.copyOfRange(i * fullEncodedBlockSize, i * fullEncodedBlockSize + fullEncodedBlockSize), data, i * fullBlockSize)
        }
        if (lastBlockSize > 0) {
            data = decodeBlock(enc.copyOfRange(fullBlockCount * fullEncodedBlockSize, fullBlockCount * fullEncodedBlockSize + lastBlockSize), data, fullBlockCount * fullBlockSize)
        }
        return data
    }
}