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

import com.svega.moneroutils.Base58
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.NetType
import com.svega.moneroutils.concat
import com.svega.moneroutils.crypto.slowhash.Keccak
import com.svega.moneroutils.exceptions.MoneroException

@ExperimentalUnsignedTypes
class IntegratedAddress : MoneroAddress {
    override val LENGTH = ADDRESS_LENGTH
    override val BYTES = ADDRESS_BYTES
    var paymentID: ByteArray = ByteArray(8)
        set(value) {
            if (value.size != 8)
                throw MoneroException("Payment ID is size ${value.size}, needs to be 8")
            field = value
        }
    var paymentIDStr: String
        get() = BinHexUtils.binaryToHex(paymentID)
        set(value) {
            paymentID = BinHexUtils.hexToByteArray(value)
        }
    var mainAddressStr = ""
        private set

    constructor(address: String, net: NetType) : super(address, net) {
        paymentID = bytes.asByteArray().sliceArray(IntRange(65, 72))
        val mAddrKeys = UByteArray(1) { net.INTEGRATED } concat bytes.copyOfRange(1, 65)
        val mAddrCSum = Keccak.fullChecksum(mAddrKeys)
        val mAddrBytes = mAddrKeys concat mAddrCSum
        validateChecksum(mAddrBytes, "Integrated address splitting")
        mainAddressStr = Base58.encode(BinHexUtils.ubinaryToHex(mAddrBytes))
        validate()
    }

    constructor(mainAddress: MainAddress, paymentIDStr: String) : super(mainAddress.address, mainAddress.netType) {
        this.paymentIDStr = paymentIDStr
        this.mainAddressStr = mainAddress.address
        val mAddrKeys = Base58.decode(mainAddressStr).copyOfRange(1, 65)
        val toHash = UByteArray(1) { mainAddress.netType.INTEGRATED } concat mAddrKeys concat paymentID.asUByteArray()
        val checksum = Keccak.fullChecksum(toHash)
        bytes = toHash concat checksum
        MoneroAddress.validateChecksum(bytes, "Integrated address creation")
        address = Base58.encode(BinHexUtils.ubinaryToHex(bytes))
        validate()
    }

    override fun validate() {
        super.validate()
        if (paymentID.size == 32)
            throw MoneroException("Legacy payment ID's are not supported!")
        if (paymentID.size != 8)
            throw MoneroException("Payment ID's must be 8 bytes long!")
    }

    companion object {
        const val ADDRESS_LENGTH = 106
        const val ADDRESS_BYTES = 77
    }
}