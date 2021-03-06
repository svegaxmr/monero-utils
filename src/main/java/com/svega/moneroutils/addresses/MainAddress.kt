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

package com.svega.moneroutils.addresses

import com.svega.crypto.ed25519.*
import com.svega.moneroutils.AddressType
import com.svega.moneroutils.NetType
import com.svega.moneroutils.SWAP32
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.exceptions.MoneroException
import com.svega.moneroutils.toByteArray
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

typealias SubAddressIndex = Pair<Int, Int>

@ExperimentalUnsignedTypes
class MainAddress : MoneroAddress {
    constructor(address: String, net: NetType) : super(address, net)
    constructor(key: FullKey, net: NetType, addrType: AddressType) : super(key, net, addrType)
    constructor(seed: ByteArray? = null, secretSpendKey: SecretKey? = null, net: NetType) :
            super(seed, secretSpendKey, net, AddressType.MAIN)

    override val LENGTH = ADDRESS_LENGTH
    override val BYTES = ADDRESS_BYTES

    init {
        validate()
    }

    fun getIntegratedAddress(paymentIDHex: String) = IntegratedAddress(this, paymentIDHex)

    private val subAddresses = HashMap<SubAddressIndex, SubAddress>()

    /**
     * Creates a subaddress for this address at position [account], [index]
     * @param account The account for the subaddress. All subaddresses in this account will have their funds spendable
     * together.
     * @param index The index to generate inside the [account]
     */
    fun genSubaddress(account: Int, index: Int): MoneroAddress {
        val subAddrIndex = SubAddressIndex(account, index)
        if (subAddresses.containsKey(subAddrIndex))
            return subAddresses[subAddrIndex]!!

        if (subAddrIndex.isZero())
            return this

        if (this.key.view.secret == null)
            throw MoneroException("Subaddresses can only be created for addresses with a private spend key!")

        val D = getSubaddressSpendPublicKey(this, subAddrIndex)

        val C = scalarmultKey(D, this.key.view.secret!!)

        val sAddr = SubAddress(FullKey(KeyPair(D, null), KeyPair(C, null)), netType)

        subAddresses[subAddrIndex] = sAddr

        return sAddr
    }

    companion object {
        private fun getSubaddressSpendPublicKey(mainAddress: MainAddress, index: SubAddressIndex): PublicKey {
            if (index.isZero())
                return mainAddress.key.spend.public

            if (mainAddress.key.view.secret == null)
                throw MoneroException("Subaddresses can only be created for addresses with a private spend key!")

            val m = subAddressSecretKey(mainAddress.key.view.secret!!, index)

            val M = m.getPublic()

            return addKeys(mainAddress.key.spend.public, M)
        }

        private fun addKeys(A: Key, B: Key): Key {
            val B2 = ge_p3()
            val A2 = ge_p3()
            assert(ge.frombytes_vartime(B2, B.data.asUByteArray()) == 0)
            assert(ge.frombytes_vartime(A2, A.data.asUByteArray()) == 0)
            val tmp2 = ge_cached()
            ge.p3_to_cached(tmp2, B2)
            val tmp3 = ge_p1p1()
            ge.add(tmp3, A2, tmp2)
            ge.p1p1_to_p3(A2, tmp3)
            val ba = ByteArray(32)
            ge.p3_tobytes(ba.asUByteArray(), A2)
            return Key(ba)
        }

        private fun scalarmultKey(P: Key, a: Key): Key {
            val A = ge_p3()
            val R = ge_p2()
            assert(ge.frombytes_vartime(A, P.data.asUByteArray()) == 0)
            ge.scalarmult(R, a.data.asUByteArray(), A)
            val aP = ByteArray(32)
            ge.tobytes(aP.asUByteArray(), R)
            return Key(aP)
        }

        private fun subAddressSecretKey(secret: SecretKey, index: SubAddressIndex): SecretKey {
            var data = "SubAddr".toByteArray() + 0.toByte()
            data += secret.data
            data += SWAP32(index.first).toByteArray()
            data += SWAP32(index.second).toByteArray()
            val s = Hashing.hashToScalar(data)
            return SecretKey(s)
        }

        const val ADDRESS_LENGTH = 95
        const val ADDRESS_BYTES = 69
    }
}

fun SubAddressIndex.isZero() = (this.first == 0) and (this.second == 0)