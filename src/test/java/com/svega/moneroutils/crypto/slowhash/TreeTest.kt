package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.crypto.Hashing
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class TreeTest {

    @ExperimentalUnsignedTypes
    @Test
    fun treeHash() {
        val lines = Files.readAllLines(Paths.get("C:\\wksp\\monero-utils\\src\\test\\java\\com\\svega\\moneroutils\\crypto\\slowhash\\tree_tests.txt"))
        val res = ByteArray(32)
        for (t in lines) {
            val spl = t.split(" ")
            val data = BinHexUtils.hexToByteArray(spl[1])
            val arr = Array(data.size / 32) { p1 ->
                ByteArray(32) { pos ->
                    data[(p1 * 32) + pos]
                }
            }

            Hashing.treeHash(arr, res)
            val get = BinHexUtils.binaryToHex(res)
            assertTrue("${spl[1]} fails: expect ${spl[0]}, get $get", get.equals(spl[0], ignoreCase = true))
        }
    }
}