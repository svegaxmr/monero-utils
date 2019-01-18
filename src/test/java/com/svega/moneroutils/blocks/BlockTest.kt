package com.svega.moneroutils.blocks

import com.svega.moneroutils.BinHexUtils
import com.svega.moneroutils.NetType
import org.junit.Test
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class BlockTest {
    @Test
    fun `Parse Genesis Block`() {
        val b = "010000000000000000000000000000000000000000000000000000000000000000000010270000013c01ff0001ffffffffffff03029b2e4c0281c0b02e7c53291a94d1d0cbff8883f8024f5142ee494ffbbd08807121017767aafcde9be00dcfd098715ebcf7f410daebc582fda69d24a28e9d0bc890d100"
        val a = Block.parseBlobHeader(BinHexUtils.hexToByteArray(b))
        assertTrue { a.hash.contentEquals(BinHexUtils.hexToByteArray("418015bb9ae982a1975da7d79277c2705727a56894ba0fb246adaabb1f4632e3")) }
        assertTrue { a.txids.size == 0 }
        assertTrue { a.blockHeader.nonce == NetType.MAINNET.genesisNonce }
    }

    @Test
    fun `Parse block V1 with TX`() {
        val b = "0100d5adc49a053b8818b2b6023cd2d532c6774e164a8fcacd603651cb3ea0cb7f9340b28ec016b4bc4ca301aa0101ff6e08acbb2702eab03067870349139bee7eab2ca2e030a6bb73d4f68ab6a3b6ca937214054cdac0843d028bbe23b57ea9bae53f12da93bb57bf8a2e40598d9fccd10c2921576e987d93cd80b4891302468738e391f07c4f2b356f7957160968e0bfef6e907c3cee2d8c23cbf04b089680c6868f01025a0f41f063e195a966051e3a29e17130a9ce97d48f55285b9bb04bdd55a09ae78088aca3cf0202d0f26169290450fe17e08974789c3458910b4db18361cdc564f8f2d0bdd2cf568090cad2c60e02d6f3483ec45505cc3be841046c7a12bf953ac973939bc7b727e54258e1881d4d80e08d84ddcb0102dae6dfb16d3e28aaaf43e00170b90606b36f35f38f8a3dceb5ee18199dd8f17c80c0caf384a30202385d7e57a4daba4cdd9e550a92dcc188838386e7581f13f09de796cbed4716a42101c052492a077abf41996b50c1b2e67fd7288bcd8c55cdc657b4e22d0804371f6901beb76a82ea17400cd6d7f595f70e1667d2018ed8f5a78d1ce07484222618c3cd"
        val a = Block.parseBlobHeader(BinHexUtils.hexToByteArray(b))
        assertTrue { a.hash.contentEquals(BinHexUtils.hexToByteArray("7d3113f562eac36f14afa08c22ae20bbbf8cffa31a4466d24850732cb96f80e9")) }
        assertTrue { a.txids.size == 1 }
        assertTrue { a.blockHeader.nonce == 2739715252 }
    }

    @Test
    fun `Parse block V2 with TX`() {
        val b = "02039fd0dfbc053beaf2f218036cf267c38178ec3b8e47c6fb8592fb828f4a21445e368017947cbea10200019c924301ffe09143068088debe0102aa5b3c259f430922c96952e258e92a2e70c76f6705f2ad0b848117ddc31cfd5980b4c4c321021669f2dfff6e099e0f6dc6787828587ffe2593c0cf83a1bee4dc159b671f6e3f80a0be81950102f99f36f6cce2fc11524328e3842bbbe58ebad367303f0db508afdc08e583ac9b80f092cbdd08023756756c5febdbc7d4df65380fc6524f4e0e0aec76d7921d20e08de877f68b9880a094a58d1d0213bead9062cd0ec2c1cbc0555a931e2f87a7625434f455cd9ca92e4916a4334680c0caf384a302026e81254b79befc18191c46610d95481f71a06a41c4e8c1521a5636964f5ed40d2b01a33e711f7b5a771b943996976da2276055083624f855abf811438d3d1d9c230e02087b165c000000000001908c909fdba8923e840457b136372f6381ec88ab75d006fe40f168b3c580c387"
        val a = Block.parseBlobHeader(BinHexUtils.hexToByteArray(b))
        assertTrue { a.hash.contentEquals(BinHexUtils.hexToByteArray("3fd720c5c8b3072fc1ccda922dec1ef25f9ed88a1e6ad4103d0fe00b180a5903")) }
        assertTrue { a.txids.size == 1 }
    }

    @Test
    fun `Parse block V3 with Multi TX`() {
        val b = "0305c89cbcc205fa7d13a90850882060479d100141ff84286599ae39c3277c8ea784393f882d1f3c02004001bd9f4901ff819f49058090bcfd0202d6d6585e194d1fd77b4703df35c2eec7f306a5196237e0a550e6b8ba69cb15d080a0d9e61d02812f8a552edfbf9549e49947c7f54c79b7ca360bb17f3fd2b01223f04d1edb5280b09dc2df0102d5ceb0be1531a6bee1e992bdc4eb7efb13054855ff21b5f12b2a5b89bc2d154580c0ee8ed20b029176f0ee9b85b9ecb31e61fdae953b22b15f4bcbf74410319e1fe5f3d30ab66080a0b6cef7850202c560cc43428bc866e80bca00f714237784718c8d91093a5ec9516508b2be87ce2b0175118ac5331a15d4b6cf5726a981fbaaa039b20d7e6755148e686252c68abd5b020800000000d82159f70584ae0c3c70ec65b11dc174f340341a1cfe2e9f2af07beeb87ded9f1e5a977b2c58d36d54a8b2ece02dca3c90e8fd64a3a8306c2c696cb55bc03b9eeed6e69883cd94f25378743b3dee1e264a9bdb133229dae6ce3de5dfd4ada277e9cd8f84b9e30efad1b130c28c38e954febca89202a63a28e1f015bcbc297349cba730e4066138d71ad694d80c24742694439ecdebbfee3827e849f05be4ea6c06da42eeab"
        val a = Block.parseBlobHeader(BinHexUtils.hexToByteArray(b))
        assertTrue("Expected 9df518e013515cb091a21f568a3152b6126b4e4e9d64380d36ccbcd920a72fc9, got ${BinHexUtils.binaryToHex(a.hash)}") { a.hash.contentEquals(BinHexUtils.hexToByteArray("9df518e013515cb091a21f568a3152b6126b4e4e9d64380d36ccbcd920a72fc9")) }
        assertTrue { a.txids.size == 5 }
    }

    @Test
    fun `Parse Block V5_5`() {
        val b = "0505DDFCA0C605A5C59ED75B0F5D469118361BE8D1821801917D02431E6113E682EBD20C7E3AC8A604800001C0F73401FF84F73401B4AEEED2B0E302023E9CC55421060C4D3EDC7FB8B5B136F370E1BCC1E0BB5C913999F2535BF7157B7101C8C8858082D470CEC69C773A063D152091503FA7B51447BC49CEB040BC9AF940022B000000000194E9FB0000000000000000000000000000000000000000000000000000000000000000000000032101322DD1E32BAD55739720DAAB3EB8ECB69B0B07FA576CE7F9869FAA575413568E00"
        val a = Block.parseBlobHeader(BinHexUtils.hexToByteArray(b))
        val hr = a.getPOWHash()
        assertTrue { hr.diff == 21251L }
    }
}