package com.svega.moneroutils.blocks

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.crypto.MoneroSerializable
import com.svega.moneroutils.crypto.slowhash.SlowHash
import com.svega.moneroutils.crypto.slowhash.SlowHashResult
import com.svega.moneroutils.getVarInt
import com.svega.moneroutils.transactions.Transaction
import com.svega.moneroutils.varIntData
import com.svega.moneroutils.writeVarInt
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalUnsignedTypes
open class Block : MoneroSerializable {
    lateinit var blockHeader: BlockHeader
    lateinit var coinbase: Transaction
    lateinit var byteBuffer: ByteBuffer
    val txids = ArrayList<ByteArray>()
    var numRCTSigs = -1
    val hasRCTSigs get() = blockHeader.major >= 4
    var isHashValid = false
    var hash = ByteArray(0)
        private set
        get() {
            if (isHashValid)
                return field
            else {
                val res = ByteArray(32)
                isHashValid = getBlockHash(this, res)
                field = res
            }
            return if (isHashValid) field else ByteArray(0)
        }

    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        val d = DataOutputStream(b)
        d.write(blockHeader.toBlob())
        d.write(coinbase.toBlob())
        d.writeVarInt(txids.size)
        for (i in txids) {
            d.write(i)
        }
        if (hasRCTSigs)
            d.writeVarInt(numRCTSigs)
        return b.toByteArray()
    }

    fun getPOWHash(nonce: Long = blockHeader.nonce): SlowHashResult {
        blockHeader.nonce = nonce
        val toHash = getBlockHashingBlob(this)
        val res = UByteArray(32)
        SlowHash.cnSlowHash(toHash.asUByteArray(), res, hashVer(blockHeader.major))
        return SlowHashResult(this, nonce, res)
    }

    companion object {
        val hashVer = { i: Int ->
            when (i) {
                1, 2, 3, 4, 5, 6 -> 0
                7 -> 1
                8, 9 -> 2
                else -> throw RuntimeException("Block version $i is not supported!")
            }
        }

        fun parseBlobHeader(header: ByteArray): Block {
            val block = Block()
            with(block) {
                byteBuffer = ByteBuffer.wrap(header).rewind()

                blockHeader = BlockHeader.parseBlockBlobHeader(byteBuffer)
                coinbase = Transaction.parseBlobHeader(byteBuffer)

                val numTxes = byteBuffer.getVarInt()
                val txid = ByteArray(32)
                for (i in 0 until numTxes) {
                    byteBuffer.get(txid)
                    txids.add(Arrays.copyOf(txid, 32))
                }

                if (hasRCTSigs and (byteBuffer.remaining() > 0)) {
                    numRCTSigs = byteBuffer.getVarInt()
                }
            }
            return block
        }

        var blockHashesCalculated = 0
        private fun getBlockHash(b: Block, res: ByteArray): Boolean {
            ++blockHashesCalculated
            val ret = calculateBlockHash(b, res)
            if (!ret)
                return false
            return true
        }

        fun getBlockHashingBlob(b: Block): ByteArray {
            val hBlob = b.blockHeader.toBlob()
            val trHash = getTXTreeHash(b)
            return hBlob + trHash + (b.txids.size + 1).varIntData()
        }

        fun getTXTreeHash(b: Block): ByteArray {
            val txIDs = Array(1 + b.txids.size) { ByteArray(32) }
            var place = 0
            val h = ByteArray(32)
            Transaction.getTXHash(b.coinbase, h)
            txIDs[place++] = h
            for (t in b.txids)
                txIDs[place++] = t
            return getTXTreeHash(txIDs)
        }

        fun getTXTreeHash(b: Array<ByteArray>): ByteArray {
            val ret = ByteArray(32)
            Hashing.treeHash(b, ret)
            return ret
        }

        private val correct202612 = BinHexUtils.hexToByteArray("3a8a2b3a29b50fc86ff73dd087ea43c6f0d6b8f936c849194d5c84c737903966")
        private val existing202612 = BinHexUtils.hexToByteArray("bbd604d2ba11ba27935e006ed39c9bfdd99b76bf4a50654bc1e1e61217962698")
        private fun calculateBlockHash(b: Block, res: ByteArray): Boolean {
            val blobHash = Hashing.getBlobHash(b.toBlob())

            if (blobHash.contentEquals(existing202612)) {
                System.arraycopy(correct202612, 0, res, 0, 32)
                return true
            }

            val result = Hashing.getObjectHash(getBlockHashingBlob(b), res)

            if (result) {
                if (res.contentEquals(existing202612)) {
                    System.arraycopy(ByteArray(32), 0, res, 0, 32)
                    return false
                }
            }

            return result
        }
    }

    override fun toString(): String {
        return "Block(\n\tblockHeader=$blockHeader,\n\tcoinbase=$coinbase, txids=$txids)"
    }
}