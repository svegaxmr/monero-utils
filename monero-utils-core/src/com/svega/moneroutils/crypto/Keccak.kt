package com.svega.moneroutils.crypto

import java.math.BigInteger

val DEFAULT_KECCAK_PERMUTATION_WIDTH = 1600

class Keccak{

    companion object {
        private var w: Int = 0
        private var n: Int = 0

        init {
            w = DEFAULT_KECCAK_PERMUTATION_WIDTH / 25
            val l = (Math.log(w.toDouble()) / Math.log(2.0)).toInt()
            n = 12 + 2 * l
        }

        /**
         * max unsigned long
         */
        private val BIT_64 = BigInteger("18446744073709551615")

        fun getReverseHex(input: ByteArray) = input.reversed().toHexString("")

        /**
         * round constants
         */
        private val RC = arrayOf(BigInteger("0000000000000001", 16), BigInteger("0000000000008082", 16), BigInteger("800000000000808A", 16), BigInteger("8000000080008000", 16), BigInteger("000000000000808B", 16), BigInteger("0000000080000001", 16), BigInteger("8000000080008081", 16), BigInteger("8000000000008009", 16), BigInteger("000000000000008A", 16), BigInteger("0000000000000088", 16), BigInteger("0000000080008009", 16), BigInteger("000000008000000A", 16), BigInteger("000000008000808B", 16), BigInteger("800000000000008B", 16), BigInteger("8000000000008089", 16), BigInteger("8000000000008003", 16), BigInteger("8000000000008002", 16), BigInteger("8000000000000080", 16), BigInteger("000000000000800A", 16), BigInteger("800000008000000A", 16), BigInteger("8000000080008081", 16), BigInteger("8000000000008080", 16), BigInteger("0000000080000001", 16), BigInteger("8000000080008008", 16))

        // The rotation offsets r[x,y].
        private val r = arrayOf(intArrayOf(0, 36, 3, 41, 18), intArrayOf(1, 44, 10, 45, 2), intArrayOf(62, 6, 43, 15, 61), intArrayOf(28, 55, 25, 21, 56), intArrayOf(27, 20, 39, 8, 14))

        fun getHash(message: String, parameter: Parameter)
                = getHash(message.toByteArray(), parameter)

        fun getHash(byteArray: ByteArray, parameter: Parameter): String {
            val message = byteArray.toHexString("")
            // Initialization and padding
            val S = Array(5) { Array(5) { BigInteger.ZERO } }

            val P = padding(message, parameter)

            // Absorbing phase
            for (Pi in P) {
                for (i in 0 until 5) {
                    (0 until 5).filter { i + it * 5 < parameter.r / w }
                            .forEach { S[i][it] = S[i][it] xor Pi[i + it * 5] }
                }

                doKeccackf(S)
            }

            // Squeezing phase
            var Z = ""

            do {
                for (x in 0..parameter.r / w) {
                    Z += getReverseHex(S[x % 5][x / 5].toByteArray()).fillWithZero(16).substring(0, 16)
                }
                doKeccackf(S)
            } while (Z.length < parameter.outputLengthInBytes * 2)

            return Z.substring(0, parameter.outputLengthInBytes * 2)
        }

        private fun doKeccackf(A: Array<Array<BigInteger>>): Array<Array<BigInteger>> {
            var NEWA = A
            for (i in 0 until n) {
                NEWA = roundB(NEWA, RC[i])
            }

            return NEWA
        }

        private fun roundB(A: Array<Array<BigInteger>>, RC: BigInteger): Array<Array<BigInteger>> {
            val C = createArrayOfFiveZEROs()
            val D = createArrayOfFiveZEROs()
            val B = Array<Array<BigInteger>>(5) { createArrayOfFiveZEROs() }

            //θ step
            for (i in 0 until 5) {
                C[i] = A[i][0] xor A[i][1] xor A[i][2] xor A[i][3]xor A[i][4]
            }

            for (i in 0 until 5) {
                D[i] = C[(i + 4) % 5] xor rot(C[(i + 1) % 5], 1)
            }

            for (i in 0 until 5) {
                for (j in 0 until 5) {
                    A[i][j] = A[i][j] xor D[i]
                }
            }

            //ρ and π steps
            for (i in 0 until 5) {
                for (j in 0 until 5) {
                    B[j][(2 * i + 3 * j) % 5] = rot(A[i][j], r[i][j])
                }
            }

            //χ step
            for (i in 0..4) {
                for (j in 0..4) {
                    A[i][j] = B[i][j] xor (!B[(i + 1) % 5][j] and B[(i + 2) % 5][j])
                }
            }

            //ι step
            A[0][0] = A[0][0] xor RC

            return A
        }

        private fun createArrayOfFiveZEROs() = arrayOf(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)

        private fun rot(x: BigInteger, n_in: Int): BigInteger {
            val n = n_in % w

            val leftShift = getShiftLeft64(x, n)
            val rightShift = x shr w - n

            return leftShift or rightShift
        }

        private fun getShiftLeft64(value: BigInteger, shift: Int): BigInteger {
            var retValue = value shl shift
            var tmpValue = value shl shift

            if (retValue > BIT_64) {
                for (i in 64 until 64 + shift) {
                    tmpValue = tmpValue.clearBit(i)
                }

                tmpValue = tmpValue.setBit(64 + shift)
                retValue = tmpValue and retValue
            }

            return retValue
        }

        private fun padding(inputMessage: String, parameter: Parameter): Array<Array<BigInteger>> {
            var message = inputMessage
            val size: Int
            message += parameter.d

            while (message.length / 2 * 8 % parameter.r != parameter.r - 8) {
                message += "00"
            }

            message += "80"
            size = message.length / 2 * 8 / parameter.r

            val arrayM = Array(size) { Array(1600 / w) { BigInteger.ZERO } }

            var count = 0
            var j = 0
            var i = 0

            for (_n in 0 until message.length) {

                if (j > parameter.r / w - 1) {
                    j = 0
                    i++
                }

                count++

                if (count * 4 % w == 0) {
                    val subString = message.substring(count - w / 4, w / 4 + (count - w / 4))
                    arrayM[i][j] = BigInteger(subString, 16)
                    var revertString = getReverseHex(arrayM[i][j].toByteArray())
                    revertString = revertString.fillWithZero(subString.length)
                    arrayM[i][j] = BigInteger(revertString, 16)
                    j++
                }

            }

            return arrayM
        }

        internal fun String.fillWithZero(fillLength: Int)
                = this + "0".repeat(Math.max(fillLength - this.length, 0))
    }
}

private const val CHARS = "0123456789abcdef"

/**
 *  Returns 2 char hex string for Byte
 */
fun Byte.toHexString() = toInt().let {
    CHARS[it.shr(4) and 0x0f].toString() + CHARS[it.and(0x0f)].toString()
}

fun ByteArray.toHexString(prefix: String = "0x") = prefix + map { it.toHexString() }.joinToString("")
fun List<Byte>.toHexString(prefix: String = "0x") = toByteArray().toHexString(prefix)

enum class Parameter constructor(val r: Int,
                                 val outputLengthInBytes: Int,
                                 val d: String) {

    KECCAK_224(1152, 28, "01"),
    KECCAK_256(1088, 32, "01"),
    KECCAK_384(832, 48, "01"),
    KECCAK_512(576, 64, "01"),
    SHA3_224(1152, 28, "06"),
    SHA3_256(1088, 32, "06"),
    SHA3_384(832, 48, "06"),
    SHA3_512(576, 64, "06"),
    SHAKE128(1344, 32, "1F"),
    SHAKE256(1088, 64, "1F")
}