/*
 * Copyright (c) 2018-2019, Sergio Vega
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object Skein {

    data class SkeinHashState(
            var statebits: Int = 256,
            val ctx_512: Skein512Ctx = Skein512Ctx()
    )

    const val SKEIN_MODIFIER_WORDS = (2)          /* number of modifier (tweak) words */

    const val SKEIN_512_STATE_WORDS = (8)
    val SKEIN_512_BLOCK_BYTES = (8 * SKEIN_512_STATE_WORDS).toUInt()

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

    private const val SKEIN_T1_POS_BLK_TYPE = 120 - 64   /* bits 120..125: type field   */
    private const val SKEIN_T1_POS_FIRST = 126 - 64   /* bits 126 : first block flag */
    private const val SKEIN_T1_POS_FINAL = 127 - 64   /* bit  127 : final block flag */

    /* tweak word T[1]: flag bit definition(s) */
    private val SKEIN_T1_FLAG_FIRST = ((1uL) shl SKEIN_T1_POS_FIRST)
    private val SKEIN_T1_FLAG_FINAL = ((1uL) shl SKEIN_T1_POS_FINAL)
    private const val SKEIN_BLK_TYPE_MSG = (48)/* message processing */
    private const val SKEIN_BLK_TYPE_OUT = (63)/* output stage */
    private val SKEIN_T1_BLK_TYPE_MSG = SKEIN_BLK_TYPE_MSG.toULong() shl SKEIN_T1_POS_BLK_TYPE  /* message processing */
    private val SKEIN_T1_BLK_TYPE_OUT = SKEIN_BLK_TYPE_OUT.toULong() shl SKEIN_T1_POS_BLK_TYPE  /* output stage */
    private val SKEIN_T1_BLK_TYPE_OUT_FINAL = (SKEIN_T1_BLK_TYPE_OUT or SKEIN_T1_FLAG_FINAL)

    private fun SKEIN_MK_64(hi32: UInt, lo32: UInt) = ((lo32) + (hi32.toULong() shl 32))
    private val SKEIN_KS_PARITY = SKEIN_MK_64(0x1BD11BDAu, 0xA9FC1A22u)

    private fun skeinSetTweak(ctxPtr: Skein512Ctx, TWK_NUM: Int, tVal: ULong) {
        ctxPtr.h.T[TWK_NUM] = tVal
    }

    private fun skeinSetT0(ctxPtr: Skein512Ctx, T0: ULong) = skeinSetTweak(ctxPtr, 0, T0)
    private fun skeinSetT1(ctxPtr: Skein512Ctx, T1: ULong) = skeinSetTweak(ctxPtr, 1, T1)

    private fun skeinSetT0T1(ctxPtr: Skein512Ctx, T0: ULong, T1: ULong) {
        skeinSetT0(ctxPtr, T0)
        skeinSetT1(ctxPtr, T1)
    }

    private fun skeinStartNewType(ctxPtr: Skein512Ctx, BLK_TYPE: ULong) {
        skeinSetT0T1(ctxPtr, 0u, SKEIN_T1_FLAG_FIRST or BLK_TYPE)
        ctxPtr.h.bCnt = 0u
    }

    /* Skein_512 round rotation constants */
    private const val R_512_0_0 = 46
    private const val R_512_0_1 = 36
    private const val R_512_0_2 = 19
    private const val R_512_0_3 = 37

    private const val R_512_1_0 = 33
    private const val R_512_1_1 = 27
    private const val R_512_1_2 = 14
    private const val R_512_1_3 = 42

    private const val R_512_2_0 = 17
    private const val R_512_2_1 = 49
    private const val R_512_2_2 = 36
    private const val R_512_2_3 = 39

    private const val R_512_3_0 = 44
    private const val R_512_3_1 = 9
    private const val R_512_3_2 = 54
    private const val R_512_3_3 = 56

    private const val R_512_4_0 = 39
    private const val R_512_4_1 = 30
    private const val R_512_4_2 = 34
    private const val R_512_4_3 = 24

    private const val R_512_5_0 = 13
    private const val R_512_5_1 = 50
    private const val R_512_5_2 = 10
    private const val R_512_5_3 = 17

    private const val R_512_6_0 = 25
    private const val R_512_6_1 = 29
    private const val R_512_6_2 = 39
    private const val R_512_6_3 = 43

    private const val R_512_7_0 = 8
    private const val R_512_7_1 = 35
    private const val R_512_7_2 = 56
    private const val R_512_7_3 = 22

    private fun MK64(hi32: UInt, lo32: UInt) = SKEIN_MK_64(hi32, lo32)

    private val SKEIN_512_IV_256 = ulongArrayOf(
            MK64(0xCCD044A1u, 0x2FDB3E13u),
            MK64(0xE8359030u, 0x1A79A9EBu),
            MK64(0x55AEA061u, 0x4F816E6Fu),
            MK64(0x2A2767A4u, 0xAE9B94DBu),
            MK64(0xEC06025Eu, 0x74DD7683u),
            MK64(0xE7A436CDu, 0xC4746251u),
            MK64(0xC36FBAF9u, 0x393AD185u),
            MK64(0x3EEDBA18u, 0x33EDFC13u)
    )

    /*
    #define BLK_BITS        (WCNT*64)               /* some useful definitions for code here */
    #define KW_TWK_BASE     (0)
    #define KW_KEY_BASE     (3)
    #define ks              (kw + KW_KEY_BASE)
    #define ts              (kw + KW_TWK_BASE)
     */

    private fun skein512Init(ctx: Skein512Ctx, hashBitLen: Int) {
        ctx.h.hashBitLen = hashBitLen.toULong()         /* output hash bit count */

        for (i in 0 until SKEIN_512_IV_256.size) {
            ctx.X[i] = SKEIN_512_IV_256[i]
        }

        //memcpy(ctx.X, SKEIN_512_IV_256, sizeof { ctx -> X })

        /* The chaining vars ctx->X are now initialized for the given hashBitLen. */
        /* Set up to process the data message portion of the hash (default) */
        //skeinStartNewType(ctx,MSG);              /* T0=0, T1= MSG type */
        skeinStartNewType(ctx, SKEIN_T1_BLK_TYPE_MSG)
    }

    private fun skein512Update(ctx: Skein512Ctx, msg_: UBytePointer, msgByteCnt_: ULong) {
        var msg = msg_
        var msgByteCnt = msgByteCnt_
        var n: ULong

        /* process full blocks, if any */
        if (msgByteCnt + ctx.h.bCnt > SKEIN_512_BLOCK_BYTES) {
            if (ctx.h.bCnt != 0uL) {                             /* finish up any buffered message data */
                n = SKEIN_512_BLOCK_BYTES.toULong() - ctx.h.bCnt  /* # bytes free in buffer b[] */
                if (n != 0uL) {
                    ctx.b[ctx.h.bCnt.toInt()] = msg[0, n.toInt()]
                    //memcpy(&ctx.b[ctx.h.bCnt],msg,n);
                    msgByteCnt -= n
                    msg = (msg + n.toInt()).toUBytePointer()
                    ctx.h.bCnt += n
                }
                skein512ProcessBlock(ctx, ctx.b, 1u, SKEIN_512_BLOCK_BYTES.toULong())
                ctx.h.bCnt = 0uL
            }
            /* now process any remaining full blocks, directly from input message data */
            if (msgByteCnt > SKEIN_512_BLOCK_BYTES) {
                n = ((msgByteCnt - 1u) / SKEIN_512_BLOCK_BYTES).toULong()   /* number of full blocks to process */
                skein512ProcessBlock(ctx, msg, n, SKEIN_512_BLOCK_BYTES.toULong())
                msgByteCnt -= (n * SKEIN_512_BLOCK_BYTES)
                msg = (msg + (n * SKEIN_512_BLOCK_BYTES).toInt()).toUBytePointer()
            }
        }

        /* copy any remaining source message data bytes into b[] */
        if (msgByteCnt != 0uL) {
            ctx.b[ctx.h.bCnt.toInt()] = msg[0, msgByteCnt.toInt()]
            //memcpy(&ctx.b[ctx.h.bCnt],msg,msgByteCnt);
            ctx.h.bCnt += msgByteCnt.toUInt()
        }
    }

    private fun skein512ProcessBlock(ctx: Skein512Ctx, blkPtr_: UBytePointer, blkCnt_: ULong, byteCntAdd: ULong) { /* do it in C */
        var blkPtr = blkPtr_
        var blkCnt = blkCnt_
        val WCNT = SKEIN_512_STATE_WORDS

        val kw = Scratchpad.getScratchpad((WCNT + 4) * 8).getPointer(0).toULongPointer()
        val X = ULongArray(8)

        val w = Scratchpad.getScratchpad(WCNT * 8).getPointer(0).toULongPointer()                           /* local copy of input block */

        val ks = (kw + 3).toULongPointer()
        val ts = (kw + 0).toULongPointer()

        ts[0] = ctx.h.T[0]
        ts[1] = ctx.h.T[1]

        do {
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

            skeinGet64LSBFirst(w, blkPtr, WCNT) /* get input block in little-endian format */

            X[0] = w[0] + ks[0]                    /* do the first full key injection */
            X[1] = w[1] + ks[1]
            X[2] = w[2] + ks[2]
            X[3] = w[3] + ks[3]
            X[4] = w[4] + ks[4]
            X[5] = w[5] + ks[5] + ts[0]
            X[6] = w[6] + ks[6] + ts[1]
            X[7] = w[7] + ks[7]

            blkPtr = (blkPtr + SKEIN_512_BLOCK_BYTES.toInt()).toUBytePointer()
            fun RotL_64(x: ULong, N: Int) = (x shl N) or (x shr (64 - N))
            fun I512(R: Int) {
                X[0] += ks[((R) + 1) % 9]   /* inject the key schedule value */
                X[1] += ks[((R) + 2) % 9]
                X[2] += ks[((R) + 3) % 9]
                X[3] += ks[((R) + 4) % 9]
                X[4] += ks[((R) + 5) % 9]
                X[5] += ks[((R) + 6) % 9] + ts[((R) + 1) % 3]
                X[6] += ks[((R) + 7) % 9] + ts[((R) + 2) % 3]
                X[7] += ks[((R) + 8) % 9] + (R.toUInt()) + 1u
            }

            fun S(first: Int, second: Int, rotlVal: Int) {
                X[first] += X[second]
                X[second] = RotL_64(X[second], rotlVal)
                X[second] = X[second] xor X[first]
            }

            for (i in 0 until 9) {
                S(0, 1, R_512_0_0)
                S(2, 3, R_512_0_1)
                S(4, 5, R_512_0_2)
                S(6, 7, R_512_0_3)
                S(2, 1, R_512_1_0)
                S(4, 7, R_512_1_1)
                S(6, 5, R_512_1_2)
                S(0, 3, R_512_1_3)
                S(4, 1, R_512_2_0)
                S(6, 3, R_512_2_1)
                S(0, 5, R_512_2_2)
                S(2, 7, R_512_2_3)
                S(6, 1, R_512_3_0)
                S(0, 7, R_512_3_1)
                S(2, 5, R_512_3_2)
                S(4, 3, R_512_3_3)
                I512(2 * (i))
                S(0, 1, R_512_4_0)
                S(2, 3, R_512_4_1)
                S(4, 5, R_512_4_2)
                S(6, 7, R_512_4_3)
                S(2, 1, R_512_5_0)
                S(4, 7, R_512_5_1)
                S(6, 5, R_512_5_2)
                S(0, 3, R_512_5_3)
                S(4, 1, R_512_6_0)
                S(6, 3, R_512_6_1)
                S(0, 5, R_512_6_2)
                S(2, 7, R_512_6_3)
                S(6, 1, R_512_7_0)
                S(0, 7, R_512_7_1)
                S(2, 5, R_512_7_2)
                S(4, 3, R_512_7_3)
                I512(2 * (i) + 1)
            }

            ctx.X[0] = X[0] xor w[0]
            ctx.X[1] = X[1] xor w[1]
            ctx.X[2] = X[2] xor w[2]
            ctx.X[3] = X[3] xor w[3]
            ctx.X[4] = X[4] xor w[4]
            ctx.X[5] = X[5] xor w[5]
            ctx.X[6] = X[6] xor w[6]
            ctx.X[7] = X[7] xor w[7]

            ts[1] = ts[1] and SKEIN_T1_FLAG_FIRST.inv()
        } while (--blkCnt != 0uL)
        ctx.h.T[0] = ts[0]
        ctx.h.T[1] = ts[1]
    }

    private fun skein512Final(ctx: Skein512Ctx, hashval: UBytePointer) {
        var n: ULong

        val X = ULongArray(SKEIN_512_STATE_WORDS)
        ctx.h.T[1] = ctx.h.T[1] or SKEIN_T1_FLAG_FINAL
        if (ctx.h.bCnt < SKEIN_512_BLOCK_BYTES.toUInt()) {            /* zero pad b[] if necessary */
            ctx.b[ctx.h.bCnt.toInt()] = UByteArray((SKEIN_512_BLOCK_BYTES - ctx.h.bCnt).toInt())
            //memset(& ctx . b [ctx.h.bCnt], 0, SKEIN_512_BLOCK_BYTES-ctx.h.bCnt);
        }

        skein512ProcessBlock(ctx, ctx.b, 1u, ctx.h.bCnt)  /* process the final block */

        /* now output the result */
        val byteCnt = (ctx.h.hashBitLen + 7u) shr 3

        //memset(ctx.b,0,sizeof(ctx.b));  /* zero out b[], so it can hold the counter */
        ctx.b[0] = UByteArray(ctx.b.size())
        //    memcpy(X,ctx.X,sizeof(X));
        for (s in 0 until X.size) {
            X[s] = ctx.X[s]
        }

        var i = 0u
        while ((i * SKEIN_512_BLOCK_BYTES) < byteCnt) {
            ctx.b.toULongPointer()[0] = i.toULong()
            skeinStartNewType(ctx, SKEIN_T1_BLK_TYPE_OUT_FINAL)
            skein512ProcessBlock(ctx, ctx.b, 1u, 8u) /* run "counter mode" */
            n = (byteCnt - i.toUInt() * SKEIN_512_BLOCK_BYTES.toUInt())
            if (n >= SKEIN_512_BLOCK_BYTES.toUInt())
                n = SKEIN_512_BLOCK_BYTES.toULong()
            skeinPut64LSBFirst((hashval + (i * SKEIN_512_BLOCK_BYTES).toInt()).toUBytePointer(), ctx.X, n.toInt())   /* "output" the ctr mode bytes */

            for (s in 0 until X.size) {
                ctx.X[s] = X[s]
            }
            i++
        }
    }

    private fun skeinPut64LSBFirst(dst08: UBytePointer, src64: ULongPointer, bCnt: Int) {
        dst08[0] = src64.toUBytePointer()[0, bCnt]
    }

    private fun skeinGet64LSBFirst(dst64: ULongPointer, src08: UBytePointer, wCnt: Int) {
        dst64.toUBytePointer()[0] = src08[0, 8 * wCnt]
    }

    private fun init(state: SkeinHashState, hashbitlen: Int) {
        state.statebits = 64 * SKEIN_512_STATE_WORDS
        skein512Init(state.ctx_512, hashbitlen)
    }

    private fun update(state: SkeinHashState, data: UBytePointer, databitlen: ULong) {
        skein512Update(state.ctx_512, data, databitlen shr 3)
    }

    private fun final(state: SkeinHashState, hashval: UBytePointer) {
        skein512Final(state.ctx_512, hashval)
    }

    private fun skeinHash(hashbitlen: Int, data: UBytePointer, /* all-in-one call */
                          databitlen: ULong, hashval: UBytePointer) {
        val state = SkeinHashState()
        init(state, hashbitlen)
        update(state, data, databitlen)
        final(state, hashval)
    }

    fun skein256Hash(hash: UBytePointer, data: UBytePointer, length: ULong) {
        skeinHash(256, data, 8u * length, hash)
    }
}