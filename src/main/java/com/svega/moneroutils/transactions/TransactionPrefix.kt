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

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.crypto.MoneroSerializable
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

private val logger = KotlinLogging.logger { }

@ExperimentalUnsignedTypes
class TransactionPrefix private constructor() : MoneroSerializable {
    var ver = -1
        private set
    var ulTime = -1L
        private set
    var numIns = -1
        private set
    var numOuts = -1
        private set
    var ins = ArrayList<TransactionInput>()
        private set
        get() = ArrayList(field)
    var outs = ArrayList<TransactionOutput>()
        private set
        get() = ArrayList(field)
    var extra = ByteArray(0)
        private set
        get() = Arrays.copyOf(field, field.size)
    var pubKey = ByteArray(0)
        private set
        get() = Arrays.copyOf(field, field.size)
    var nonce = ByteArray(0)
        private set
        get() = Arrays.copyOf(field, field.size)
    var paymentID: PaymentID? = null
        private set
    var extraPubKeys = ArrayList<ByteArray>()
        private set
        get() = ArrayList(field)

    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        val d = DataOutputStream(b)
        d.writeVarInt(ver)
        d.writeVarLong(ulTime)
        d.writeVarInt(numIns)
        for (i in ins) {
            d.write(i.toBlob())
        }
        d.writeVarInt(numOuts)
        for (i in outs) {
            d.write(i.toBlob())
        }
        d.writeVarInt(extra.size)
        d.write(extra)
        return b.toByteArray()
    }

    @ExperimentalUnsignedTypes
    companion object {
        fun parseFromBlob(bb: ByteBuffer): TransactionPrefix {
            val ret = TransactionPrefix()
            with(ret) {
                ver = bb.getVarInt()
                ulTime = bb.getVarLong()
                numIns = bb.getVarInt()
                val tins = ArrayList<TransactionInput>()
                for (i in 0 until numIns) {
                    tins.add(TransactionInput.parseFromBlob(bb))
                }
                ins = tins
                numOuts = bb.getVarInt()
                val touts = ArrayList<TransactionOutput>()
                for (i in 0 until numOuts) {
                    touts.add(TransactionOutput.parseFromBlob(bb))
                }
                outs = touts

                val extraSize = bb.getVarInt()
                val temp = ByteArray(extraSize)
                bb.get(temp)
                extra = temp
                val bb2 = ByteBuffer.wrap(temp) //tx extra

                try {
                    while (bb2.remaining() != 0) {
                        val id = bb2.get().toInt() and 0xFF //make positive and correctly bounded
                        when (id) {
                            0 -> {
                                while (bb2.remaining() != 0) {
                                    bb2.get()
                                }
                            }
                            1 -> { //pubKey
                                val pKey = ByteArray(32)
                                bb2.get(pKey)
                                pubKey = pKey
                            }
                            2 -> { //nonce
                                val bb3 = bb2.readNewBuffer()
                                if ((bb3.remaining() != 9) and (bb3.remaining() != 33)) {
                                    val tmp = ByteArray(bb3.remaining())
                                    bb3.get(tmp)
                                    nonce = tmp
                                } else {
                                    val nxt = bb3.get().toInt()
                                    val encrypted = nxt == 1
                                    val pid = ByteArray(if (encrypted) 8 else 32)
                                    bb3.get(pid)
                                    paymentID = PaymentID(encrypted, pid)
                                }
                            }
                            4 -> {
                                val tExtras = ArrayList<ByteArray>()
                                val key = ByteArray(32)
                                for (i in 0 until bb2.getVarInt()) {
                                    bb2.get(key)
                                    tExtras.add(key)
                                }
                                extraPubKeys = tExtras
                            }
                            else -> logger.warn { "Failed parsing tx extra (${BinHexUtils.binaryToHex(temp)})! At id $id" }
                        }
                    }
                } catch (e: Exception) {
                    logger.warn { "Failed parsing tx extra (${BinHexUtils.binaryToHex(temp)})! ${e.localizedMessage}" }
                }
                //TODO: SIGNATURES
            }
            return ret
        }

        fun getTXPrefixHash(t: Transaction, res: ByteArray) = System.arraycopy(Hashing.getBlobHash(t.toBlob()), 0, res, 0, 32)
    }

    override fun toString(): String {
        return "TransactionPrefix(ver=$ver, ulTime=$ulTime, numIns=$numIns, numOuts=$numOuts, paymentID=$paymentID, ins=${Arrays.toString(ins.toArray())}, outs=${Arrays.toString(outs.toArray())}, pubKey=${BinHexUtils.binaryToHex(pubKey)})"
    }
}
