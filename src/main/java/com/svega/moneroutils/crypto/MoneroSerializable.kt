package com.svega.moneroutils.crypto

interface MoneroSerializable {
    fun toBlob(): ByteArray
}