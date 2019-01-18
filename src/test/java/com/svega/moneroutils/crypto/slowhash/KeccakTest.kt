package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.BinHexUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class KeccakTest {
    @ExperimentalUnsignedTypes
    @Test
    fun `Test keccakf1600`() {
        val lines = Files.readAllLines(Paths.get("C:\\wksp\\monero-utils\\src\\test\\java\\com\\svega\\moneroutils\\crypto\\slowhash\\fast_tests.txt"))
        val ret = Scratchpad.getScratchpad(200)

        for (t in lines) {
            val spl = t.split(" ")
            val data = BinHexUtils.hexToByteArray(spl[1]).toUByteArray()
            val sp2 = Scratchpad.getScratchpad(data.size)
            sp2[0] = data
            Keccak.keccak(sp2.getPointer(0), ret.getPointer(0))
            val get = BinHexUtils.binaryToHex(ret[0, ret.size].toByteArray())
            assertTrue("${spl[1]} fails: expect ${spl[0]}, get $get", get.startsWith(spl[0], ignoreCase = true))
        }
    }
}