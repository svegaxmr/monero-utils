package com.svega.moneroutils

import com.svega.moneroutils.BinHexUtils.hexToUByteArray
import java.math.BigInteger

@ExperimentalUnsignedTypes
object Base58{
	private const val alphabetStr = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
	private val alphabet = alphabetStr.toByteArray().asUByteArray()
	private val encodedBlockSizes = intArrayOf(0, 2, 3, 5, 6, 7, 9, 10, 11)
	private val alphabetSize = alphabet.size
	private const val fullBlockSize = 8
	private const val fullEncodedBlockSize = 11
	private val twoPow8 = 256u
	private val UINT64_MAX = BigInteger("2").pow(64)

	fun getAlphabetStr() = alphabetStr

	private fun uint8BufToUInt64(data: UByteArray) : ULong{
		if (data.isEmpty() || data.size > 8) {
			throw MoneroException("Invalid input length ${data.size}")
		}
		var res = 0uL
		var i = 0
		for(c in 9 - data.size until 9) {
			when(c == 1) {
				true -> {
                    res += data[i++].toULong()
                }
				false ->{
                    res *= twoPow8
                    res += data[i++].toULong()
                }
			}
		}
		return res
	}

    private fun uint64ToUInt8Buf(num: ULong, size: Int) : UByteArray {
        if (size < 1 || size > 8) {
            throw MoneroException("Invalid input length")
        }
        var numByteArray = num.toUByteArray()
        if(numByteArray[0] > 127u){
            numByteArray = UByteArray(numByteArray.size + 1){
                when(it){
                    0 -> 0u
                    else -> numByteArray[it - 1]
                }
            }
        }
        val res = UByteArray(size)
        numByteArray.copyInto(res,
                size - if(numByteArray.size <= size) numByteArray.size else size,
                if(numByteArray.size <= size) 0 else numByteArray.size - size,
                (if(numByteArray.size <= size) 0 else numByteArray.size - size) + (if(numByteArray.size <= size) numByteArray.size else size))
		return res
	}

	private fun encodeBlock(data: UByteArray, buf: UByteArray, index: Int) : UByteArray {
		if (data.isEmpty() || data.size > fullEncodedBlockSize) {
			throw MoneroException("Invalid block length: ${data.size}")
		}
		var num = uint8BufToUInt64(data)
		var i = encodedBlockSizes[data.size] - 1
		// while num > 0
        var lastNum = num
		while ((lastNum >= num) and (num > 0uL)) {
            lastNum = num
            val remainder = num % alphabetSize.toULong()
			num = num / alphabetSize.toUInt()
			// remainder = num % alphabetSize
			// num = num / alphabet_size
			buf[index + i] = alphabet[remainder.toInt()]
			i--
		}
		return buf
	}

	@Throws(MoneroException::class)
	fun encode(hex: String) : String{
		val data = hexToUByteArray(hex)
		if (data.isEmpty()) {
			return ""
		}
		val fullBlockCount = Math.floor(data.size.toDouble() / fullBlockSize).toInt()
		val sizeOfLastBlock = data.size % fullBlockSize
		val resSize = (fullBlockCount * fullEncodedBlockSize + encodedBlockSizes[sizeOfLastBlock])
		var res = UByteArray(resSize)
        for (i in 0 until resSize) {
			res[i] = alphabet[0]
		}
		for (i in 0 until fullBlockCount) {
			res = encodeBlock(data.copyOfRange(i * fullBlockSize, i * fullBlockSize + fullBlockSize), res, i * fullEncodedBlockSize)
		}
		if (sizeOfLastBlock > 0) {
			res = encodeBlock(data.copyOfRange(fullBlockCount * fullBlockSize, fullBlockCount * fullBlockSize + sizeOfLastBlock), res, fullBlockCount * fullEncodedBlockSize)
		}
		return String(res.toByteArray())
	}

	private fun decodeBlock(data: UByteArray, buf: UByteArray, index: Int) : UByteArray{
		if (data.isEmpty() || data.size > fullEncodedBlockSize) {
			throw MoneroException("Invalid block length: ${data.size}")
		}
		val resSize = encodedBlockSizes.indexOf(data.size)
		if (resSize <= 0) {
			throw MoneroException("Invalid block size")
		}
		var resNum = BigInteger.ZERO
		var order = BigInteger.ONE
		for (i in data.size - 1 downTo 0) {
			val digit = alphabet.indexOf(data[i])
			if (digit < 0) {
				throw MoneroException("Invalid symbol")
			}
			val product = order.multiply(digit.toBigInteger()).add(resNum)
			if (product > UINT64_MAX) {
				throw MoneroException("Overflow")
			}
			resNum = product
			order = order.multiply(alphabetSize.toBigInteger())
		}
		if (resSize < fullBlockSize && (BigInteger("2").pow(8 * resSize) < resNum)) {
			throw MoneroException("Overflow 2")
		}
        uint64ToUInt8Buf(resNum.toLong().toULong(), resSize).copyInto(buf, index)
		return buf
	}

	@Throws(MoneroException::class)
	fun decode(enc_: String): UByteArray{
		val enc = enc_.toByteArray().asUByteArray()
		if (enc.isEmpty()) {
			return UByteArray(0)
		}
		val fullBlockCount = Math.floor(enc.size.toDouble() / fullEncodedBlockSize).toInt()
		val lastBlockSize = enc.size % fullEncodedBlockSize
		val lastDecodedBlockSize = encodedBlockSizes.indexOf(lastBlockSize)
		if (lastDecodedBlockSize < 0) {
			throw MoneroException("Invalid encoded length")
		}
		val dataSize = fullBlockCount * fullBlockSize + lastDecodedBlockSize
		var data = UByteArray(dataSize)
        for (i in 0 until fullBlockCount) {
			data = decodeBlock(enc.copyOfRange(i * fullEncodedBlockSize, i * fullEncodedBlockSize + fullEncodedBlockSize), data, i * fullBlockSize)
		}
		if (lastBlockSize > 0) {
			data = decodeBlock(enc.copyOfRange(fullBlockCount * fullEncodedBlockSize, fullBlockCount * fullEncodedBlockSize + lastBlockSize), data, fullBlockCount * fullBlockSize)
		}
		return data
	}
}