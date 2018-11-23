package com.svega.moneroutils.addresses

import com.svega.crypto.ed25519.*
import com.svega.crypto.ed25519.objects.ge_cached
import com.svega.crypto.ed25519.objects.ge_p1p1
import com.svega.crypto.ed25519.objects.ge_p2
import com.svega.crypto.ed25519.objects.ge_p3
import com.svega.moneroutils.AddressType
import com.svega.moneroutils.NetType
import com.svega.moneroutils.SWAP32
import com.svega.moneroutils.crypto.Hashing
import com.svega.moneroutils.toByteArray

class MainAddress: MoneroAddress {
    constructor(address: String, net: NetType): super(address, net)
    constructor(key: FullKey, net: NetType, addrType: AddressType): super(key, net, addrType)
    constructor(seed: ByteArray? = null, secretSpendKey: SecretKey? = null, net: NetType):
            super(seed, secretSpendKey, net, AddressType.MAIN)
    override val LENGTH = 95
    override val BYTES = 69
    init{
        validate()
    }

    private val subAddresses = HashMap<Pair<Int, Int>, SubAddress>()
    fun genSubaddress(account: Int, idx: Int): MoneroAddress{
        val index = Pair(account, idx)
        if(subAddresses.containsKey(index))
            return subAddresses[index]!!

        if((index.first == 0) and (index.second == 0))
            return this

        val D = getSubaddressSpendPublicKey(this, index)

        val C = scalarmultKey(D, this.key.view.secret!!)

        println("C data: ${C.str}")

        val sAddr = SubAddress(FullKey(KeyPair(D, null), KeyPair(C, null)), net)

        subAddresses[index] = sAddr

        return sAddr
    }

    private fun getSubaddressSpendPublicKey(mainAddress: MainAddress, index: Pair<Int, Int>): PublicKey {
        if((index.first == 0) and (index.second == 0))
            return mainAddress.key.spend.public

        val m = subAddressSecretKey(mainAddress.key.view.secret!!, index)

        val M = m.getPublic()

        return addKeys(mainAddress.key.spend.public, M)
    }

    private fun addKeys(A: Key, B: Key): Key {
        val B2 = ge_p3()
        val A2 = ge_p3()
        assert(ge_frombytes_vartime(B2, B.data) == 0)
        assert(ge_frombytes_vartime(A2, A.data) == 0)
        val tmp2 = ge_cached()
        ge_p3_to_cached(tmp2, B2)
        val tmp3 = ge_p1p1()
        ge_add(tmp3, A2, tmp2)
        ge_p1p1_to_p3(A2, tmp3)
        val ba = ByteArray(32)
        ge_p3_tobytes(ba, A2)
        return Key(ba)
    }

    private fun scalarmultKey(P: Key, a: Key): Key {
        val A = ge_p3()
        val R = ge_p2()
        assert(ge_frombytes_vartime(A, P.data) == 0)
        ge_scalarmult(R, a.data, A)
        val aP = ByteArray(32)
        ge_tobytes(aP, R)
        return Key(aP)
    }

    private fun subAddressSecretKey(secret: SecretKey, index: Pair<Int, Int>): SecretKey {
        var data = "SubAddr".toByteArray()+0.toByte()
        data += secret.data
        data += SWAP32(index.first).toByteArray()
        data += SWAP32(index.second).toByteArray()
        val s = Hashing.hashToScalar(data)
        return SecretKey(s)
    }
}