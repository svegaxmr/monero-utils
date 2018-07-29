package com.svega.moneroutils.transactions

import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.addresses.MoneroAddress
import com.svega.moneroutils.blocks.getVarInt
import java.nio.ByteBuffer

data class TransactionOutput(val address: MoneroAddress, val amount: XMRAmount){
    companion object {
        fun parseFromBlob(bb: ByteBuffer){
            val ver = bb.getVarInt()
            val ulTime = bb.getVarInt()
            val inputNums = bb.getVarInt()
            println("TXOut: Ver $ver, unlocks at $ulTime, inputNums is $inputNums")
        }
    }
}