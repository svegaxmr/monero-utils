module monero.utils.core {
    exports com.svega.moneroutils.addresses;
    exports com.svega.moneroutils.transactions;
    exports com.svega.moneroutils;

    requires kotlin.stdlib;
    requires java.base;
    requires svega.common.utils;
    requires jvm.crypto;
    requires jvm.ed25519;

}