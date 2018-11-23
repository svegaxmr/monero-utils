package com.svega.moneroutils.transactions

import com.svega.moneroutils.*
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.util.*

class TransactionOutput private constructor(): MoneroSerializable{
    var amount = XMRAmount(-1)
        private set
    var outputType = -1
        private set
    var key = ByteArray(0)
        private set
        get() = Arrays.copyOf(field, field.size)
    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        val d = DataOutputStream(b)
        d.writeVarLong(amount.atomicUnits)
        d.write(outputType)
        d.write(key)
        return b.toByteArray()
    }
    companion object {
        fun parseFromBlob(bb: ByteBuffer): TransactionOutput {
            val ret = TransactionOutput()
            with(ret) {
                amount = XMRAmount(bb.getVarLong())
                outputType = (bb.get().toInt() and 0xFF)
                if(outputType != 2)
                    TODO("only txout_to_key supported, $outputType selected")
                val tkey = ByteArray(32)
                bb.get(tkey)
                key = tkey
            }
            return ret
        }
    }

    override fun toString(): String {
        return "TransactionOutput(amount=${amount.toOther(AmountDivision.WHOLE)}, outputType=$outputType)"
    }
}