package com.svega.moneroutils.blocks

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.transactions.CoinbaseTransaction
import com.svega.moneroutils.transactions.MoneroTransaction
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

open class Block private constructor() {
    var major = -1
    var minor = -1
    var timestamp = Date(0)
    var lastHash = ByteArray(32)
    var nonce = 0L
    lateinit var coinbase: CoinbaseTransaction
    var extra = ByteArray(0)
    var txids = ArrayList<ByteArray>() //TODO: Daemon?
    var txes = ArrayList<MoneroTransaction>() //TODO: Daemon?
    lateinit var byteBuffer: ByteBuffer
    companion object {
        fun parseBlobHeader(header: String){
            val block = Block()
            block.byteBuffer = ByteBuffer.allocateDirect(header.length / 2).put(BinHexUtils.hexToByteArray(header)).rewind()
            block.major = block.byteBuffer.getVarInt()
            block.minor = block.byteBuffer.getVarInt()
            block.timestamp = Date(block.byteBuffer.getVarInt() * 1000L)
            block.lastHash = ByteArray(32)
            block.byteBuffer.get(block.lastHash)
            block.nonce = SWAP32(block.byteBuffer.int).toLong() and 0xffffffffL
            println("Block ver ${block.major}.${block.minor} at time ${block.timestamp} with a previous hash of " +
                    "${BinHexUtils.binaryToHex(block.lastHash)} and nonce ${block.nonce}")
            CoinbaseTransaction.parseFromBlob(block.byteBuffer)
            val extraSize = block.byteBuffer.getVarInt()
            block.extra = ByteArray(extraSize)
            block.byteBuffer.get(block.extra)
            println("Extra is ${BinHexUtils.binaryToHex(block.extra)}")
            val numRCTSigs = block.byteBuffer.getVarInt()
            println("numRCTSigs is $numRCTSigs")
            val numTxes = block.byteBuffer.getVarInt()
            val txid = ByteArray(32)
            for(i in 0 until numTxes){
                block.byteBuffer.get(txid)
                block.txids.add(Arrays.copyOf(txid, 32))
                println("Has tx ${BinHexUtils.binaryToHex(txid)}")
            }
        }

        fun SWAP32(x: Int) = ((( (x) and 0x000000ff) shl 24) or (( (x) and 0x0000ff00) shl  8)
                or (( (x) and 0x00ff0000) shr  8) or (( (x.toLong()) and 0xff000000L) ushr 24).toInt())
    }
}

fun ByteBuffer.getVarLong(): Long {
    var tmp: Long
    tmp = this.get().toLong()
    if (tmp >= 0) {
        return tmp
    }
    var result = tmp and 0x7f
    tmp = this.get().toLong()
    if (tmp >= 0) {
        result = result or (tmp shl 7)
    } else {
        result = result or (tmp and 0x7f shl 7)
        tmp = this.get().toLong()
        if (tmp >= 0) {
            result = result or (tmp shl 14)
        } else {
            result = result or (tmp and 0x7f shl 14)
            tmp = this.get().toLong()
            if (tmp >= 0) {
                result = result or (tmp shl 21)
            } else {
                result = result or (tmp and 0x7f shl 21)
                tmp = this.get().toLong()
                if (tmp >= 0) {
                    result = result or (tmp shl 28)
                } else {
                    result = result or (tmp and 0x7f shl 28)
                    tmp = this.get().toLong()
                    if (tmp >= 0) {
                        result = result or (tmp shl 35)
                    } else {
                        result = result or (tmp and 0x7f shl 35)
                        tmp = this.get().toLong()
                        if (tmp >= 0) {
                            result = result or (tmp shl 42)
                        } else {
                            result = result or (tmp and 0x7f shl 42)
                            tmp = this.get().toLong()
                            if (tmp >= 0) {
                                result = result or (tmp shl 49)
                            } else {
                                result = result or (tmp and 0x7f shl 49)
                                tmp = this.get().toLong()
                                if (tmp >= 0) {
                                    result = result or (tmp shl 56)
                                } else {
                                    result = result or (tmp and 0x7f shl 56)
                                    result = result or (this.get().toLong() shl 63)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return result
}

fun ByteBuffer.getVarInt(): Int {
    var tmp: Int = this.get().toInt()
    if ((tmp) >= 0) {
        return tmp
    }
    var result = tmp and 0x7f
    tmp = this.get().toInt()
    if (tmp >= 0) {
        result = result or (tmp shl 7)
    } else {
        result = result or (tmp and 0x7f shl 7)
        tmp = this.get().toInt()
        if (tmp >= 0) {
            result = result or (tmp shl 14)
        } else {
            result = result or (tmp and 0x7f shl 14)
            tmp = this.get().toInt()
            if ((tmp) >= 0) {
                result = result or (tmp shl 21)
            } else {
                result = result or (tmp and 0x7f shl 21)
                tmp = this.get().toInt()
                result = result or (tmp shl 28)
                while (tmp < 0) {
                    // We get into this loop only in the case of overflow.
                    // By doing this, we can call getVarInt() instead of
                    // getVarLong() when we only need an int.
                    tmp = this.get().toInt()
                }
            }
        }
    }
    return result
}

fun main(args: Array<String>){
    Block.parseBlobHeader("0707bafbe4d9056a441458c221dca048688c03d4a1477ca10e7442d40d1413fa17aeb74f79e6eaef64004002948e6201ffd88d6201c1d7d4b9ea7d026e8db50407311eb0b808e54f4e16dd0092828f9e9834790e7a6faf2f0e1c014c2b01373ded292eb617006956ffca81aad9028113d65b0c7b6273eaae85e705e4450d02080000010f14070000000402f4dee22e98d140355086639317fc41605f6da8c6d2a366727b59ae049cff4ff9063b712364969cab347d28f1f7c0ca4b1b56ce65d6204873e93588c7c8ef1ff5c9cadb3dd20f02c0116908625903da7e90996b8757c14c514c39d4dc9ce513e8afbc755e33f9374c78bfd4aa63684213f707e238cd6fc00d4fc6c6d1b83855")
}