package com.svega.moneroutils.blocks

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.transactions.CoinbaseTransaction
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

open class Block private constructor() {
    lateinit var blockHeader: BlockHeader
    lateinit var coinbase: CoinbaseTransaction
    var extra = ByteArray(0)
    var unknown = -1
    var txids = ArrayList<ByteArray>()
    lateinit var byteBuffer: ByteBuffer
    companion object {
        fun parseBlobHeader(header: String): Block {
            val block = Block()
            with(block){
                byteBuffer = ByteBuffer.allocateDirect(header.length / 2).put(BinHexUtils.hexToByteArray(header)).rewind()

                blockHeader = BlockHeader.parseBlockBlobHeader(byteBuffer)
                coinbase = CoinbaseTransaction.parseFromBlob(byteBuffer)

                if(blockHeader.major > 1) {
                    val numRCTSigs = byteBuffer.getVarInt()
                    println("numRCTSigs is $numRCTSigs")
                }else{
                    println("maj v1 has no rctsigs")
                }
                byteBuffer.printNextBytes(4)
                unknown = byteBuffer.getVarInt()
                println("unknown is $unknown")
                val numTxes = byteBuffer.getVarInt()
                println("there are $numTxes tx in block")
                val txid = ByteArray(32)
                for(i in 0 until numTxes){
                    byteBuffer.get(txid)
                    txids.add(Arrays.copyOf(txid, 32))
                }
                val extraSize = byteBuffer.getVarInt()
                extra = ByteArray(extraSize)
                byteBuffer.get(extra)
                println("Extra is ${BinHexUtils.binaryToHex(extra)}")
            }
            return block
        }
    }
}

fun ByteBuffer.getVarLong(): Long {
    var tmp = this.get().toLong()
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

fun ByteBuffer.printNextBytes(n: Int){
    mark()
    val one = ByteArray(n)
    get(one)
    reset()
    println("next is ${BinHexUtils.binaryToHex(one)}")
}