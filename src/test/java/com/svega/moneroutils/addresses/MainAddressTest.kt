package com.svega.moneroutils.addresses

import com.svega.moneroutils.Base58
import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.NetType
import org.junit.Test

@ExperimentalUnsignedTypes
class MainAddressTest {

    @Test
    fun genSubaddress() {

        val mAddr = MainAddress(secretSpendKey = SecretKey(BinHexUtils.hexToByteArray("fee20bf1e36ea0fdbb95c1a98714420c19f63726998487a615a4223e55b3c407")),
                net = NetType.MAINNET)

        //val aAddr = MoneroAddress.stringToAddress("87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVSwzFuXo")
        val tAddr = mAddr.genSubaddress(0, 1)
        val a11act = MoneroAddress.stringToAddress("89X5BfXPXDefd6oS27JnFmakXStmhSkmXVseqTagN7ooKQJN429ViaPEDveWRN2EKnjUVgoiS1aD43fFkTmZ52zREyEyKYk")

        println(BinHexUtils.binaryToHex(tAddr.bytes))
        println(BinHexUtils.binaryToHex(a11act.bytes))

        val tAddr2 = mAddr.genSubaddress(1, 1)

        println("---------------")

        println(BinHexUtils.binaryToHex(tAddr2.bytes))

        println(BinHexUtils.binaryToHex(com.svega.moneroutils.crypto.slowhash.Keccak.fullChecksum(Base58.decode("87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVCSSwkPp"))))
        println(BinHexUtils.binaryToHex(com.svega.moneroutils.crypto.slowhash.Keccak.fullChecksum(BinHexUtils.hexToUByteArray("2ABA74EB26859CA5E6E8880CCFF13794C9C7D7D167FAB44AAC9F4DA33D16094A6E044378CE5FBB144F0DE13C6A923D4DFDEF8B71C43E6C8F0FE6840026BD94467B"))))

        //println(BinHexUtils.binaryToHex(Base58.decode("87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVCSSwkPp")))
        //println(BinHexUtils.binaryToHex(Base58.decode("87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVSwzFuXo")))

        //println("---------------")


        //println("00")
        //println(mAddr.key.getAddressString(AddressType.MAIN, NetType.MAINNET))

       // println(tAddr.address)
        //println("t then A")
        //println(tAddr2.key.spend.public.str)
        //println(aAddr.key.spend.public.str)
        //println("----")
        //println(tAddr2.key.view.public.str)
        //println(aAddr.key.view.public.str)

        val s = MoneroAddress.stringToAddress(tAddr2.address)
        println(s.address)

        //assert(mAddr.genSubaddress(1, 1).address == "87VmKXcMdWqHCyvDoAZSwDEATFUW2Y5443A7GseCna6EiT52tctN9xVTWRJeJLkUKfVrZHT31Pfg74ecKZiTvHjVCSSwkPp")
        //println("02")
        //println(mAddr.genSubaddress(0, 2).address)
        //println("10")
        //println(mAddr.genSubaddress(1, 0).address)
        //println("11")
        //println(mAddr.genSubaddress(1, 1).address)

        //89X5BfXPXDefd6oS27JnFmakXStmhSkmXVseqTagN7ooKQJN429ViaPEDveWRN2EKnjUVgoiS1aD43fFkTmZ52zREyEyKYk
    }
}