package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.BinHexUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class BlakeTest {

    @ExperimentalUnsignedTypes
    @Test
    fun `Blake Test`() {
        val lines = Files.readAllLines(Paths.get("C:\\wksp\\monero-utils\\src\\test\\java\\com\\svega\\moneroutils\\crypto\\slowhash\\blake_tests.txt"))
        val out = Scratchpad.getScratchpad(32)
        for (t in lines) {
            val spl = t.split(" ")
            val data = BinHexUtils.hexToByteArray(spl[1]).toUByteArray()
            val don = Scratchpad.getScratchpad(data.size)
            don[0] = data
            Blake.blake256Hash(out.getPointer(0), don.getPointer(0), data.size.toULong())
            val get = BinHexUtils.binaryToHex(out[0, out.size].toByteArray())
            assertTrue("${spl[1]} fails: expect ${spl[0]}, get $get", get.equals(spl[0], ignoreCase = true))
        }
    }
}