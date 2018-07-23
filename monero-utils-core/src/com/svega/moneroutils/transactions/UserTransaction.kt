package com.svega.moneroutils.transactions

import com.svega.moneroutils.MoneroException

open class UserTransaction(outs: ArrayList<TransactionOutput>, ins: ArrayList<MoneroTransaction>, fee_: Long): MoneroTransaction(outs) {
    val fee = fee_
    val inputs = ins
    companion object {
        fun begin() = TransactionBuilder()
    }
}

class TransactionBuilder{
    val outputs = ArrayList<TransactionOutput>()
    val inputs = ArrayList<MoneroTransaction>()
    var fee = 0L
    @Throws(MoneroException::class)
    fun build() : UserTransaction{
        val netType = outputs[0].address.net
        if(outputs.any { it.address.net != netType }){
            throw MoneroException("Output not all net type ${outputs[0].address.net}")
        }
        if(inputs.any { it.getOutputList().any{ it.address.net != netType } }){
            throw MoneroException("Input outs not all net type ${outputs[0].address.net}")
        }
        return UserTransaction(outputs, inputs, fee)
    }
}