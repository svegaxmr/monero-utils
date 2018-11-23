package com.svega.moneroutils.transactions

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*

class TransactionInput private constructor(): MoneroSerializable{
    var height  = -1
        private set
    var inputType = -1
        private set
    var amount = XMRAmount(0)
        private set
    var keyOffsets = ArrayList<Long>()
        private set
        get() = ArrayList(field)
    var keyImage = ByteArray(0)
        private set
        get() = Arrays.copyOf(field, field.size)
    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        val d = DataOutputStream(b)
        d.write(inputType)
        when(inputType) {
            0x02 -> {
                d.writeVarLong(amount.atomicUnits)
                d.writeVarInt(keyOffsets.size)
                for(l in keyOffsets){
                    d.writeVarLong(l)
                }
                d.write(keyImage)
            }
            0xFF -> {
                d.writeVarInt(height)
            }
            else -> TODO()
        }
        return b.toByteArray()
    }
    companion object {
        fun parseFromBlob(bb: ByteBuffer): TransactionInput {
            val ret = TransactionInput()
            with(ret) {
                inputType = (bb.get().toInt() and 0xFF)
                when (inputType) {
                    0x02 -> {
                        amount = XMRAmount(bb.getVarLong())
                        val numKOffsets = bb.getVarInt()
                        val tkeyOffsets = ArrayList<Long>()
                        for(i in 0 until numKOffsets){
                            tkeyOffsets.add(bb.getVarLong())
                        }
                        keyOffsets = tkeyOffsets
                        val kImg = ByteArray(32)
                        bb.get(kImg)
                        keyImage = kImg
                    }
                    0xFF -> {
                        height = bb.getVarInt()
                    }
                    else -> TODO("Transaction onputType $inputType is not implemented")
                }
            }
            return ret
        }
    }

    override fun toString(): String {
        return "TransactionInput(inputType=$inputType, amount=${amount.toOther(AmountDivision.WHOLE)})"
    }
}