package com.svega.moneroutils.addresses

import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class IntegratedAddressTest{
    @Test
    fun `Main Address and Payment ID`(){
        val iAddrStr = "4LL9oSLmtpccfufTMvppY6JwXNouMBzSkbLYfpAV5Usx3skxNgYeYTRj5UzqtReoS44qo9mtmXCqY45DJ852K5Jv2bYXZKKQePHES9khPK"
        val mAddrStr = "4AdUndXHHZ6cfufTMvppY6JwXNouMBzSkbLYfpAV5Usx3skxNgYeYTRj5UzqtReoS44qo9mtmXCqY45DJ852K5Jv2684Rge"
        val mAddr = MoneroAddress.stringToAddress(mAddrStr) as MainAddress
        val iAddr = IntegratedAddress(mAddr, "8a125052fe6f3877")
        assertEquals(iAddrStr, iAddr.address)
    }
}