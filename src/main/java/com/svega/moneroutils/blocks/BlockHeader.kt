package com.svega.moneroutils.blocks

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*

open class BlockHeader: MoneroSerializable{
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

    override fun toBlob(): ByteArray{
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