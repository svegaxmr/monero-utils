package com.svega.moneroutils.transactions

abstract class MoneroTransaction(outs: ArrayList<TransactionOutput>) {
    protected val outputs = outs
    fun getOutputList() = outputs
}