package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object Skein{

    data class SkeinHashState(
        var statebits: Int = 256,
        val ctx_512: Skein512Ctx = Skein512Ctx()
    )
    const val SKEIN_MODIFIER_WORDS = ( 2)          /* number of modifier (tweak) words */

    const val SKEIN_512_STATE_WORDS = ( 8)
    val SKEIN_512_BLOCK_BYTES = ( 8*SKEIN_512_STATE_WORDS).toUInt()

    private val SKEIN_RND_SPECIAL = (1000u)
    val SKEIN_RND_KEY_INITIAL = (SKEIN_RND_SPECIAL+0u)
    val SKEIN_RND_KEY_INJECT = (SKEIN_RND_SPECIAL+1u)
    val SKEIN_RND_FEED_FWD = (SKEIN_RND_SPECIAL+2u)

    data class SkeinCtxHdrT(
            var hashBitLen: ULong,
            var bCnt: ULong,
            val T: ULongArray = ULongArray(SKEIN_MODIFIER_WORDS)
    )

    data class Skein512Ctx(
            val h: SkeinCtxHdrT = SkeinCtxHdrT(0u, 0u),
            val X: ULongPointer = Scratchpad.getScratchpad(8 * SKEIN_512_STATE_WORDS).getPointer(0).toULongPointer(),
            val b: UBytePointer = Scratchpad.getScratchpad(SKEIN_512_BLOCK_BYTES.toInt()).getPointer(0)
    )

    const val SKEIN_TREE_HASH = (1)

    private const val SKEIN_T1_POS_TREE_LVL = 112 - 64   /* bits 112..118: level in hash tree   */
    private const val SKEIN_T1_POS_BIT_PAD = 119 - 64   /* bit  119 : partial final input byte */
    private const val SKEIN_T1_POS_BLK_TYPE = 120 - 64   /* bits 120..125: type field   */
    private const val SKEIN_T1_POS_FIRST = 126 - 64   /* bits 126 : first block flag */
    private const val SKEIN_T1_POS_FINAL = 127 - 64   /* bit  127 : final block flag */

    /* tweak word T[1]: flag bit definition(s) */
    private val SKEIN_T1_FLAG_FIRST = ((1uL ) shl SKEIN_T1_POS_FIRST)
    private val SKEIN_T1_FLAG_FINAL = ((1uL ) shl SKEIN_T1_POS_FINAL)
    private val SKEIN_T1_FLAG_BIT_PAD = ((1uL ) shl SKEIN_T1_POS_BIT_PAD)

    /* tweak word T[1]: tree level bit field mask */
    val SKEIN_T1_TREE_LVL_MASK = ((0x7FuL) shl SKEIN_T1_POS_TREE_LVL)

    /* tweak word T[1]: block type field */
    private const val SKEIN_BLK_TYPE_KEY = ( 0)/* key, for MAC and KDF */
    private const val SKEIN_BLK_TYPE_CFG = ( 4)/* configuration block */
    private const val SKEIN_BLK_TYPE_PERS = ( 8)/* personalization string */
    private const val SKEIN_BLK_TYPE_PK = (12)/* public key (for digital signature hashing) */
    private const val SKEIN_BLK_TYPE_KDF = (16)/* key identifier for KDF */
    private const val SKEIN_BLK_TYPE_NONCE = (20)/* nonce for PRNG */
    private const val SKEIN_BLK_TYPE_MSG = (48)/* message processing */
    private const val SKEIN_BLK_TYPE_OUT = (63)/* output stage */
    private const val SKEIN_BLK_TYPE_MASK = (63)/* bit field mask */

    //#define SKEIN_T1_BLK_TYPE(T)   (((u64b_t) (SKEIN_BLK_TYPE_##T)) shl SKEIN_T1_POS_BLK_TYPE)
    val SKEIN_T1_BLK_TYPE_KEY = SKEIN_BLK_TYPE_KEY.toULong()  /* key, for MAC and KDF */
    private val SKEIN_T1_BLK_TYPE_CFG = SKEIN_BLK_TYPE_CFG.toULong() shl SKEIN_T1_POS_BLK_TYPE  /* configuration block */
    val SKEIN_T1_BLK_TYPE_PERS = SKEIN_BLK_TYPE_PERS.toULong() /* personalization string */
    val SKEIN_T1_BLK_TYPE_PK = SKEIN_BLK_TYPE_PK.toULong()   /* public key (for digital signature hashing) */
    val SKEIN_T1_BLK_TYPE_KDF = SKEIN_BLK_TYPE_KDF.toULong()  /* key identifier for KDF */
    val SKEIN_T1_BLK_TYPE_NONCE = SKEIN_BLK_TYPE_NONCE.toULong()/* nonce for PRNG */
    private val SKEIN_T1_BLK_TYPE_MSG = SKEIN_BLK_TYPE_MSG.toULong() shl SKEIN_T1_POS_BLK_TYPE  /* message processing */
    private val SKEIN_T1_BLK_TYPE_OUT = SKEIN_BLK_TYPE_OUT.toULong() shl SKEIN_T1_POS_BLK_TYPE  /* output stage */
    val SKEIN_T1_BLK_TYPE_MASK = SKEIN_BLK_TYPE_MASK.toULong() /* field bit mask */

    private fun SKEIN_T1_TREE_LEVEL(n: ULong) = n shl SKEIN_T1_POS_TREE_LVL

    val SKEIN_T1_BLK_TYPE_CFG_FINAL = (SKEIN_T1_BLK_TYPE_CFG or SKEIN_T1_FLAG_FINAL)
    private val SKEIN_T1_BLK_TYPE_OUT_FINAL = (SKEIN_T1_BLK_TYPE_OUT or SKEIN_T1_FLAG_FINAL)

    private val SKEIN_VERSION: UInt = (1u)

    val SKEIN_ID_STRING_LE: UInt = (0x33414853u)/* "SHA3" (little-endian)*/

    const val SKEIN_CFG_STR_LEN = (4*8)

    /* bit field definitions in config block treeInfo word */
    private const val SKEIN_CFG_TREE_LEAF_SIZE_POS = ( 0)
    private const val SKEIN_CFG_TREE_NODE_SIZE_POS = ( 8)
    private const val SKEIN_CFG_TREE_MAX_LEVEL_POS = (16)

    val SKEIN_CFG_TREE_LEAF_SIZE_MSK = ((0xFFuL) shl SKEIN_CFG_TREE_LEAF_SIZE_POS)
    val SKEIN_CFG_TREE_NODE_SIZE_MSK = ((0xFFuL) shl SKEIN_CFG_TREE_NODE_SIZE_POS)
    val SKEIN_CFG_TREE_MAX_LEVEL_MSK = ((0xFFuL) shl SKEIN_CFG_TREE_MAX_LEVEL_POS)

    private fun SKEIN_MK_64(hi32: UInt, lo32: UInt) = ((lo32) + (hi32.toULong() shl 32))
    val SKEIN_SCHEMA_VER =  SKEIN_MK_64(SKEIN_VERSION,SKEIN_ID_STRING_LE)
    private val SKEIN_KS_PARITY =  SKEIN_MK_64(0x1BD11BDAu,0xA9FC1A22u)

    private fun SKEIN_CFG_TREE_INFO(leaf: ULong, node: ULong, maxLvl: ULong) =
            ( (leaf shl SKEIN_CFG_TREE_LEAF_SIZE_POS) or
                    (node shl SKEIN_CFG_TREE_NODE_SIZE_POS) or
                    (maxLvl shl SKEIN_CFG_TREE_MAX_LEVEL_POS) )

    val SKEIN_CFG_TREE_INFO_SEQUENTIAL = SKEIN_CFG_TREE_INFO(0u,0u,0u)

    private fun Skein_Get_Tweak(ctxPtr: Skein512Ctx, TWK_NUM: Int) = (ctxPtr.h.T[TWK_NUM])
    private fun Skein_Set_Tweak(ctxPtr: Skein512Ctx, TWK_NUM: Int, tVal: ULong){ctxPtr.h.T[TWK_NUM] = tVal}

    fun Skein_Get_T0(ctxPtr: Skein512Ctx) = Skein_Get_Tweak(ctxPtr,0)
    fun Skein_Get_T1(ctxPtr: Skein512Ctx) = Skein_Get_Tweak(ctxPtr,1)
    private fun Skein_Set_T0(ctxPtr: Skein512Ctx, T0: ULong) = Skein_Set_Tweak(ctxPtr,0,T0)
    private fun Skein_Set_T1(ctxPtr: Skein512Ctx, T1: ULong) = Skein_Set_Tweak(ctxPtr,1,T1)

    private fun Skein_Set_T0_T1(ctxPtr: Skein512Ctx, T0: ULong, T1: ULong){
        Skein_Set_T0(ctxPtr, T0)
        Skein_Set_T1(ctxPtr, T1)
    }

    private fun Skein_Start_New_Type(ctxPtr: Skein512Ctx, BLK_TYPE: ULong){
        Skein_Set_T0_T1(ctxPtr,0u, SKEIN_T1_FLAG_FIRST or BLK_TYPE)
        ctxPtr.h.bCnt=0u
    }

    fun Skein_Clear_First_Flag(hdr: SkeinCtxHdrT){hdr.T[1] = hdr.T[1] and SKEIN_T1_FLAG_FIRST.inv()}
    fun Skein_Set_Bit_Pad_Flag(hdr: SkeinCtxHdrT){hdr.T[1] = hdr.T[1] or SKEIN_T1_FLAG_BIT_PAD}

    fun Skein_Set_Tree_Level(hdr: SkeinCtxHdrT, height: ULong){hdr.T[1] = hdr.T[1] or SKEIN_T1_TREE_LEVEL(height)}

    /* Skein_512 round rotation constants */
    private const val R_512_0_0=46
    private const val R_512_0_1=36
    private const val R_512_0_2=19
    private const val R_512_0_3=37

    private const val R_512_1_0=33
    private const val R_512_1_1=27
    private const val R_512_1_2=14
    private const val R_512_1_3=42

    private const val R_512_2_0=17
    private const val R_512_2_1=49
    private const val R_512_2_2=36
    private const val R_512_2_3=39

    private const val R_512_3_0=44
    private const val R_512_3_1=9
    private const val R_512_3_2=54
    private const val R_512_3_3=56

    private const val R_512_4_0=39
    private const val R_512_4_1=30
    private const val R_512_4_2=34
    private const val R_512_4_3=24

    private const val R_512_5_0=13
    private const val R_512_5_1=50
    private const val R_512_5_2=10
    private const val R_512_5_3=17

    private const val R_512_6_0=25
    private const val R_512_6_1=29
    private const val R_512_6_2=39
    private const val R_512_6_3=43

    private const val R_512_7_0=8
    private const val R_512_7_1=35
    private const val R_512_7_2=56
    private const val R_512_7_3=22

    const val SKEIN_512_ROUNDS_TOTAL = 72

    private fun MK_64(hi32: UInt, lo32: UInt) = SKEIN_MK_64(hi32, lo32)

    val SKEIN_512_IV_256 = ulongArrayOf(
            MK_64(0xCCD044A1u,0x2FDB3E13u),
            MK_64(0xE8359030u,0x1A79A9EBu),
            MK_64(0x55AEA061u,0x4F816E6Fu),
            MK_64(0x2A2767A4u,0xAE9B94DBu),
            MK_64(0xEC06025Eu,0x74DD7683u),
            MK_64(0xE7A436CDu,0xC4746251u),
            MK_64(0xC36FBAF9u,0x393AD185u),
            MK_64(0x3EEDBA18u,0x33EDFC13u)
    )

    /*
    #define BLK_BITS        (WCNT*64)               /* some useful definitions for code here */
    #define KW_TWK_BASE     (0)
    #define KW_KEY_BASE     (3)
    #define ks              (kw + KW_KEY_BASE)
    #define ts              (kw + KW_TWK_BASE)
     */

    private fun Skein_512_Init(ctx: Skein512Ctx, hashBitLen: Int) {
        ctx.h.hashBitLen = hashBitLen.toULong()         /* output hash bit count */

        for(i in 0 until SKEIN_512_IV_256.size){
            ctx.X[i] = SKEIN_512_IV_256[i]
        }

        //memcpy(ctx.X, SKEIN_512_IV_256, sizeof { ctx -> X })

        /* The chaining vars ctx->X are now initialized for the given hashBitLen. */
        /* Set up to process the data message portion of the hash (default) */
        //Skein_Start_New_Type(ctx,MSG);              /* T0=0, T1= MSG type */
        Skein_Start_New_Type(ctx, SKEIN_T1_BLK_TYPE_MSG)
    }

    private fun Skein_512_Update(ctx: Skein512Ctx, msg_: UBytePointer, msgByteCnt_: ULong){
        var msg = msg_
        var msgByteCnt = msgByteCnt_
        var n: ULong

        /* process full blocks, if any */
        if (msgByteCnt + ctx.h.bCnt > SKEIN_512_BLOCK_BYTES){
            if (ctx.h.bCnt != 0uL){                             /* finish up any buffered message data */
                n = SKEIN_512_BLOCK_BYTES.toULong() - ctx.h.bCnt  /* # bytes free in buffer b[] */
                if (n != 0uL){
                    ctx.b[ctx.h.bCnt.toInt()] = msg[0, n.toInt()]
                    //memcpy(&ctx.b[ctx.h.bCnt],msg,n);
                    msgByteCnt  -= n
                    msg = (msg + n.toInt()).toUBytePointer()
                    ctx.h.bCnt += n
                }
                Skein_512_Process_Block(ctx,ctx.b,1u,SKEIN_512_BLOCK_BYTES.toULong())
                ctx.h.bCnt = 0uL
            }
            /* now process any remaining full blocks, directly from input message data */
            if (msgByteCnt > SKEIN_512_BLOCK_BYTES){
                n = ((msgByteCnt-1u) / SKEIN_512_BLOCK_BYTES).toULong()   /* number of full blocks to process */
                Skein_512_Process_Block(ctx,msg,n,SKEIN_512_BLOCK_BYTES.toULong())
                msgByteCnt -= (n * SKEIN_512_BLOCK_BYTES)
                msg = (msg + (n * SKEIN_512_BLOCK_BYTES).toInt()).toUBytePointer()
            }
        }

        /* copy any remaining source message data bytes into b[] */
        if (msgByteCnt!= 0uL){
            ctx.b[ctx.h.bCnt.toInt()] = msg[0, msgByteCnt.toInt()]
            //memcpy(&ctx.b[ctx.h.bCnt],msg,msgByteCnt);
            ctx.h.bCnt += msgByteCnt.toUInt()
        }
    }

    private fun Skein_512_Process_Block(ctx: Skein512Ctx, blkPtr_: UBytePointer, blkCnt_: ULong, byteCntAdd: ULong) { /* do it in C */
        var blkPtr = blkPtr_
        var blkCnt = blkCnt_
        val WCNT = SKEIN_512_STATE_WORDS

        val kw = Scratchpad.getScratchpad((WCNT+4) * 8).getPointer(0).toULongPointer()
        var X0: ULong
        var X1: ULong
        var X2: ULong
        var X3: ULong
        var X4: ULong
        var X5: ULong
        var X6: ULong
        var X7: ULong

        val w = Scratchpad.getScratchpad(WCNT * 8).getPointer(0).toULongPointer()                           /* local copy of input block */

        val ks = (kw + 3).toULongPointer()
        val ts = (kw + 0).toULongPointer()

        ts[0] = ctx.h.T[0]
        ts[1] = ctx.h.T[1]

        do{
            ts[0] += byteCntAdd                    /* update processed length */

            /* precompute the key schedule for this block */
            ks[0] = ctx.X[0]
            ks[1] = ctx.X[1]
            ks[2] = ctx.X[2]
            ks[3] = ctx.X[3]
            ks[4] = ctx.X[4]
            ks[5] = ctx.X[5]
            ks[6] = ctx.X[6]
            ks[7] = ctx.X[7]
            ks[8] = ks[0] xor ks[1] xor ks[2] xor ks[3] xor
                    ks[4] xor ks[5] xor ks[6] xor ks[7] xor SKEIN_KS_PARITY

            ts[2] = ts[0] xor ts[1]

            Skein_Get64_LSB_First(w, blkPtr, WCNT) /* get input block in little-endian format */

            X0 = w[0] + ks[0]                    /* do the first full key injection */
            X1 = w[1] + ks[1]
            X2 = w[2] + ks[2]
            X3 = w[3] + ks[3]
            X4 = w[4] + ks[4]
            X5 = w[5] + ks[5] + ts[0]
            X6 = w[6] + ks[6] + ts[1]
            X7 = w[7] + ks[7]

            blkPtr = (blkPtr + SKEIN_512_BLOCK_BYTES.toInt()).toUBytePointer()
            fun RotL_64(x: ULong, N: Int) = (x shl N) or (x shr (64-N))
            fun I512(R: Int){
                X0   += ks[((R)+1) % 9]   /* inject the key schedule value */
                X1   += ks[((R)+2) % 9]
                X2   += ks[((R)+3) % 9]
                X3   += ks[((R)+4) % 9]
                X4   += ks[((R)+5) % 9]
                X5   += ks[((R)+6) % 9] + ts[((R)+1) % 3]
                X6   += ks[((R)+7) % 9] + ts[((R)+2) % 3]
                X7   += ks[((R)+8) % 9] +     (R.toUInt())+1u
            }

            for(i in 0 until 9) {
                X0+=X1
                X1=RotL_64(X1,R_512_0_0)
                X1=X1 xor X0
                X2+=X3
                X3=RotL_64(X3,R_512_0_1)
                X3=X3 xor X2
                X4+=X5
                X5=RotL_64(X5,R_512_0_2)
                X5=X5 xor X4
                X6+=X7
                X7=RotL_64(X7,R_512_0_3)
                X7=X7 xor X6
                X2+=X1
                X1=RotL_64(X1,R_512_1_0)
                X1=X1 xor X2
                X4+=X7
                X7=RotL_64(X7,R_512_1_1)
                X7=X7 xor X4
                X6+=X5
                X5=RotL_64(X5,R_512_1_2)
                X5=X5 xor X6
                X0+=X3
                X3=RotL_64(X3,R_512_1_3)
                X3=X3 xor X0
                X4+=X1
                X1=RotL_64(X1,R_512_2_0)
                X1=X1 xor X4
                X6+=X3
                X3=RotL_64(X3,R_512_2_1)
                X3=X3 xor X6
                X0+=X5
                X5=RotL_64(X5,R_512_2_2)
                X5=X5 xor X0
                X2+=X7
                X7=RotL_64(X7,R_512_2_3)
                X7=X7 xor X2
                X6+=X1
                X1=RotL_64(X1,R_512_3_0)
                X1=X1 xor X6
                X0+=X7
                X7=RotL_64(X7,R_512_3_1)
                X7=X7 xor X0
                X2+=X5
                X5=RotL_64(X5,R_512_3_2)
                X5=X5 xor X2
                X4+=X3
                X3=RotL_64(X3,R_512_3_3)
                X3=X3 xor X4
                I512(2*(i))
                X0+=X1
                X1=RotL_64(X1,R_512_4_0)
                X1=X1 xor X0
                X2+=X3
                X3=RotL_64(X3,R_512_4_1)
                X3=X3 xor X2
                X4+=X5
                X5=RotL_64(X5,R_512_4_2)
                X5=X5 xor X4
                X6+=X7
                X7=RotL_64(X7,R_512_4_3)
                X7=X7 xor X6
                X2+=X1
                X1=RotL_64(X1,R_512_5_0)
                X1=X1 xor X2
                X4+=X7
                X7=RotL_64(X7,R_512_5_1)
                X7=X7 xor X4
                X6+=X5
                X5=RotL_64(X5,R_512_5_2)
                X5=X5 xor X6
                X0+=X3
                X3=RotL_64(X3,R_512_5_3)
                X3=X3 xor X0
                X4+=X1
                X1=RotL_64(X1,R_512_6_0)
                X1=X1 xor X4
                X6+=X3
                X3=RotL_64(X3,R_512_6_1)
                X3=X3 xor X6
                X0+=X5
                X5=RotL_64(X5,R_512_6_2)
                X5=X5 xor X0
                X2+=X7
                X7=RotL_64(X7,R_512_6_3)
                X7=X7 xor X2
                X6+=X1
                X1=RotL_64(X1,R_512_7_0)
                X1=X1 xor X6
                X0+=X7
                X7=RotL_64(X7,R_512_7_1)
                X7=X7 xor X0
                X2+=X5
                X5=RotL_64(X5,R_512_7_2)
                X5=X5 xor X2
                X4+=X3
                X3=RotL_64(X3,R_512_7_3)
                X3=X3 xor X4
                I512(2*(i)+1)
            }

            ctx.X[0] = X0 xor w[0]
            ctx.X[1] = X1 xor w[1]
            ctx.X[2] = X2 xor w[2]
            ctx.X[3] = X3 xor w[3]
            ctx.X[4] = X4 xor w[4]
            ctx.X[5] = X5 xor w[5]
            ctx.X[6] = X6 xor w[6]
            ctx.X[7] = X7 xor w[7]

            ts[1] = ts[1] and SKEIN_T1_FLAG_FIRST.inv()
        }while(--blkCnt != 0uL)
        ctx.h.T[0] = ts[0]
        ctx.h.T[1] = ts[1]
    }

    private fun Skein_512_Final(ctx: Skein512Ctx, hashval: UBytePointer){
        var n = 0uL

        val X = ULongArray(SKEIN_512_STATE_WORDS)
        ctx.h.T[1] = ctx.h.T[1] or SKEIN_T1_FLAG_FINAL
        if (ctx.h.bCnt < SKEIN_512_BLOCK_BYTES.toUInt()) {            /* zero pad b[] if necessary */
            ctx.b[ctx.h.bCnt.toInt()] = UByteArray((SKEIN_512_BLOCK_BYTES-ctx.h.bCnt).toInt())
            //memset(& ctx . b [ctx.h.bCnt], 0, SKEIN_512_BLOCK_BYTES-ctx.h.bCnt);
        }

        Skein_512_Process_Block(ctx, ctx.b, 1u, ctx.h.bCnt)  /* process the final block */

        /* now output the result */
        val byteCnt = (ctx.h.hashBitLen + 7u) shr 3

        //memset(ctx.b,0,sizeof(ctx.b));  /* zero out b[], so it can hold the counter */
        ctx.b[0] = UByteArray(ctx.b.size())
        //    memcpy(X,ctx.X,sizeof(X));
        for(s in 0 until X.size){
            X[s] = ctx.X[s]
        }

        var i = 0u
        while ((i * SKEIN_512_BLOCK_BYTES) < byteCnt) {
            ctx.b.toULongPointer()[0] = i.toULong()
            Skein_Start_New_Type(ctx, SKEIN_T1_BLK_TYPE_OUT_FINAL)
            Skein_512_Process_Block(ctx,ctx.b,1u,8u) /* run "counter mode" */
            n = (byteCnt - i.toUInt() * SKEIN_512_BLOCK_BYTES.toUInt())
            if (n >= SKEIN_512_BLOCK_BYTES.toUInt())
                n  = SKEIN_512_BLOCK_BYTES.toULong()
            Skein_Put64_LSB_First((hashval+(i*SKEIN_512_BLOCK_BYTES).toInt()).toUBytePointer(),ctx.X,n.toInt())   /* "output" the ctr mode bytes */

            for(s in 0 until X.size){
                ctx.X[s] = X[s]
            }
            i++
        }
    }

    private fun Skein_Put64_LSB_First(dst08: UBytePointer, src64: ULongPointer, bCnt: Int){
        dst08[0] = src64.toUBytePointer()[0, bCnt]
    }

    private fun Skein_Get64_LSB_First(dst64: ULongPointer, src08: UBytePointer, wCnt: Int){
        dst64.toUBytePointer()[0] = src08[0, 8*wCnt]
    }

    private fun Init(state: SkeinHashState, hashbitlen: Int) {
        state.statebits = 64 * SKEIN_512_STATE_WORDS
        Skein_512_Init(state.ctx_512, hashbitlen)
    }

    private fun Update(state: SkeinHashState, data: UBytePointer, databitlen: ULong) {
        Skein_512_Update(state.ctx_512, data, databitlen shr 3)
    }

    private fun Final(state: SkeinHashState, hashval: UBytePointer){
        Skein_512_Final(state.ctx_512, hashval)
    }

    private fun skein_hash(hashbitlen: Int, data: UBytePointer, /* all-in-one call */
                   databitlen: ULong, hashval: UBytePointer) {
        val state = SkeinHashState()
        Init(state, hashbitlen)
        Update(state, data, databitlen)
        Final(state, hashval)
    }

    fun hash_extra_skein(data: UBytePointer, length: ULong, hash: UBytePointer) {
        skein_hash(256, data, 8u * length, hash)
    }
}