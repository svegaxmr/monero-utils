package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.blocks.Block
import com.svega.moneroutils.getSwappedULong

@ExperimentalUnsignedTypes
data class SlowHashResult(
    val block: Block,
    val nonce: Long,
    val hash: UByteArray
){
    val diff: Long = (0xFFFFFFFFFFFFFFFFUL / hash.getSwappedULong(3)).toLong()
}