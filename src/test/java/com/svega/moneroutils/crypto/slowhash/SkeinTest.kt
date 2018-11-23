package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.BinHexUtils
import org.junit.Assert
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

@ExperimentalUnsignedTypes
class SkeinTest {

    @Test
    fun `Test Skein`(){
        val lines = Files.readAllLines(Paths.get("C:\\wksp\\monero-utils\\src\\test\\java\\com\\svega\\moneroutils\\crypto\\slowhash\\skein_tests.txt"))
        val out = Scratchpad.getScratchpad(32)
        for(t in lines){
            val spl = t.split(" ")
            val data = BinHexUtils.hexToByteArray(spl[1]).toUByteArray()
            val don = Scratchpad.getScratchpad(data.size)
            don[0] = data
            Skein.hashExtraSkein(don.getPointer(0), data.size.toULong(), out.getPointer(0))
            val get = BinHexUtils.binaryToHex(out[0, out.size].toByteArray())
            Assert.assertTrue("${spl[1]} fails: expect ${spl[0]}, get $get", get.equals(spl[0], ignoreCase = true))
        }
    }
}