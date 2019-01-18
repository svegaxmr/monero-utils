package com.svega.moneroutils.addresses

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.NetType
import org.junit.Test
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class MainAddressTest {
    @Test
    fun genSubaddress() {
        val mAddr = MainAddress(secretSpendKey = SecretKey(BinHexUtils.hexToByteArray("fee20bf1e36ea0fdbb95c1a98714420c19f63726998487a615a4223e55b3c407")),
                net = NetType.MAINNET)

        assertTrue { mAddr.genSubaddress(0, 0).address == mAddr.address }

        val tAddr = mAddr.genSubaddress(0, 1)
        val a11act = MoneroAddress.stringToAddress("89X5BfXPXDefd6oS27JnFmakXStmhSkmXVseqTagN7ooKQJN429ViaPEDveWRN2EKnjUVgoiS1aD43fFkTmZ52zREyEyKYk")
        assertTrue("${tAddr.address} should equal ${a11act.address}") { tAddr.address == a11act.address }

        val tAddr2 = mAddr.genSubaddress(0, 2)

        assertTrue { tAddr2.address == "82ajGY3zUSBfiJmYz4af9kMXVQoHHpLiYdHNLNaFL8MZ1i8FG38VepECLqM7Vjdef4YWYr6wKyBGbHAMaRrUeoh74QKPhPB" }

        assertTrue { mAddr.genSubaddress(1, 1).address == "87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVCSSwkPp" }
    }
}