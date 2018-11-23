package com.svega.moneroutils

import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalUnsignedTypes
class Base58Test{
    @Test
    fun `Test Encode`(){
        val shouldBe = "4BDLzJ4Mj811RVm3JagmGiK46cwJrcaWrLjFNST4tzE8YrcK8q5Z2B2QG1SRimoRuvBMECbjFaU6z7rWy3CbfoEDMU5AMjM"
        val hex = "12fd12679548b32e028667aa0d41e7076bef275d3085178f75f2cde21a79f625be7356d032ff58698b0e139bb6aaae6d3ddf033302a46b6f28f9ce800028d2e6b5688434f0"
        val returned = Base58.encode(hex)

        assertTrue("Encode does not match! $shouldBe, $returned",  shouldBe == returned)
    }

    @Test
    fun `Test Decode`(){
        val test = "4BDLzJ4Mj811RVm3JagmGiK46cwJrcaWrLjFNST4tzE8YrcK8q5Z2B2QG1SRimoRuvBMECbjFaU6z7rWy3CbfoEDMU5AMjM"
        val ret = BinHexUtils.hexToUByteArray("12fd12679548b32e028667aa0d41e7076bef275d3085178f75f2cde21a79f625be7356d032ff58698b0e139bb6aaae6d3ddf033302a46b6f28f9ce800028d2e6b5688434f0")
        val returned = Base58.decode(test)

        assertTrue("Decode does not match! ${BinHexUtils.binaryToHex(ret)}," +
                " ${BinHexUtils.binaryToHex(returned)}",  ret.contentEquals(returned))
    }
}