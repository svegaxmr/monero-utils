package com.svega.moneroutils.crypto

import com.svega.crypto.common.CryptoOps.sc_reduce32
import com.svega.moneroutils.crypto.slowhash.Keccak
import com.svega.moneroutils.exceptions.HashingException
import com.svega.moneroutils.varIntData

@ExperimentalUnsignedTypes
object Hashing {
    @Suppress("MemberVisibilityCanBePrivate")
    const val HASH_SIZE = 32
    @JvmStatic
    fun hashToScalar(data: ByteArray): ByteArray {
        return sc_reduce32(Keccak.rawKeccak(data))
    }

    @JvmStatic
    fun getBlobHash(s: ByteArray): ByteArray {
        return Keccak.rawKeccak(s)
    }

    @JvmStatic
    fun getObjectHash(s: ByteArray, res: ByteArray): Boolean {
        System.arraycopy(Keccak.rawKeccak(s.size.varIntData() + s), 0, res, 0, HASH_SIZE)
        return true
    }

    @JvmStatic
    fun getObjectHash(s: MoneroSerializable, res: ByteArray): Boolean {
        System.arraycopy(getBlobHash(s.toBlob()), 0, res, 0, HASH_SIZE)
        return true
    }

    @Throws(HashingException::class)
    @JvmStatic
    fun treeHash(b: Array<ByteArray>, res: ByteArray) {
        when {
            b.isEmpty() -> throw HashingException("Treehash must have minimum one hash!")
            b.size == 1 -> System.arraycopy(b[0], 0, res, 0, HASH_SIZE)
            b.size == 2 -> System.arraycopy(Keccak.rawKeccak(b[0] + b[1]), 0, res, 0, HASH_SIZE)
            else -> {
                var i: Int
                var j: Int
                var cnt = treeHashCount(b.size)
                val count = b.size

                val ints = ByteArray(cnt * HASH_SIZE)

                var longHash = b[0]
                for (q in 1 until b.size) {
                    longHash += b[q]
                }
                longHash.copyOfRange(0, (2 * cnt - b.size) * HASH_SIZE).copyInto(ints)

                i = 2 * cnt - count
                j = 2 * cnt - count
                while (j < cnt) {
                    Keccak.rawKeccak(longHash.copyOfRange((i * HASH_SIZE), (i * HASH_SIZE) + 64)).copyInto(ints, destinationOffset = (j * HASH_SIZE))
                    i += 2
                    ++j
                }

                if(i != b.size) {
                    throw HashingException("TreeHash checks failed!")
                }

                while (cnt > 2) {
                    cnt = cnt shr 1
                    i = 0
                    j = 0
                    while (j < cnt) {
                        Keccak.rawKeccak(ints.copyOfRange((i * HASH_SIZE), (i * HASH_SIZE) + 64)).copyInto(ints, destinationOffset = (j * HASH_SIZE))
                        i += 2
                        ++j
                    }
                }

                Keccak.rawKeccak(ints.copyOfRange(0, 64)).copyInto(res)
            }
        }
    }

    private fun treeHashCount(count: Int): Int {
        if(count < 3) {// cases for 0,1,2 are handled elsewhere
            throw HashingException("TreeHashCount needs minimum 3!")
        }
        if(count > 0x10000000) {// sanity limit to 2^28, MSB=1 will cause an inf loop
            throw HashingException("Sanity limit: TreeHashCount does not work with > 2^28")
        }

        var pow = 2
        while (pow < count) pow = pow shl 1
        return pow shr 1
    }
}