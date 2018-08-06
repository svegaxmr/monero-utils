package com.svega.moneroutils.blocks

import com.svega.moneroutils.BinHexUtils
import java.nio.ByteBuffer
import java.util.*

class BlockHeader {
    var major = -1
        private set
    var minor = -1
        private set
    var timestamp = Date(0)
        private set
    var lastHash = ByteArray(32)
        private set
    var nonce = 0L
        private set
    companion object {
        fun parseBlockBlobHeader(buffer: ByteBuffer): BlockHeader {
            val blockHeader = BlockHeader()
            blockHeader.major = buffer.getVarInt()
            blockHeader.minor = buffer.getVarInt()
            blockHeader.timestamp = Date(buffer.getVarLong() * 1000L)
            blockHeader.lastHash = ByteArray(32)
            buffer.get(blockHeader.lastHash)
            blockHeader.nonce = SWAP32(buffer.int).toLong() and 0xffffffffL
            return blockHeader
        }

        fun SWAP32(x: Int) = ((( (x) and 0x000000ff) shl 24) or (( (x) and 0x0000ff00) shl  8)
                or (( (x) and 0x00ff0000) shr  8) or (( (x.toLong()) and 0xff000000L) ushr 24).toInt())
    }

    override fun toString(): String {
        return "BlockHeader(major=$major, minor=$minor, timestamp=$timestamp, lastHash=${BinHexUtils.binaryToHex(lastHash)}, nonce=$nonce)"
    }
}