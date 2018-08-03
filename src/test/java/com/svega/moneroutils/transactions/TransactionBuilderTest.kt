package com.svega.moneroutils.transactions

import com.svega.moneroutils.XMRAmount
import com.svega.moneroutils.addresses.MoneroAddress
import org.junit.Test

class TransactionBuilderTest{
    @Test(expected = ZeroInputException::class)
    fun `Build Multi Net TX`(){
        val b = TransactionBuilder()
        val mainnetAddr = MoneroAddress.stringToAddress("4BDLzJ4Mj811RVm3JagmGiK46cwJrcaWrLjFNST4tzE8YrcK8q5Z2B2QG1SRimoRuvBMECbjFaU6z7rWy3CbfoEDMU5AMjM")
        val testnetAddr = MoneroAddress.stringToAddress("9zLj7p4ptRXaePyZnUTcBJYsWqeo372Pb9tRCGwq1dxDXBBCouuiZpBDPMRExtzniAagHGVK2UiKgbzrC8RudGMdPK2x8vn")
        val out1 = TransactionOutput(mainnetAddr, XMRAmount(5000))
        val out2 = TransactionOutput(testnetAddr, XMRAmount(5000))
        b.outputs.add(out1)
        b.outputs.add(out2)
        b.build()
    }

    @Test(expected = ZeroInputException::class)
    fun `No Input TX`(){
        val b = TransactionBuilder()
        val mainnetAddr = MoneroAddress.stringToAddress("4BDLzJ4Mj811RVm3JagmGiK46cwJrcaWrLjFNST4tzE8YrcK8q5Z2B2QG1SRimoRuvBMECbjFaU6z7rWy3CbfoEDMU5AMjM")
        val out1 = TransactionOutput(mainnetAddr, XMRAmount(5000))
        b.outputs.add(out1)
        b.build()
    }

    @Test(expected = ZeroOutputException::class)
    fun `No Output TX`(){
        val b = TransactionBuilder()
        b.build()
    }
}

