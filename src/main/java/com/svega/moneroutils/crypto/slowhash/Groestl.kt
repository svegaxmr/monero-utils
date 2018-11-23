package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
@Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")
object Groestl {
    private val T = uintArrayOf(0xa5f432c6u, 0xc6a597f4u, 0x84976ff8u, 0xf884eb97u, 0x99b05eeeu, 0xee99c7b0u, 0x8d8c7af6u, 0xf68df78cu, 0xd17e8ffu, 0xff0de517u, 0xbddc0ad6u, 0xd6bdb7dcu, 0xb1c816deu, 0xdeb1a7c8u, 0x54fc6d91u, 0x915439fcu,
            0x50f09060u, 0x6050c0f0u, 0x3050702u, 0x2030405u, 0xa9e02eceu, 0xcea987e0u, 0x7d87d156u, 0x567dac87u, 0x192bcce7u, 0xe719d52bu, 0x62a613b5u, 0xb56271a6u, 0xe6317c4du, 0x4de69a31u, 0x9ab559ecu, 0xec9ac3b5u,
            0x45cf408fu, 0x8f4505cfu, 0x9dbca31fu, 0x1f9d3ebcu, 0x40c04989u, 0x894009c0u, 0x879268fau, 0xfa87ef92u, 0x153fd0efu, 0xef15c53fu, 0xeb2694b2u, 0xb2eb7f26u, 0xc940ce8eu, 0x8ec90740u, 0xb1de6fbu, 0xfb0bed1du,
            0xec2f6e41u, 0x41ec822fu, 0x67a91ab3u, 0xb3677da9u, 0xfd1c435fu, 0x5ffdbe1cu, 0xea256045u, 0x45ea8a25u, 0xbfdaf923u, 0x23bf46dau, 0xf7025153u, 0x53f7a602u, 0x96a145e4u, 0xe496d3a1u, 0x5bed769bu, 0x9b5b2dedu,
            0xc25d2875u, 0x75c2ea5du, 0x1c24c5e1u, 0xe11cd924u, 0xaee9d43du, 0x3dae7ae9u, 0x6abef24cu, 0x4c6a98beu, 0x5aee826cu, 0x6c5ad8eeu, 0x41c3bd7eu, 0x7e41fcc3u, 0x206f3f5u, 0xf502f106u, 0x4fd15283u, 0x834f1dd1u,
            0x5ce48c68u, 0x685cd0e4u, 0xf4075651u, 0x51f4a207u, 0x345c8dd1u, 0xd134b95cu, 0x818e1f9u, 0xf908e918u, 0x93ae4ce2u, 0xe293dfaeu, 0x73953eabu, 0xab734d95u, 0x53f59762u, 0x6253c4f5u, 0x3f416b2au, 0x2a3f5441u,
            0xc141c08u, 0x80c1014u, 0x52f66395u, 0x955231f6u, 0x65afe946u, 0x46658cafu, 0x5ee27f9du, 0x9d5e21e2u, 0x28784830u, 0x30286078u, 0xa1f8cf37u, 0x37a16ef8u, 0xf111b0au, 0xa0f1411u, 0xb5c4eb2fu, 0x2fb55ec4u,
            0x91b150eu, 0xe091c1bu, 0x365a7e24u, 0x2436485au, 0x9bb6ad1bu, 0x1b9b36b6u, 0x3d4798dfu, 0xdf3da547u, 0x266aa7cdu, 0xcd26816au, 0x69bbf54eu, 0x4e699cbbu, 0xcd4c337fu, 0x7fcdfe4cu, 0x9fba50eau, 0xea9fcfbau,
            0x1b2d3f12u, 0x121b242du, 0x9eb9a41du, 0x1d9e3ab9u, 0x749cc458u, 0x5874b09cu, 0x2e724634u, 0x342e6872u, 0x2d774136u, 0x362d6c77u, 0xb2cd11dcu, 0xdcb2a3cdu, 0xee299db4u, 0xb4ee7329u, 0xfb164d5bu, 0x5bfbb616u,
            0xf601a5a4u, 0xa4f65301u, 0x4dd7a176u, 0x764decd7u, 0x61a314b7u, 0xb76175a3u, 0xce49347du, 0x7dcefa49u, 0x7b8ddf52u, 0x527ba48du, 0x3e429fddu, 0xdd3ea142u, 0x7193cd5eu, 0x5e71bc93u, 0x97a2b113u, 0x139726a2u,
            0xf504a2a6u, 0xa6f55704u, 0x68b801b9u, 0xb96869b8u, 0x0u, 0x0u, 0x2c74b5c1u, 0xc12c9974u, 0x60a0e040u, 0x406080a0u, 0x1f21c2e3u, 0xe31fdd21u, 0xc8433a79u, 0x79c8f243u, 0xed2c9ab6u, 0xb6ed772cu,
            0xbed90dd4u, 0xd4beb3d9u, 0x46ca478du, 0x8d4601cau, 0xd9701767u, 0x67d9ce70u, 0x4bddaf72u, 0x724be4ddu, 0xde79ed94u, 0x94de3379u, 0xd467ff98u, 0x98d42b67u, 0xe82393b0u, 0xb0e87b23u, 0x4ade5b85u, 0x854a11deu,
            0x6bbd06bbu, 0xbb6b6dbdu, 0x2a7ebbc5u, 0xc52a917eu, 0xe5347b4fu, 0x4fe59e34u, 0x163ad7edu, 0xed16c13au, 0xc554d286u, 0x86c51754u, 0xd762f89au, 0x9ad72f62u, 0x55ff9966u, 0x6655ccffu, 0x94a7b611u, 0x119422a7u,
            0xcf4ac08au, 0x8acf0f4au, 0x1030d9e9u, 0xe910c930u, 0x60a0e04u, 0x406080au, 0x819866feu, 0xfe81e798u, 0xf00baba0u, 0xa0f05b0bu, 0x44ccb478u, 0x7844f0ccu, 0xbad5f025u, 0x25ba4ad5u, 0xe33e754bu, 0x4be3963eu,
            0xf30eaca2u, 0xa2f35f0eu, 0xfe19445du, 0x5dfeba19u, 0xc05bdb80u, 0x80c01b5bu, 0x8a858005u, 0x58a0a85u, 0xadecd33fu, 0x3fad7eecu, 0xbcdffe21u, 0x21bc42dfu, 0x48d8a870u, 0x7048e0d8u, 0x40cfdf1u, 0xf104f90cu,
            0xdf7a1963u, 0x63dfc67au, 0xc1582f77u, 0x77c1ee58u, 0x759f30afu, 0xaf75459fu, 0x63a5e742u, 0x426384a5u, 0x30507020u, 0x20304050u, 0x1a2ecbe5u, 0xe51ad12eu, 0xe12effdu, 0xfd0ee112u, 0x6db708bfu, 0xbf6d65b7u,
            0x4cd45581u, 0x814c19d4u, 0x143c2418u, 0x1814303cu, 0x355f7926u, 0x26354c5fu, 0x2f71b2c3u, 0xc32f9d71u, 0xe13886beu, 0xbee16738u, 0xa2fdc835u, 0x35a26afdu, 0xcc4fc788u, 0x88cc0b4fu, 0x394b652eu, 0x2e395c4bu,
            0x57f96a93u, 0x93573df9u, 0xf20d5855u, 0x55f2aa0du, 0x829d61fcu, 0xfc82e39du, 0x47c9b37au, 0x7a47f4c9u, 0xacef27c8u, 0xc8ac8befu, 0xe73288bau, 0xbae76f32u, 0x2b7d4f32u, 0x322b647du, 0x95a442e6u, 0xe695d7a4u,
            0xa0fb3bc0u, 0xc0a09bfbu, 0x98b3aa19u, 0x199832b3u, 0xd168f69eu, 0x9ed12768u, 0x7f8122a3u, 0xa37f5d81u, 0x66aaee44u, 0x446688aau, 0x7e82d654u, 0x547ea882u, 0xabe6dd3bu, 0x3bab76e6u, 0x839e950bu, 0xb83169eu,
            0xca45c98cu, 0x8cca0345u, 0x297bbcc7u, 0xc729957bu, 0xd36e056bu, 0x6bd3d66eu, 0x3c446c28u, 0x283c5044u, 0x798b2ca7u, 0xa779558bu, 0xe23d81bcu, 0xbce2633du, 0x1d273116u, 0x161d2c27u, 0x769a37adu, 0xad76419au,
            0x3b4d96dbu, 0xdb3bad4du, 0x56fa9e64u, 0x6456c8fau, 0x4ed2a674u, 0x744ee8d2u, 0x1e223614u, 0x141e2822u, 0xdb76e492u, 0x92db3f76u, 0xa1e120cu, 0xc0a181eu, 0x6cb4fc48u, 0x486c90b4u, 0xe4378fb8u, 0xb8e46b37u,
            0x5de7789fu, 0x9f5d25e7u, 0x6eb20fbdu, 0xbd6e61b2u, 0xef2a6943u, 0x43ef862au, 0xa6f135c4u, 0xc4a693f1u, 0xa8e3da39u, 0x39a872e3u, 0xa4f7c631u, 0x31a462f7u, 0x37598ad3u, 0xd337bd59u, 0x8b8674f2u, 0xf28bff86u,
            0x325683d5u, 0xd532b156u, 0x43c54e8bu, 0x8b430dc5u, 0x59eb856eu, 0x6e59dcebu, 0xb7c218dau, 0xdab7afc2u, 0x8c8f8e01u, 0x18c028fu, 0x64ac1db1u, 0xb16479acu, 0xd26df19cu, 0x9cd2236du, 0xe03b7249u, 0x49e0923bu,
            0xb4c71fd8u, 0xd8b4abc7u, 0xfa15b9acu, 0xacfa4315u, 0x709faf3u, 0xf307fd09u, 0x256fa0cfu, 0xcf25856fu, 0xafea20cau, 0xcaaf8feau, 0x8e897df4u, 0xf48ef389u, 0xe9206747u, 0x47e98e20u, 0x18283810u, 0x10182028u,
            0xd5640b6fu, 0x6fd5de64u, 0x888373f0u, 0xf088fb83u, 0x6fb1fb4au, 0x4a6f94b1u, 0x7296ca5cu, 0x5c72b896u, 0x246c5438u, 0x3824706cu, 0xf1085f57u, 0x57f1ae08u, 0xc7522173u, 0x73c7e652u, 0x51f36497u, 0x975135f3u,
            0x2365aecbu, 0xcb238d65u, 0x7c8425a1u, 0xa17c5984u, 0x9cbf57e8u, 0xe89ccbbfu, 0x21635d3eu, 0x3e217c63u, 0xdd7cea96u, 0x96dd377cu, 0xdc7f1e61u, 0x61dcc27fu, 0x86919c0du, 0xd861a91u, 0x85949b0fu, 0xf851e94u,
            0x90ab4be0u, 0xe090dbabu, 0x42c6ba7cu, 0x7c42f8c6u, 0xc4572671u, 0x71c4e257u, 0xaae529ccu, 0xccaa83e5u, 0xd873e390u, 0x90d83b73u, 0x50f0906u, 0x6050c0fu, 0x103f4f7u, 0xf701f503u, 0x12362a1cu, 0x1c123836u,
            0xa3fe3cc2u, 0xc2a39ffeu, 0x5fe18b6au, 0x6a5fd4e1u, 0xf910beaeu, 0xaef94710u, 0xd06b0269u, 0x69d0d26bu, 0x91a8bf17u, 0x17912ea8u, 0x58e87199u, 0x995829e8u, 0x2769533au, 0x3a277469u, 0xb9d0f727u, 0x27b94ed0u,
            0x384891d9u, 0xd938a948u, 0x1335deebu, 0xeb13cd35u, 0xb3cee52bu, 0x2bb356ceu, 0x33557722u, 0x22334455u, 0xbbd604d2u, 0xd2bbbfd6u, 0x709039a9u, 0xa9704990u, 0x89808707u, 0x7890e80u, 0xa7f2c133u, 0x33a766f2u,
            0xb6c1ec2du, 0x2db65ac1u, 0x22665a3cu, 0x3c227866u, 0x92adb815u, 0x15922aadu, 0x2060a9c9u, 0xc9208960u, 0x49db5c87u, 0x874915dbu, 0xff1ab0aau, 0xaaff4f1au, 0x7888d850u, 0x5078a088u, 0x7a8e2ba5u, 0xa57a518eu,
            0x8f8a8903u, 0x38f068au, 0xf8134a59u, 0x59f8b213u, 0x809b9209u, 0x980129bu, 0x1739231au, 0x1a173439u, 0xda751065u, 0x65daca75u, 0x315384d7u, 0xd731b553u, 0xc651d584u, 0x84c61351u, 0xb8d303d0u, 0xd0b8bbd3u,
            0xc35edc82u, 0x82c31f5eu, 0xb0cbe229u, 0x29b052cbu, 0x7799c35au, 0x5a77b499u, 0x11332d1eu, 0x1e113c33u, 0xcb463d7bu, 0x7bcbf646u, 0xfc1fb7a8u, 0xa8fc4b1fu, 0xd6610c6du, 0x6dd6da61u, 0x3a4e622cu, 0x2c3a584eu)

    private const val ROWS = 8
    private val LENGTHFIELDLEN = ROWS
    private const val COLS512 = 8

    val SIZE512 = (ROWS*COLS512)

    private const val HASH_BIT_LEN = 256

    private fun ROTL32(v: UInt, n: Int) = (((v shl n) or (v shr (32-n))) and 0xffffffffu)


    private fun u32BIG(a: UInt)	=
            ((ROTL32(a,8) and 0x00FF00FFu) or
            (ROTL32(a,24) and 0xFF00FF00u))


/* NIST API begin */


    val indices_cyclic= ubyteArrayOf(0u,1u,2u,3u,4u,5u,6u,7u,0u,1u,2u,3u,4u,5u,6u)

    private fun COLUMN(x: UBytePointer, y: UIntPointer, i: Int, c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int, c6: Int, c7: Int){
        var tv1 = 0u
        var tv2 = 0u
        var tu = 0u
        var tl = 0u
        var t = 0u
        tu = T[(2u * x[4*c0+0]).toInt()]
        tl = T[(2u * x[4*c0+0]+1u).toInt()]
        tv1 = T[(2u * x[4*c1+1]).toInt()]
        tv2 = T[(2u * x[4*c1+1]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,1,t)
        var amount_bytes = 1
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tu = tu xor tv1
        tl = tl xor tv2
        tv1 = T[(2u * x[4*c2+2]).toInt()]
        tv2 = T[(2u * x[4*c2+2]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,2,t)
        amount_bytes = 2
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tu = tu xor tv1
        tl = tl xor tv2
        tv1 = T[(2u * x[4*c3+3]).toInt()]
        tv2 = T[(2u * x[4*c3+3]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,3,t)=
        amount_bytes = 3
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tu = tu xor tv1
        tl = tl xor tv2
        tl = tl xor T[(2u * x[4*c4+0]).toInt()]
        tu = tu xor T[(2u * x[4*c4+0]+1u).toInt()]
        tv1 = T[(2u * x[4*c5+1]).toInt()]
        tv2 = T[(2u * x[4*c5+1]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,1,t)
        amount_bytes = 1
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tl = tl xor tv1
        tu = tu xor tv2
        tv1 = T[(2u * x[4*c6+2]).toInt()]
        tv2 = T[(2u * x[4*c6+2]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,2,t)
        amount_bytes = 2
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tl = tl xor tv1
        tu = tu xor tv2
        tv1 = T[(2u * x[4*c7+3]).toInt()]
        tv2 = T[(2u * x[4*c7+3]+1u).toInt()]
        //ROTATE_COLUMN_DOWN(tv1,tv2,3,t)
        amount_bytes = 3
        t = (tv1 shl (8 * amount_bytes)) or (tv2 shr (8 * (4 - amount_bytes)))
        tv2 = (tv2 shl (8 * amount_bytes)) or (tv1 shr (8 * (4 - amount_bytes)))
        tv1 = t
        tl = tl xor tv1
        tu = tu xor tv2
        y[i] = tu
        y[i+1] = tl
    }

    private fun  Init(ctx: GSHashState) {

        /* set initial value */
        ctx.chaining[2*COLS512-1] = u32BIG(HASH_BIT_LEN.toUInt())

        /* set other variables */
        ctx.buf_ptr = 0
        ctx.block_counter1 = 0u
        ctx.block_counter2 = 0u
        ctx.bits_in_last_byte = 0
    }

    private fun Update(ctx: GSHashState, input: UBytePointer, databitlen: ULong) {
        var index = 0
        val msglen = (databitlen/8u).toInt()
        val rem = (databitlen%8u).toInt()

        /* if the buffer contains data that has not yet been digested, first
           add data to buffer until full */
        if (ctx.buf_ptr != 0) {
            while (ctx.buf_ptr < SIZE512 && index < msglen) {
                ctx.buffer[ctx.buf_ptr++] = input[index++]
            }
            if (ctx.buf_ptr < SIZE512) {
                /* buffer still not full, return */
                if (rem != 0) {
                    ctx.bits_in_last_byte = rem
                    ctx.buffer[ctx.buf_ptr++] = input[index]
                }
                return
            }

            /* digest buffer */
            ctx.buf_ptr = 0
            Transform(ctx, ctx.buffer, SIZE512)
        }

        /* digest bulk of message */
        Transform(ctx, (input+index).toUBytePointer(), msglen-index)
        index += ((msglen-index)/SIZE512)*SIZE512

        /* store remaining data in buffer */
        while (index < msglen) {
            ctx.buffer[ctx.buf_ptr++] = input[index++]
        }

        /* if non-integral number of bytes have been supplied, store
           remaining bits in last byte, together with information about
           number of bits */
        if (rem != 0) {
            ctx.bits_in_last_byte = rem
            ctx.buffer[ctx.buf_ptr++] = input[index]
        }
    }

    private fun RND512P(x: UBytePointer, y: UIntPointer, r: UInt) {
        val x32 = x.toUIntPointer()
        x32[0] = x32[0] xor (0x00000000u xor r)
        x32[2] = x32[2] xor (0x00000010u xor r)
        x32[4] = x32[4] xor (0x00000020u xor r)
        x32[6] = x32[6] xor (0x00000030u xor r)
        x32[8] = x32[8] xor (0x00000040u xor r)
        x32[10] = x32[10] xor (0x00000050u xor r)
        x32[12] = x32[12] xor (0x00000060u xor r)
        x32[14] = x32[14] xor (0x00000070u xor r)
        COLUMN(x,y, 0,  0,  2,  4,  6,  9, 11, 13, 15)
        COLUMN(x,y, 2,  2,  4,  6,  8, 11, 13, 15,  1)
        COLUMN(x,y, 4,  4,  6,  8, 10, 13, 15,  1,  3)
        COLUMN(x,y, 6,  6,  8, 10, 12, 15,  1,  3,  5)
        COLUMN(x,y, 8,  8, 10, 12, 14,  1,  3,  5,  7)
        COLUMN(x,y,10, 10, 12, 14,  0,  3,  5,  7,  9)
        COLUMN(x,y,12, 12, 14,  0,  2,  5,  7,  9, 11)
        COLUMN(x,y,14, 14,  0,  2,  4,  7,  9, 11, 13)
    }

    private fun RND512Q(x: UBytePointer, y: UIntPointer, r: UInt) {
        val x32 = x.toUIntPointer()
        x32[0] = x32[0].inv()
        x32[1] = x32[1] xor (0xffffffffu xor r)
        x32[2] = x32[2].inv()
        x32[3] = x32[3] xor (0xefffffffu xor r)
        x32[4] = x32[4].inv()
        x32[5] = x32[5] xor (0xdfffffffu xor r)
        x32[6] = x32[6].inv()
        x32[7] = x32[7] xor (0xcfffffffu xor r)
        x32[8] = x32[8].inv()
        x32[9] = x32[9] xor (0xbfffffffu xor r)
        x32[10] = x32[10].inv()
        x32[11] = x32[11] xor (0xafffffffu xor r)
        x32[12] = x32[12].inv()
        x32[13] = x32[13] xor (0x9fffffffu xor r)
        x32[14] = x32[14].inv()
        x32[15] = x32[15] xor (0x8fffffffu xor r)
        COLUMN(x,y, 0,  2,  6, 10, 14,  1,  5,  9, 13)
        COLUMN(x,y, 2,  4,  8, 12,  0,  3,  7, 11, 15)
        COLUMN(x,y, 4,  6, 10, 14,  2,  5,  9, 13,  1)
        COLUMN(x,y, 6,  8, 12,  0,  4,  7, 11, 15,  3)
        COLUMN(x,y, 8, 10, 14,  2,  6,  9, 13,  1,  5)
        COLUMN(x,y,10, 12,  0,  4,  8, 11, 15,  3,  7)
        COLUMN(x,y,12, 14,  2,  6, 10, 13,  1,  5,  9)
        COLUMN(x,y,14,  0,  4,  8, 12, 15,  3,  7, 11)
    }

    private fun F512(h: UIntPointer, m: UIntPointer) {
        var i = 0u
        val Ptmp = Scratchpad.getScratchpad(4 * 2 * COLS512).getPointer(0).toUIntPointer()
        val Qtmp = Scratchpad.getScratchpad(4 * 2 * COLS512).getPointer(0).toUIntPointer()
        val y = Scratchpad.getScratchpad(4 * 2 * COLS512).getPointer(0).toUIntPointer()
        val z = Scratchpad.getScratchpad(4 * 2 * COLS512).getPointer(0).toUIntPointer()

        for (i in 0 until 2*COLS512) {
            z[i] = m[i]
            Ptmp[i] = h[i] xor m[i]
        }

        /* compute Q(m) */
        RND512Q(z.toUBytePointer(), y, 0x00000000u)
        RND512Q(y.toUBytePointer(), z, 0x01000000u)
        RND512Q(z.toUBytePointer(), y, 0x02000000u)
        RND512Q(y.toUBytePointer(), z, 0x03000000u)
        RND512Q(z.toUBytePointer(), y, 0x04000000u)
        RND512Q(y.toUBytePointer(), z, 0x05000000u)
        RND512Q(z.toUBytePointer(), y, 0x06000000u)
        RND512Q(y.toUBytePointer(), z, 0x07000000u)
        RND512Q(z.toUBytePointer(), y, 0x08000000u)
        RND512Q(y.toUBytePointer(), Qtmp, 0x09000000u)

        /* compute P(h+m) */
        RND512P(Ptmp.toUBytePointer(), y, 0x00000000u)
        RND512P(y.toUBytePointer(), z, 0x00000001u)
        RND512P(z.toUBytePointer(), y, 0x00000002u)
        RND512P(y.toUBytePointer(), z, 0x00000003u)
        RND512P(z.toUBytePointer(), y, 0x00000004u)
        RND512P(y.toUBytePointer(), z, 0x00000005u)
        RND512P(z.toUBytePointer(), y, 0x00000006u)
        RND512P(y.toUBytePointer(), z, 0x00000007u)
        RND512P(z.toUBytePointer(), y, 0x00000008u)
        RND512P(y.toUBytePointer(), Ptmp, 0x00000009u)

        /* compute P(h+m) + Q(m) + h */
        for (i in 0 until 2*COLS512) {
            h[i] = h[i] xor Ptmp[i] xor Qtmp[i]
        }
    }

    private fun Transform(ctx: GSHashState, input: UBytePointer, msglen: Int) {
        var msglen = msglen
        var input = input
        /* digest message, one block at a time */
        while (msglen >= SIZE512){
            F512(ctx.chaining, input.toUIntPointer())

            /* increment block counter */
            ctx.block_counter1++
            if (ctx.block_counter1 == 0u)
                ctx.block_counter2++
            msglen -= SIZE512
            input = (input + SIZE512).toUBytePointer()
        }
    }

    private fun OutputTransformation(ctx: GSHashState) {
        val temp = Scratchpad.getScratchpad(4*2*COLS512).getPointer(0).toUIntPointer()
        val y = Scratchpad.getScratchpad(4*2*COLS512).getPointer(0).toUIntPointer()
        val z = Scratchpad.getScratchpad(4*2*COLS512).getPointer(0).toUIntPointer()

        for (j in 0 until 2*COLS512) {
            temp[j] = ctx.chaining[j]
        }
        RND512P(temp.toUBytePointer(), y, 0x00000000u)
        RND512P(y.toUBytePointer(), z, 0x00000001u)
        RND512P(z.toUBytePointer(), y, 0x00000002u)
        RND512P(y.toUBytePointer(), z, 0x00000003u)
        RND512P(z.toUBytePointer(), y, 0x00000004u)
        RND512P(y.toUBytePointer(), z, 0x00000005u)
        RND512P(z.toUBytePointer(), y, 0x00000006u)
        RND512P(y.toUBytePointer(), z, 0x00000007u)
        RND512P(z.toUBytePointer(), y, 0x00000008u)
        RND512P(y.toUBytePointer(), temp, 0x00000009u)
        for (j in 0 until 2*COLS512) {
            ctx.chaining[j] = ctx.chaining[j] xor temp[j]
        }
    }

    private fun Final(ctx: GSHashState,
                      output: UBytePointer) {
        var i: Int
        var j = 0
        val hashbytelen = HASH_BIT_LEN / 8
        val s = ctx.chaining.toUBytePointer()

        /* pad with '1'-bit and first few '0'-bits */
        if (ctx.bits_in_last_byte != 0) {
            ctx.buffer[ctx.buf_ptr - 1] = ctx.buffer[ctx.buf_ptr - 1] and ((1 shl ctx.bits_in_last_byte) - 1 shl 8 - ctx.bits_in_last_byte).toUByte()
            ctx.buffer[ctx.buf_ptr - 1] = ctx.buffer[ctx.buf_ptr - 1] xor (0x1 shl 7 - ctx.bits_in_last_byte).toUByte()
            ctx.bits_in_last_byte = 0
        } else
            ctx.buffer[ctx.buf_ptr++] = 0x80u

        /* pad with '0'-bits */
        if (ctx.buf_ptr > SIZE512 - LENGTHFIELDLEN) {
            /* padding requires two blocks */
            while (ctx.buf_ptr < SIZE512) {
                ctx.buffer[ctx.buf_ptr++] = 0u
            }
            /* digest first padding block */
            Transform(ctx, ctx.buffer, SIZE512)
            ctx.buf_ptr = 0
        }
        while (ctx.buf_ptr < SIZE512 - LENGTHFIELDLEN) {
            ctx.buffer[ctx.buf_ptr++] = 0u
        }

        /* length padding */
        ctx.block_counter1++
        if (ctx.block_counter1 == 0u) ctx.block_counter2++
        ctx.buf_ptr = SIZE512

        while (ctx.buf_ptr > SIZE512 - 4) {
            ctx.buffer[--ctx.buf_ptr] = (ctx.block_counter1).toUByte()
            ctx.block_counter1 = ctx.block_counter1 shr 8
        }
        while (ctx.buf_ptr > SIZE512 - LENGTHFIELDLEN) {
            ctx.buffer[--ctx.buf_ptr] = (ctx.block_counter2).toUByte()
            ctx.block_counter2 = ctx.block_counter2 shr 8
        }
        /* digest final padding block */
        Transform(ctx, ctx.buffer, SIZE512)
        /* perform output transformation */
        OutputTransformation(ctx)

        /* store hash result in output */
        i = SIZE512 - hashbytelen
        while (i < SIZE512) {
            output[j] = s[i]
            i++
            j++
        }

        /* zeroise relevant variables and deallocate memory */
        i = 0
        while (i < COLS512) {
            ctx.chaining[i] = 0u
            i++
        }
        i = 0
        while (i < SIZE512) {
            ctx.buffer[i] = 0u
            i++
        }
    }

    fun groestl256Hash(out: UBytePointer, din: UBytePointer, inlen: ULong) {
        val S = GSHashState()
        Init(S)
        Update(S, din, inlen * 8u)
        Final(S, out)
    }
}

@ExperimentalUnsignedTypes
class GSHashState{
    val chaining = Scratchpad.getScratchpad(Groestl.SIZE512).getPointer(0).toUIntPointer()            /* actual state */
    var block_counter1 = 0u
    var block_counter2 = 0u
    val buffer = Scratchpad.getScratchpad(Groestl.SIZE512).getPointer(0)
    var buf_ptr = 0              /* data buffer pointer */
    var bits_in_last_byte = 0    /* no. of message bits in last byte of
			       data buffer */
}