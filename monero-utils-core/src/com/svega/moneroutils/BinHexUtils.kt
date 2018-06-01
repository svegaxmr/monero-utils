package com.svega.moneroutils

class BinHexUtils {
    companion object {
        fun hexToBinary(hex: String): Array<UInt8>{
            if (hex.length % 2 != 0)
                throw MoneroException("Hex string has invalid length!")
            if(hex.isEmpty())
                return Array(0, {UInt8(0)})
            val res = Array(hex.length / 2, {_ -> UInt8(0)})
            for (i in 0 until hex.length / 2) {
                res[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16).toUInt8()
            }
            return res
        }

        fun hexToByteArray(hex: String): ByteArray{
            if (hex.length % 2 != 0)
                throw MoneroException("Hex string has invalid length!")
            if(hex.isEmpty())
                return byteArrayOf()
            val l = hex.length
            val data = ByteArray(l / 2)
            var i = 0
            while (i < l) {
                data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }

        fun convertHexToString(hex: String): String {
            val sb = StringBuilder()
            var i = 0
            while (i < hex.length - 1) {
                sb.append(Integer.parseInt(hex.substring(i, i + 2), 16).toChar())
                i += 2
            }
            return sb.toString()
        }

        fun binaryToHex(bin: Array<UInt8>) : String{
            val out = StringBuilder()
            for (b in bin) {
                out.append(String.format("%02X", b.toByte()))
            }
            return out.toString()
        }

        fun binaryToHex(bin: ByteArray) : String{
            val out = StringBuilder()
            for (b in bin) {
                out.append(String.format("%02X", b))
            }
            return out.toString()
        }

        fun binaryToHex(bin: List<Byte>) : String{
            val out = StringBuilder()
            for (b in bin) {
                out.append(String.format("%02X", b))
            }
            return out.toString()
        }

        fun stringToBinary(str: String) : Array<UInt8>{
            val bytes = str.toByteArray()
            val ret = Array(bytes.size, {_ -> UInt8(0)})
            for(i in 0 until bytes.size){
                ret[i] = bytes[i].toUInt8()
            }
            return ret
        }

        fun binaryToString(bin: Array<UInt8>) : String {
            val cr = CharArray(bin.size)

            for(i in 0 until bin.size){
                cr[i] = bin[i].toChar()
                if(cr[i].toInt() >= 128)
                    cr[i] = (cr[i].toInt() and 0x00FF).toChar()
            }

            return String(cr)
        }
    }
}