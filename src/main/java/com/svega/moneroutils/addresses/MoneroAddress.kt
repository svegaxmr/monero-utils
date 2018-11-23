package com.svega.moneroutils.addresses

import com.svega.moneroutils.*
import com.svega.common.math.*
import com.svega.crypto.common.CryptoOps
import com.svega.crypto.common.algos.Keccak
import com.svega.crypto.common.algos.Parameter
import java.io.Serializable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


abstract class MoneroAddress: Serializable {
    var bytes: Array<UInt8>
        protected set
    var address: String
        protected set
    var key: FullKey
        private set
    var net: NetType
        private set
    var seed: ByteArray?
        private set
    abstract val BYTES: Int
    abstract val LENGTH: Int
    protected constructor(address: String, net: NetType){
        this.net = net
        this.address = address
        seed = null
        bytes = Base58.decode(address)
        val spend = KeyPair(Key(bytes.sliceArray(IntRange(1, 32)).asByteArray()), null)
        val view = KeyPair(Key(bytes.sliceArray(IntRange(33, 64)).asByteArray()), null)
        key = FullKey(spend, view)
    }

    protected constructor(seed: ByteArray? = null, secretSpendKey: SecretKey? = null, net: NetType, addrType: AddressType){
        if((seed == null) and (secretSpendKey == null))
            throw MoneroException("seed and secret spend key cannot be null!")
        this.net = net
        this.seed = seed
        val spend: KeyPair = when(secretSpendKey == null){
            true -> generateKeys(seed!!)
            false -> KeyPair.genFromSecret(secretSpendKey.data)
        }
        val second = Keccak.getHash(spend.secret!!.data, Parameter.KECCAK_256) //keccak256 of view gets spend
        val view = generateKeys(second) //spend is got from generatekeys
        key = FullKey(spend, view)
        address = key.getAddressString(addrType, net)
        bytes = key.getAddressBytes(addrType, net).asUInt8Array()
    }

    protected constructor(key: FullKey, net: NetType, addrType: AddressType){
        this.key = key
        this.net = net
        seed = null
        address = key.getAddressString(addrType, net)
        bytes = key.getAddressBytes(addrType, net).asUInt8Array()
    }

    open fun validate(){
        if(address.length != LENGTH)
            throw MoneroException("Address $address is not $LENGTH characters long!")
        if(bytes.size != BYTES)
            throw MoneroException("Address $address is not $BYTES bytes long!")
        validateChecksum(bytes, address)
    }

    @Throws(IOException::class)
    private fun writeObject(stream: ObjectOutputStream) {
        stream.writeInt(SERIALIZABLE_VERSION)
        stream.writeShort(bytes.size)
        stream.write(bytes.asByteArray())
        stream.writeUTF(address)
        stream.writeObject(key)
        stream.writeObject(net)
        if(seed == null){
            stream.writeBoolean(false)
        }else{
            stream.writeBoolean(true)
            stream.writeShort(seed!!.size)
            stream.write(seed)
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(stream: ObjectInputStream){
        stream.readInt() //version
        var read = stream.readShort()
        val temp = ByteArray(read.toInt())
        stream.readFully(temp)
        bytes = temp.asUInt8Array()
        address = stream.readUTF()
        key = stream.readObject() as FullKey
        net = stream.readObject() as NetType
        if(stream.readBoolean()){
            read = stream.readShort()
            seed = ByteArray(read.toInt())
            stream.readFully(seed)
        }

    }

    companion object {
        const val SERIALIZABLE_VERSION = 0
        @Throws(MoneroException::class)
        fun stringToAddress(address: String) : MoneroAddress {
            val arr = Base58.decode(address)
            validateChecksum(arr, address)
            return when(arr[0]){
                NetType.MAINNET.MAINADDR -> MainAddress(address, NetType.MAINNET)
                NetType.MAINNET.INTEGRATED -> IntegratedAddress(address, NetType.MAINNET)
                NetType.MAINNET.SUBADDR -> SubAddress(address, NetType.MAINNET)
                NetType.TESTNET.MAINADDR -> MainAddress(address, NetType.TESTNET)
                NetType.TESTNET.INTEGRATED -> IntegratedAddress(address, NetType.TESTNET)
                NetType.TESTNET.SUBADDR -> SubAddress(address, NetType.TESTNET)
                NetType.STAGENET.MAINADDR -> MainAddress(address, NetType.STAGENET)
                NetType.STAGENET.INTEGRATED -> IntegratedAddress(address, NetType.STAGENET)
                NetType.STAGENET.SUBADDR -> SubAddress(address, NetType.STAGENET)
                else -> throw MoneroException("Address prefix ${arr[0]} is not a valid prefix")
            }
        }
        @Throws(MoneroException::class)
        fun validateChecksum(bytes: Array<UInt8>, address: String) {
            val checksum = Keccak.addressChecksum(bytes)
            if (!checksum.contentEquals(bytes.sliceArray(IntRange(bytes.size - 4, bytes.size - 1)))) {
                println(BinHexUtils.binaryToHex(checksum))
                println(BinHexUtils.binaryToHex(bytes.sliceArray(IntRange(bytes.size - 4, bytes.size - 1))))
                throw MoneroException("Invalid address $address fails checksum")
            }
        }
        fun generateKeys(seed: ByteArray): KeyPair {
            if (seed.size != 32)
                throw MoneroException("Invalid input length!")
            return KeyPair.genFromSecret(CryptoOps.sc_reduce32(seed))
        }
    }
}