package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.blocks.Block
import java.math.BigInteger

@ExperimentalUnsignedTypes
data class SlowHashResult(
        val block: Block,
        val nonce: Long,
        val hash: UByteArray
) {
    val diff: Long = (top / BigInteger(hash.asByteArray().reversedArray())).toLong()
    companion object {
        private val top = BigInteger("2").pow(256).dec()
    }
}