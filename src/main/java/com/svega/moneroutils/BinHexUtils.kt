package com.svega.moneroutils

import com.svega.moneroutils.exceptions.MoneroException

@ExperimentalUnsignedTypes
object BinHexUtils {
    fun hexToByteArray(s: String): ByteArray {
        val len = s.length

        if (len % 2 != 0)
            throw MoneroException("Hex string has invalid length!")
        if (s.isEmpty())
            return byteArrayOf()

        val out = ByteArray(len / 2)

        var i = 0
        while (i < len) {
            val h = hexToBin(s[i])
            val l = hexToBin(s[i + 1])
            if (h == -1 || l == -1) {
                throw IllegalArgumentException(
                        "contains illegal character for hexBinary: $s")
            }

            out[i / 2] = (h * 16 + l).toByte()
            i += 2
        }

        return out
    }

    @ExperimentalUnsignedTypes
    fun hexToUByteArray(s: String): UByteArray {
        val len = s.length

        if (len % 2 != 0)
            throw MoneroException("Hex string has invalid length!")
        if (s.isEmpty())
            return ubyteArrayOf()

        val out = UByteArray(len / 2)

        var i = 0
        while (i < len) {
            val h = hexToBin(s[i])
            val l = hexToBin(s[i + 1])
            if (h == -1 || l == -1) {
                throw IllegalArgumentException(
                        "contains illegal character for hexBinary: $s")
            }

            out[i / 2] = (h * 16 + l).toUByte()
            i += 2
        }

        return out
    }

    private fun hexToBin(ch: Char): Int {
        if (ch in '0'..'9') {
            return ch - '0'
        }
        if (ch in 'A'..'F') {
            return ch - 'A' + 10
        }
        return if (ch in 'a'..'f') {
            ch - 'a' + 10
        } else -1
    }

    fun convertHexToString(hex: String) = String(hexToByteArray(hex))

    private val hexCode = "0123456789ABCDEF".toCharArray()
    private fun printHexBinary(data: ByteArray): String {
        val r = StringBuilder(data.size * 2)
        for (b in data) {
            r.append(hexCode[b.toInt() shr 4 and 0xF])
            r.append(hexCode[b.toInt() and 0xF])
        }
        return r.toString()
    }

    fun binaryToHex(bin: ByteArray) = printHexBinary(bin)

    fun ubinaryToHex(bin: UByteArray) = printHexBinary(bin.asByteArray())

    fun binaryToHex(bin: List<Byte>) = printHexBinary(bin.toByteArray())
}