package com.svega.moneroutils.crypto.slowhash

import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertTrue

class ScratchpadTest{
    @ExperimentalUnsignedTypes
    @Test
    fun `Scratchpads function the same`(){
        val sp1 = UByteArrayScratchpad(8)
        val sp2 = ByteBufferScratchpad(8)
        sp1[0] = 8u
        sp2[0] = 8u
        assertTrue("Pointer values are different!"){ sp1.getPointer(0).toULongPointer()[0].equals(sp2.getPointer(0).toULongPointer()[0])}//("${sp1.getPointer(0).toULongPointer()[0]}: ${sp2.getPointer(0).toULongPointer()[0]}")

        val b = ByteBuffer.allocate(8)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(0, 2)
        //println(b.asIntBuffer().get(0))
        val arr = b.array()
        arr[1] = 3
        b.put(2, 4)
        //println(arr[2])
        //println(b.asIntBuffer().get(0))
    }
}