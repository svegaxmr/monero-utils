package com.svega.moneroutils.crypto.slowhash

@ExperimentalUnsignedTypes
object Keccak {

    private val keccakfRndc =
            ulongArrayOf(
                    0x0000000000000001u, 0x0000000000008082u, 0x800000000000808au,
                    0x8000000080008000u, 0x000000000000808bu, 0x0000000080000001u,
                    0x8000000080008081u, 0x8000000000008009u, 0x000000000000008au,
                    0x0000000000000088u, 0x0000000080008009u, 0x000000008000000au,
                    0x000000008000808bu, 0x800000000000008bu, 0x8000000000008089u,
                    0x8000000000008003u, 0x8000000000008002u, 0x8000000000000080u,
                    0x000000000000800au, 0x800000008000000au, 0x8000000080008081u,
                    0x8000000000008080u, 0x0000000080000001u, 0x8000000080008008u
            )

    private val keccakfRotc =
            intArrayOf(
                    1,  3,  6,  10, 15, 21, 28, 36, 45, 55, 2,  14,
                    27, 41, 56, 8,  25, 43, 62, 18, 39, 61, 20, 44
            )

    private val keccakfPiln =
            intArrayOf(
                    10, 7,  11, 17, 18, 3, 5,  16, 8,  21, 24, 4,
                    15, 23, 19, 13, 12, 2, 20, 14, 22, 9,  6,  1
            )
    private fun ROTL64(x: ULong, y: Int) = (((x) shl (y)) or ((x) shr (64 - (y))))

    fun keccakf(st: ULongPointer, rounds: Int)
    {
        var i: Int
        var j: Int
        var round = 0
        var t: ULong
        val bc = ULongArray(5)

        while (round < rounds) {

            // Theta
            i = 0
            while (i < 5) {
                bc[i] = st[i] xor st[i + 5] xor st[i + 10] xor st[i + 15] xor st[i + 20]
                i++
            }

            i = 0
            while (i < 5) {
                t = bc[(i + 4) % 5] xor ROTL64(bc[(i + 1) % 5], 1)
                j = 0
                while (j < 25) {
                    st[j + i] = st[j + i] xor t
                    j += 5
                }
                i++
            }

            // Rho Pi
            t = st[1]
            i = 0
            while (i < 24) {
                j = keccakfPiln[i]
                bc[0] = st[j]
                st[j] = ROTL64(t, keccakfRotc[i])
                t = bc[0]
                i++
            }

            //  Chi
            j = 0
            while (j < 25) {
                i = 0
                while (i < 5) {
                    bc[i] = st[j + i]
                    i++
                }
                i = 0
                while (i < 5) {
                    st[j + i] = st[j + i] xor (bc[(i + 1) % 5].inv() and bc[(i + 2) % 5])
                    i++
                }
                j += 5
            }

            //  Iota
            st[0] = st[0] xor keccakfRndc[round]
            round++
        }
    }
    private const val KECCAK_ROUNDS = 24
    private const val HASH_DATA_AREA = 136
    fun keccak(din_: UBytePointer, md: UBytePointer)
    {
        var din = din_
        var inlen = din.size()
        val spst = Scratchpad.getScratchpad(200)
        val st = spst.getPointer(0).toULongPointer()
        val temp = Scratchpad.getScratchpad(144).getPointer(0)
        val rsiz: Int = HASH_DATA_AREA
        val rsizw = rsiz / 8

        while (inlen >= rsiz) {
            for (i in 0 until rsizw)
                st[i] = st[i] xor din.toULongPointer()[i]
            keccakf(st, KECCAK_ROUNDS)
            inlen -= rsiz
            din = (din + rsiz).toUBytePointer()
        }

        temp[0] = din[0, inlen]
        temp[inlen++] = 1u
        temp[inlen] = UByteArray(rsiz - inlen)
        temp[rsiz - 1] = temp[rsiz - 1] or 0x80u

        for (i in 0 until rsizw)
            st[i] = st[i] xor temp.toULongPointer()[i]

        keccakf(st, KECCAK_ROUNDS)

        md[0] = spst[0, md.size()]
    }

    fun keccak1600(din: UBytePointer, md: UBytePointer)
    {
        keccak(din, md)
    }

    fun rawKeccak(data: ByteArray) = rawKeccak(data.asUByteArray()).asByteArray()

    fun rawKeccak(data: UByteArray): UByteArray {
        val sp = Scratchpad.wrap(data)
        val out = Scratchpad.getScratchpad(32)
        keccak1600(sp.getPointer(), out.getPointer())
        return out.getRawArray()
    }

    fun fullChecksum(data: UByteArray): UByteArray {
        val sp = Scratchpad.wrap(data)
        val out = Scratchpad.getScratchpad(32)
        keccak1600(sp.getPointer(), out.getPointer())
        return out.getRawArray().copyOfRange(0, 4)
    }

    fun checkChecksum(data: UByteArray): Boolean {
        val toCheck = data.copyOfRange(0, data.size - 4)
        val preCSum = data.copyOfRange(data.size - 4, data.size)
        return preCSum.contentEquals(fullChecksum(toCheck))
    }
}