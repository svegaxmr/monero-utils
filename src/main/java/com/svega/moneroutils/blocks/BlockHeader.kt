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

package com.svega.moneroutils.blocks

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*

@ExperimentalUnsignedTypes
open class BlockHeader : MoneroSerializable {
    var major = -1
    var minor = -1
    var timestamp = Date(0)
    var lastHash = ByteArray(32)
    var nonce = 0L

    companion object {
        fun parseBlockBlobHeader(buffer: ByteBuffer): BlockHeader {
            val blockHeader = BlockHeader()
            with(blockHeader) {
                major = buffer.getVarInt()
                minor = buffer.getVarInt()
                timestamp = Date(buffer.getVarLong() * 1000L)
                lastHash = ByteArray(32)
                buffer.get(lastHash)
                nonce = SWAP32(buffer.int).toLong() and 0xffffffffL
            }
            return blockHeader
        }
    }

    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        val d = DataOutputStream(b)
        d.writeVarInt(major)
        d.writeVarInt(minor)
        d.writeVarLong(timestamp.time / 1000L)
        d.write(lastHash)
        d.writeInt(SWAP32(nonce.toInt()))
        return b.toByteArray()
    }

    override fun toString(): String {
        return "BlockHeader(major=$major, minor=$minor, timestamp=$timestamp, lastHash=${BinHexUtils.binaryToHex(lastHash)}, nonce=$nonce)"
    }
}