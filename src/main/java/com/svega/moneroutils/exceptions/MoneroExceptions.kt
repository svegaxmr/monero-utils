package com.svega.moneroutils.exceptions

open class MoneroException : Exception {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

open class AddressException : MoneroException {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

class InvalidChecksumException : AddressException {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

open class Base58Exception : MoneroException {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

class Base58EncodeException : Base58Exception {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

class Base58DecodeException : Base58Exception {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}

class HashingException : MoneroException {
    constructor() : super()
    constructor(err: String) : super(err)
    constructor(e: Exception) : super(e)
    constructor(err: String, e: Exception) : super(err, e)
}