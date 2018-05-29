package com.svega.moneroutils

class BinHexUtils {
    companion object {
        fun hexToBinary(hex: String): Array<UInt8>{
            if (hex.length % 2 != 0)
                throw MoneroException("Hex string has invalid length!")
            if(hex.isEmpty())
                return Array(0, {UInt8(0)})
            println(hex)
            for(b in hex.toByteArray())
                print(" ${b.toInt()}")
            println()
            val res = Array(hex.length / 2, {_ -> UInt8(0)})
            for (i in 0 until hex.length / 2) {
                println("${hex.substring(i * 2, i * 2 + 2).length}: ${hex.substring(i * 2, i * 2 + 2)}")
                try {
                    res[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16).toUInt8()
                }catch (_: NumberFormatException){
                    res[i] = UInt8(0)
                }

            }
            return res
        }

        fun hexToByteArray(hex: String): ByteArray{
            if (hex.length % 2 != 0)
                throw MoneroException("Hex string has invalid length!")
            if(hex.isEmpty())
                return byteArrayOf()
            val res = ByteArray(hex.length / 2, {_ -> 0})
            for (i in 0 until hex.length / 2) {
                res[i] = Integer.parseInt(hex.replace('\u0000','0').substring(i * 2, i * 2 + 2), 16).toByte()
            }
            return res
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

        @Throws(MoneroException::class)
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