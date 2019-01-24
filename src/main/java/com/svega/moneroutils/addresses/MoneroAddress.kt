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

import com.svega.crypto.common.CryptoOps
import com.svega.crypto.common.algos.Keccak
import com.svega.crypto.common.algos.Parameter
import com.svega.moneroutils.AddressType
import com.svega.moneroutils.Base58
import com.svega.moneroutils.NetType
import com.svega.moneroutils.exceptions.InvalidChecksumException
import com.svega.moneroutils.exceptions.MoneroException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

@ExperimentalUnsignedTypes
abstract class MoneroAddress : Serializable {
    var bytes: UByteArray
        protected set
    var address: String
        protected set
    var key: FullKey
        private set
    var netType: NetType
        private set
    var seed: ByteArray?
        private set
    abstract val BYTES: Int
    abstract val LENGTH: Int

    protected constructor(address: String, net: NetType) {
        this.netType = net
        this.address = address
        seed = null
        bytes = Base58.decode(address)
        val spend = KeyPair(Key(bytes.copyOfRange(0, 32).asByteArray()), null)
        val view = KeyPair(Key(bytes.copyOfRange(32, 64).asByteArray()), null)
        key = FullKey(spend, view)
    }

    protected constructor(seed: ByteArray? = null, secretSpendKey: SecretKey? = null, net: NetType, addrType: AddressType) {
        if ((seed == null) and (secretSpendKey == null))
            throw MoneroException("seed and secret spend key cannot be null!")
        this.netType = net
        this.seed = seed
        val spend: KeyPair = when (secretSpendKey == null) {
            true -> generateKeys(seed!!)
            false -> KeyPair.genFromSecret(secretSpendKey.data)
        }
        val second = Keccak.getHash(spend.secret!!.data, Parameter.KECCAK_256) //keccak256 of view gets spend
        val view = generateKeys(second) //spend is got from generatekeys
        key = FullKey(spend, view)
        address = key.getAddressString(addrType, net)
        bytes = key.getAddressBytes(addrType, net)
    }

    protected constructor(key: FullKey, net: NetType, addrType: AddressType) {
        this.key = key
        this.netType = net
        seed = null
        address = key.getAddressString(addrType, net)
        bytes = key.getAddressBytes(addrType, net)
    }

    protected open fun validate() {
        if (address.length != LENGTH)
            throw MoneroException("Address $address is not $LENGTH characters long!")
        if (bytes.size != BYTES)
            throw MoneroException("Address $address is not $BYTES bytes long!")
        validateChecksum(bytes, address)
    }

    @Throws(IOException::class)
    private fun writeObject(stream: ObjectOutputStream) {
        stream.writeInt(SERIALIZABLE_VERSION)
        stream.writeShort(bytes.size)
        stream.write(bytes.asByteArray())
        stream.writeUTF(address)
        stream.writeObject(key)
        stream.writeObject(netType)
        if (seed == null) {
            stream.writeBoolean(false)
        } else {
            stream.writeBoolean(true)
            stream.writeShort(seed!!.size)
            stream.write(seed)
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream) {
        stream.readInt() //version
        var read = stream.readShort()
        val temp = ByteArray(read.toInt())
        stream.readFully(temp)
        bytes = temp.asUByteArray()
        address = stream.readUTF()
        key = stream.readObject() as FullKey
        netType = stream.readObject() as NetType
        if (stream.readBoolean()) {
            read = stream.readShort()
            seed = ByteArray(read.toInt())
            stream.readFully(seed)
        }

    }

    @ExperimentalUnsignedTypes
    companion object {
        const val SERIALIZABLE_VERSION = 0
        /**
         * Takes in the string [address] and returns an address object
         * @param address The address to turn into the object
         * @return The MoneroAddress that is represented by [address]
         * @throws MoneroException When the network byte does not match any known network byte
         */
        @JvmStatic
        @Throws(MoneroException::class)
        fun stringToAddress(address: String): MoneroAddress {
            val arr = Base58.decode(address)
            validateChecksum(arr, address)
            return when (arr[0]) {
                NetType.MAINNET.MAINADDR -> MainAddress(address, NetType.MAINNET)
                NetType.MAINNET.INTEGRATED -> IntegratedAddress(address, NetType.MAINNET)
                NetType.MAINNET.SUBADDR -> SubAddress(address, NetType.MAINNET)
                NetType.TESTNET.MAINADDR -> MainAddress(address, NetType.TESTNET)
                NetType.TESTNET.INTEGRATED -> IntegratedAddress(address, NetType.TESTNET)
                NetType.TESTNET.SUBADDR -> SubAddress(address, NetType.TESTNET)
                NetType.STAGENET.MAINADDR -> MainAddress(address, NetType.STAGENET)
                NetType.STAGENET.INTEGRATED -> IntegratedAddress(address, NetType.STAGENET)
                NetType.STAGENET.SUBADDR -> SubAddress(address, NetType.STAGENET)
                else -> throw MoneroException("Address prefix ${arr[0]} is not a valid network byte")
            }
        }

        /**
         * Validates that a given address is valid
         * @param bytes The bytes of the address
         * @param address The string representation of the address
         * @throws InvalidChecksumException When the checksum does not match
         */
        @JvmStatic
        @Throws(InvalidChecksumException::class)
        fun validateChecksum(bytes: UByteArray, address: String) {
            val checksum = com.svega.moneroutils.crypto.slowhash.Keccak.checkChecksum(bytes)
            if (!checksum) {
                throw InvalidChecksumException("Invalid address $address fails checksum")
            }
        }

        /**
         * Generates address keys from [seed]
         * @param seed A [ByteArray] with 32 bytes
         * @return A KeyPair with all keys filled out
         * @throws MoneroException if the length of [seed] is not 32 bytes
         */
        @JvmStatic
        @Throws(MoneroException::class)
        fun generateKeys(seed: ByteArray): KeyPair {
            if (seed.size != 32)
                throw MoneroException("Invalid input length!")
            return KeyPair.genFromSecret(CryptoOps.sc_reduce32(seed))
        }
    }
}