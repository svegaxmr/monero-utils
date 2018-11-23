package com.svega.moneroutils.crypto

import com.svega.crypto.common.CryptoOps.sc_reduce32
import com.svega.moneroutils.MoneroException
import com.svega.moneroutils.crypto.slowhash.Keccak
import com.svega.moneroutils.varIntData

@ExperimentalUnsignedTypes
object Hashing {
    fun hashToScalar(data: ByteArray): ByteArray{
        return sc_reduce32(Keccak.rawKeccak(data))
    }
    fun getBlobHash(s: ByteArray): ByteArray {
        return Keccak.rawKeccak(s)
    }
    fun getObjectHash(s: ByteArray, res: ByteArray): Boolean {
        System.arraycopy(Keccak.rawKeccak(s.size.varIntData() + s), 0, res, 0, 32)
        return true
    }
    fun getObjectHash(s: MoneroSerializable, res: ByteArray): Boolean {
        System.arraycopy(getBlobHash(s.toBlob()), 0, res, 0, 32)
        return true
    }
    fun treeHash(b: Array<ByteArray>, res: ByteArray) {
        if(b.isEmpty())
            throw MoneroException("Treehash must have minimum one crypto")
        when {
            b.size == 1 -> System.arraycopy(b[0], 0, res, 0, 32)
            b.size == 2 -> System.arraycopy(Keccak.rawKeccak(b[0] + b[1]), 0, res, 0, 32)
            else -> {
                var i = 0
                var j = 0
                var cnt = treeHashCount(b.size)
                TODO()
            }
        }
    }
    private fun treeHashCount(count: Int): Int{
        assert( count >= 3 ) // cases for 0,1,2 are handled elsewhere
        assert( count <= 0x10000000 ) // sanity limit to 2^28, MSB=1 will cause an inf loop

        var pow = 2
        while (pow < count) pow = pow shl 1
        return pow shr 1
    }
}