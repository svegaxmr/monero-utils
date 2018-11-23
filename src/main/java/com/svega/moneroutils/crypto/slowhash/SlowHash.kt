package com.svega.moneroutils.crypto.slowhash

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.crypto.slowhash.AESB.aesb_single_round
import com.svega.moneroutils.crypto.slowhash.IntUtils.mul128
import com.svega.moneroutils.crypto.slowhash.SlowHash.INIT_SIZE_BYTE
import com.svega.moneroutils.crypto.slowhash.SlowHash.cnSlowHash
import com.svega.moneroutils.toDouble

@ExperimentalUnsignedTypes
object SlowHash {
    private const val MEMORY = (1 shl 21) // 2MB scratchpad
    private const val ITER = (1 shl 20)
    private const val AES_BLOCK_SIZE = 16
    private const val AES_KEY_SIZE = 32
    private const val INIT_SIZE_BLK = 8
    const val INIT_SIZE_BYTE = (INIT_SIZE_BLK * AES_BLOCK_SIZE)

    private val extra_hashes = Array(4) { { x: Scratchpad, y: Int, z: UBytePointer -> when(it) {
        0 -> Blake.blake256Hash(z, x.getPointer(0), y.toULong())
        1 -> Groestl.groestl256Hash(z, x.getPointer(0), y.toULong())
        2 -> JH.hash_extra_jh(x.getPointer(0), y, z)
        else -> Skein.hash_extra_skein(x.getPointer(0), y.toULong(), z)
    }
    }}

    fun mul(a: ULongPointer, b: ULongPointer, res: ULongPointer) {
        val (hi, lo) = mul128(a[0], b[0])
        res[0] = hi
        res[1] = lo
    }

    fun copyBlock(dst: UBytePointer, src: UBytePointer){
        dst[0] = src[0, AES_BLOCK_SIZE]
    }

    fun sum_half_blocks(a: ULongPointer, b: ULongPointer) {
        a[0] = a[0] + b[0]
        a[1] = a[1] + b[1]
    }

    private val t = Scratchpad.getScratchpad(16)
    fun swap_blocks(a: UBytePointer, b: UBytePointer){
        t[0] = a[0, 16]
        a[0] = b[0, 16]
        b[0] = t[0, 16]
    }

    fun xor_blocks(a: ULongPointer, b: ULongPointer) {
        a[0] = a[0] xor b[0]
        a[1] = a[1] xor b[1]
    }

    fun xor16(left: UBytePointer, right: UBytePointer) {
        for (i in 0 until 16) {
            left[i] = left[i] xor right[i]
        }
    }

    const val MASK = (((MEMORY / AES_BLOCK_SIZE) - 1) shl 4)
    fun state_index(x: UIntPointer) = (x[0].toInt() and MASK)

    fun cnSlowHash(data: UByteArray, hash: UByteArray, variant: Int){
        val long_state = Scratchpad.getScratchpad(MEMORY)
        val state = HashState()
        val text = Scratchpad.getScratchpad(INIT_SIZE_BYTE)
        val a = Scratchpad.getScratchpad(AES_BLOCK_SIZE).getPointer(0)
        val b = Scratchpad.getScratchpad(AES_BLOCK_SIZE * 2).getPointer(0)
        val bOff16 = (b + 16).toUBytePointer()
        val c = Scratchpad.getScratchpad(AES_BLOCK_SIZE).getPointer(0)
        val c1 = Scratchpad.getScratchpad(AES_BLOCK_SIZE).getPointer(0)
        val d = Scratchpad.getScratchpad(AES_BLOCK_SIZE).getPointer(0)
        val au = a.toUIntPointer()
        val al = a.toULongPointer()
        val bl = b.toULongPointer()
        val cl = c.toULongPointer()
        val c1i = c1.toUIntPointer()
        val c1l = c1.toULongPointer()
        val dl = d.toULongPointer()
        val oaesCtx = OAES.oaes_alloc()

        val spz = if(variant == 1) UByteArrayScratchpad(data.size) else Scratchpad.getScratchpad(data.size)//needed for tweak1_2 pointing at 35
        spz[0] = data
        Keccak.keccak(spz.getPointer(0), state.b)

        text[0] = state.init[0, INIT_SIZE_BYTE]

        OAES.oaes_key_import_data(oaesCtx, state.b[0, AES_KEY_SIZE])

        val tweak1_2 = when {
                variant != 1 -> 0uL
                data.size < 43 -> throw RuntimeException("Data must be at least 43 bytes")
                else -> state.w[24] xor spz.getPointer(35).toULongPointer()[0]
            }

        var division_result = 0uL
        var sqrt_result = 0uL
        if (variant >= 2){
            bl[2] = state.w[8] xor state.w[10]
            bl[3] = state.w[9] xor state.w[11]
            division_result = state.w[12]
            sqrt_result = state.w[13]
        }

        val tpj = text.getPointer(0)
        for (i in 0 until MEMORY / INIT_SIZE_BYTE) {
            tpj.offset = 0
            for (j in 0 until INIT_SIZE_BLK) {
                AESB.aesb_pseudo_round(tpj, tpj, oaesCtx.key.expData)
                tpj.offset += AES_BLOCK_SIZE
            }
            long_state[i * INIT_SIZE_BYTE] = text.getRawArray()
        }

        al[0] = state.w[0] xor state.w[4]
        al[1] = state.w[1] xor state.w[5]
        bl[0] = state.w[2] xor state.w[6]
        bl[1] = state.w[3] xor state.w[7]

        val lsp = long_state.getPointer(0)

        for (i in 0 until ITER / 2) {
            lsp.offset = state_index(au)
            aesb_single_round(lsp, lsp, a)
            copyBlock(c1, lsp)

            val oldOff1 = lsp.offset
            lsp.offset = 0
            VARIANT2_PORTABLE_SHUFFLE_ADD(lsp, oldOff1, al, bl, variant)
            lsp.offset = oldOff1

            xor16(lsp, b)

            if (variant == 1){
                val tmp = lsp[11].toInt()
                val index = (((tmp shr 3) and 6) or (tmp and 1)) shl 1
                lsp[11] = (tmp xor ((0x75310 shr index) and 0x30)).toUByte()
            }

            lsp.offset = state_index(c1i)
            copyBlock(c, lsp)

            //VARIANT2_PORTABLE_INTEGER_MATH(c, c1); b, ptr
            if (variant >= 2){
                cl[0] = cl[0] xor division_result xor (sqrt_result shl 32)
                val dividend = c1l[1]
                val divisor = ((c1l[0] + (sqrt_result shl 1).toUInt()) or 0x80000001UL).toUInt()
                division_result = ((dividend / divisor).toUInt()) + (((dividend % divisor).toULong()) shl 32)
                val sqrt_input = c1l[0] + division_result
                sqrt_result = (Math.sqrt(sqrt_input.toDouble() + 18446744073709551616.0) * 2.0 - 8589934592.0).toLong().toULong()
                val s = sqrt_result shr 1
                val b1 = sqrt_result and 1u
                val r2 = s * (s + b1) + (sqrt_result shl 32)
                sqrt_result += (if(r2 + (1UL shl 32) < sqrt_input - s) 1u else 0u) - (if(r2 + b1 > sqrt_input) 1u else 0u)
            }

            mul(c1l, cl, dl)
            val oldOff = lsp.offset
            if (variant >= 2) {
                lsp.offset = oldOff xor 0x10
                xor16(lsp, d)
                lsp.offset = oldOff xor 0x20
                xor16(d, lsp)
            }
            lsp.offset = 0
            VARIANT2_PORTABLE_SHUFFLE_ADD(lsp, oldOff, al, bl, variant)
            lsp.offset = oldOff

            sum_half_blocks(al, dl)
            swap_blocks(a, c)
            xor16(a, c)

            //technically only for variant 1
            //but we set tweak to 0 if not var1
            //xor by 0 is the original
            cl[1] = cl[1] xor tweak1_2

            copyBlock(lsp, c)

            if (variant >= 2) {
                copyBlock(bOff16, b)
            }

            copyBlock(b, c1)
        }

        text[0] = state.init[0, INIT_SIZE_BYTE]
        OAES.oaes_key_import_data(oaesCtx, state.b[32, AES_KEY_SIZE])
        lsp.offset = 0
        for (i in 0 until MEMORY / INIT_SIZE_BYTE) {
            tpj.offset = 0
            for (j in 0 until INIT_SIZE_BLK) {
                xor16(tpj, lsp)
                AESB.aesb_pseudo_round(tpj, tpj, oaesCtx.key.expData)
                tpj.offset += AES_BLOCK_SIZE
                lsp.offset += AES_BLOCK_SIZE
            }
        }

        state.init[0] = text.getRawArray()
        Keccak.keccakf(state.w, 24)

        val sp = Scratchpad.getScratchpad(32)

        println("Using hash function ${when(state.b[0].toInt() and 3){
            0 -> "hash_extra_blake"
            1 -> "hash_extra_groestl"
            2 -> "hash_extra_jh"
            else -> "hash_extra_skein"
        }}")
        extra_hashes[state.b[0].toInt() and 3](state.state, 200, sp.getPointer(0))
        sp.getRawArray().copyInto(hash)
    }

    fun VARIANT2_PORTABLE_SHUFFLE_ADD(base_ptr: UBytePointer, offset: Int, al: ULongPointer, bl: ULongPointer, variant: Int) {
        if (variant >= 2){
            val chunk1 = ((base_ptr) + ((offset) xor 0x10)).toULongPointer()
            val chunk2 = ((base_ptr) + ((offset) xor 0x20)).toULongPointer()
            val chunk3 = ((base_ptr) + ((offset) xor 0x30)).toULongPointer()

            val chunk1_old = ulongArrayOf(chunk1[0], chunk1[1])

            chunk1[0] = chunk3[0] + bl[2]
            chunk1[1] = chunk3[1] + bl[3]

            chunk3[0] = chunk2[0] + al[0]
            chunk3[1] = chunk2[1] + al[1]

            chunk2[0] = chunk1_old[0] + bl[0]
            chunk2[1] = chunk1_old[1] + bl[1]
        }
    }
}

@ExperimentalUnsignedTypes
fun main(args: Array<String>){
    val res = UByteArray(32)

    val var0 = "2f8e3df40bd11f9ac90c743ca8e32bb391da4fb98612aa3b6cdc639ee00b31f5 6465206f6d6e69627573206475626974616e64756d\n" +
            "722fa8ccd594d40e4a41f3822734304c8d5eff7e1b528408e2229da38ba553c4 6162756e64616e732063617574656c61206e6f6e206e6f636574\n" +
            "bbec2cacf69866a8e740380fe7b818fc78f8571221742d729d9d02d7f8989b87 63617665617420656d70746f72\n" +
            "b1257de4efc5ce28c6b40ceb1c6c8f812a64634eb3e81c5220bee9b2b76a6f05 6578206e6968696c6f206e6968696c20666974"

    val var1 = "b5a7f63abb94d07d1a6445c36c07c7e8327fe61b1647e391b4c7edae5de57a3d 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000\n" +
            "80563c40ed46575a9e44820d93ee095e2851aa22483fd67837118c6cd951ba61 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\n" +
            "5bb40c5880cef2f739bdb6aaaf16161eaae55530e7b10d7ea996b751a299e949 8519e039172b0d70e5ca7b3383d6b3167315a422747b73f019cf9528f0fde341fd0f2a63030ba6450525cf6de31837669af6f1df8131faf50aaab8d3a7405589\n" +
            "613e638505ba1fd05f428d5c9f8e08f8165614342dac419adc6a47dce257eb3e 37a636d7dafdf259b7287eddca2f58099e98619d2f99bdb8969d7b14498102cc065201c8be90bd777323f449848b215d2977c92c4c1c2da36ab46b2e389689ed97c18fec08cd3b03235c5e4c62a37ad88c7b67932495a71090e85dd4020a9300\n" +
            "ed082e49dbd5bbe34a3726a0d1dad981146062b39d36d62c71eb1ed8ab49459b 38274c97c45a172cfc97679870422e3a1ab0784960c60514d816271415c306ee3a3ed1a77e31f6a885c3cb"

    val var2 = "353fdc068fd47b03c04b9431e005e00b68c2168a3cc7335c8b9b308156591a4f 5468697320697320612074657374205468697320697320612074657374205468697320697320612074657374\n" +
            "72f134fc50880c330fe65a2cb7896d59b2e708a0221c6a9da3f69b3a702d8682 4c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e73656374657475722061646970697363696e67\n" +
            "410919660ec540fc49d8695ff01f974226a2a28dbbac82949c12f541b9a62d2f 656c69742c2073656420646f20656975736d6f642074656d706f7220696e6369646964756e74207574206c61626f7265\n" +
            "4472fecfeb371e8b7942ce0378c0ba5e6d0c6361b669c587807365c787ae652d 657420646f6c6f7265206d61676e6120616c697175612e20557420656e696d206164206d696e696d2076656e69616d2c\n" +
            "577568395203f1f1225f2982b637f7d5e61b47a0f546ba16d46020b471b74076 71756973206e6f737472756420657865726369746174696f6e20756c6c616d636f206c61626f726973206e697369\n" +
            "f6fd7efe95a5c6c4bb46d9b429e3faf65b1ce439e116742d42b928e61de52385 757420616c697175697020657820656120636f6d6d6f646f20636f6e7365717561742e20447569732061757465\n" +
            "422f8cfe8060cf6c3d9fd66f68e3c9977adb683aea2788029308bbe9bc50d728 697275726520646f6c6f7220696e20726570726568656e646572697420696e20766f6c7570746174652076656c6974\n" +
            "512e62c8c8c833cfbd9d361442cb00d63c0a3fd8964cfd2fedc17c7c25ec2d4b 657373652063696c6c756d20646f6c6f726520657520667567696174206e756c6c612070617269617475722e\n" +
            "12a794c1aa13d561c9c6111cee631ca9d0a321718d67d3416add9de1693ba41e 4578636570746575722073696e74206f6363616563617420637570696461746174206e6f6e2070726f6964656e742c\n" +
            "2659ff95fc74b6215c1dc741e85b7a9710101b30620212f80eb59c3c55993f9d 73756e7420696e2063756c706120717569206f666669636961206465736572756e74206d6f6c6c697420616e696d20696420657374206c61626f72756d2e"


    val ts = Array(3) {
        when (it) {
            0 -> Pair(var0, 0)
            1 -> Pair(var1, 1)
            else -> Pair(var2, 2)
        }
    }
    println("---WARMUP---")
    for(i in 0 until 5) {
        for(t in ts){
            for (l in t.first.split("\n")) {
                cnSlowHash(BinHexUtils.hexToByteArray(l.split(" ")[1]).toUByteArray(), res, t.second)
                print(l.split(" ")[0])
                print(": ")
                print(BinHexUtils.binaryToHex(res.toByteArray()))
                print(": ")
                println(BinHexUtils.binaryToHex(res.toByteArray()).equals(l.split(" ")[0], true))
            }
        }
    }
    var a = 0
    val al = IntArray(3)
    val th = LongArray(3)
    var pass = 0
    var fail = 0
    var nFail = 0
    println("---START---")
    val total = System.nanoTime()
    for(i in 0 until 10) {
        for(t in ts){
            for (l in t.first.split("\n")) {
                val it = System.nanoTime()
                cnSlowHash(BinHexUtils.hexToByteArray(l.split(" ")[1]).toUByteArray(), res, t.second)
                ++a
                ++al[t.second]
                th[t.second] += System.nanoTime() - it
                print(l.split(" ")[0])
                print(": ")
                print(BinHexUtils.binaryToHex(res.toByteArray()))
                print(": ")
                val good = BinHexUtils.binaryToHex(res.toByteArray()).equals(l.split(" ")[0], true)
                println(good)
                pass += if(good) 1 else 0
                if(!good){
                    fail++
                    if(res.toByteArray()[0].toUInt() == 0u){
                        nFail++
                    }
                }
            }
        }
    }
    println("Took ${(System.nanoTime() - total) / 1e9} seconds")
    println("Completed $a hashes")
    println("At ${a / ((System.nanoTime() - total) / 1e9)} h/s")
    for(i in 0 until 3){
        println("Variant $i")
        println("Took ${th[i] / 1e9} seconds")
        println("Completed ${al[i]} hashes")
        println("At ${al[i] / (th[i] / 1e9)} h/s")
    }
    println("$pass pass, $fail fail ($nFail null)")
}

@ExperimentalUnsignedTypes
class HashState{
    val state = Scratchpad.getScratchpad(200)
    val k = state.getPointer(0, 64)
    val init = state.getPointer(64, INIT_SIZE_BYTE)
    val b = state.getPointer(0)
    val w = state.getPointer(0).toULongPointer()
}