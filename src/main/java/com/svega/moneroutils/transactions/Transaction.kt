package com.svega.moneroutils.transactions

import com.svega.moneroutils.AmountDivision
import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.crypto.MoneroSerializable
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.ByteBuffer
import java.net.HttpURLConnection
import java.nio.file.Files
import java.nio.file.Paths


class Transaction private constructor(): MoneroSerializable{
    lateinit var header: TransactionPrefix
        private set
    var totalOut = XMRAmount(-1)
        private set
    var isHashValid = false
        private set
    var hash =  ByteArray(0)
        private set
        get() {
            if(isHashValid)
                return field
            else{
                val res = ByteArray(32)
                isHashValid = getTXHash(this, res)
                field = res
            }
            return if(isHashValid) field else ByteArray(0)
        }

    override fun toBlob(): ByteArray {
        val b = ByteArrayOutputStream()
        b.write(header.toBlob())
        return b.toByteArray()
    }
    companion object {
        fun parseBlobHeader(blob: ByteArray) = parseBlobHeader(ByteBuffer.wrap(blob).rewind())

        fun parseBlobHeader(bb: ByteBuffer): Transaction {
            val tx = Transaction()
            with(tx) {
                header = TransactionPrefix.parseFromBlob(bb)
                for(t in header.outs){
                    totalOut += t.amount
                }
            }
            return tx
        }

        var txHashesCalculated = 0
        fun getTXHash(t: Transaction, res: ByteArray): Boolean{
            ++txHashesCalculated
            return calculateTXHash(t, res)
        }

        private fun calculateTXHash(t: Transaction, res: ByteArray): Boolean{
            if(t.header.ver == 1){
                return Hashing.getObjectHash(t, res)
            }

            val hashes = Array(3) {ByteArray(32)}

            //rct

            TransactionPrefix.getTXPrefixHash(t, hashes[0])

            return true
        }
    }

    override fun toString(): String {
        return "Transaction(header=$header, totalOut=${totalOut.toOther(AmountDivision.WHOLE)})"
    }

}

typealias CoinbaseTransaction = Transaction

fun main(args: Array<String>){
    val address = URL("http://node.moneroworld.com:18089/get_transaction_pool")
    val hc = address.openConnection() as HttpURLConnection


    hc.doOutput = true
    hc.doInput = true
    hc.useCaches = false
    hc.setRequestProperty("Content-Type", "application/json")

    val path = Paths.get("out.json")
    /*Files.delete(path)

    Files.copy(hc.inputStream, path)*/

    Transaction.parseBlobHeader(Files.readAllBytes(path))
}