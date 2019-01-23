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

package com.svega.moneroutils.transactions

import com.svega.moneroutils.AmountDivision
import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


@ExperimentalUnsignedTypes
class Transaction private constructor() : MoneroSerializable {
    lateinit var header: TransactionPrefix
        private set
    var totalOut = XMRAmount(-1)
        private set
    var isHashValid = false
        private set
    var hash = ByteArray(0)
        private set
        get() {
            if (isHashValid)
                return field
            else {
                val res = ByteArray(32)
                isHashValid = getTXHash(this, res)
                field = res
            }
            return if (isHashValid) field else ByteArray(0)
        }

    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        b.write(header.toBlob())
        return b.toByteArray()
    }

    @ExperimentalUnsignedTypes
    companion object {
        fun parseBlobHeader(blob: ByteArray) = parseBlobHeader(ByteBuffer.wrap(blob).rewind())

        fun parseBlobHeader(bb: ByteBuffer): Transaction {
            val tx = Transaction()
            with(tx) {
                header = TransactionPrefix.parseFromBlob(bb)
                for (t in header.outs) {
                    totalOut += t.amount
                }
                if (header.ver == 2) {//rct

                }
            }
            return tx
        }

        var txHashesCalculated = 0
        fun getTXHash(t: Transaction, res: ByteArray): Boolean {
            ++txHashesCalculated
            return calculateTXHash(t, res)
        }

        private fun calculateTXHash(t: Transaction, res: ByteArray): Boolean {
            if (t.header.ver == 1) {
                return Hashing.getObjectHash(t, res)
            }

            val hashes = Array(3) { ByteArray(32) }

            //rct

            TransactionPrefix.getTXPrefixHash(t, hashes[0])

            return true
        }
    }

    override fun toString(): String {
        return "Transaction(header=$header, totalOut=${totalOut.toOther(AmountDivision.WHOLE)})"
    }

}

@ExperimentalUnsignedTypes
typealias CoinbaseTransaction = Transaction