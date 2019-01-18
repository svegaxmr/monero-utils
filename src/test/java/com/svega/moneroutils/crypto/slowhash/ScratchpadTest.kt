package com.svega.moneroutils.crypto.slowhash

import org.junit.Test
import kotlin.test.assertTrue

class ScratchpadTest {
    @ExperimentalUnsignedTypes
    @Test
    fun `Scratchpads function the same`() {
        val sp1 = UByteArrayScratchpad(8)
        val sp2 = ByteBufferScratchpad(8)
        sp1[0] = 8u
        sp2[0] = 8u
        assertTrue("Pointer values are different!") { sp1.getPointer(0).toULongPointer()[0] == sp2.getPointer(0).toULongPointer()[0] }//("${sp1.getPointer(0).toULongPointer()[0]}: ${sp2.getPointer(0).toULongPointer()[0]}")
    }
}