/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.tonapi.apis

import io.kotlintest.shouldBe
import io.kotlintest.specs.ShouldSpec

import io.tonapi.apis.WalletApi
import io.tonapi.models.Accounts
import io.tonapi.models.GetWalletBackup200Response
import io.tonapi.models.Seqno
import io.tonapi.models.StatusDefaultResponse
import io.tonapi.models.TonConnectProof200Response
import io.tonapi.models.TonConnectProofRequest

class WalletApiTest : ShouldSpec() {
    init {
        // uncomment below to create an instance of WalletApi
        //val apiInstance = WalletApi()

        // to test getAccountSeqno
        should("test getAccountSeqno") {
            // uncomment below to test getAccountSeqno
            //val accountId : kotlin.String = 0:97264395BD65A255A429B11326C84128B7D70FFED7949ABAE3036D506BA38621 // kotlin.String | account ID
            //val result : Seqno = apiInstance.getAccountSeqno(accountId)
            //result shouldBe ("TODO")
        }

        // to test getWalletBackup
        should("test getWalletBackup") {
            // uncomment below to test getWalletBackup
            //val xTonConnectAuth : kotlin.String = xTonConnectAuth_example // kotlin.String | 
            //val result : GetWalletBackup200Response = apiInstance.getWalletBackup(xTonConnectAuth)
            //result shouldBe ("TODO")
        }

        // to test getWalletsByPublicKey
        should("test getWalletsByPublicKey") {
            // uncomment below to test getWalletsByPublicKey
            //val publicKey : kotlin.String = NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2ODQ3... // kotlin.String | 
            //val result : Accounts = apiInstance.getWalletsByPublicKey(publicKey)
            //result shouldBe ("TODO")
        }

        // to test setWalletBackup
        should("test setWalletBackup") {
            // uncomment below to test setWalletBackup
            //val xTonConnectAuth : kotlin.String = xTonConnectAuth_example // kotlin.String | 
            //val body : java.io.File = BINARY_DATA_HERE // java.io.File | Information for saving backup
            //apiInstance.setWalletBackup(xTonConnectAuth, body)
        }

        // to test tonConnectProof
        should("test tonConnectProof") {
            // uncomment below to test tonConnectProof
            //val tonConnectProofRequest : TonConnectProofRequest =  // TonConnectProofRequest | Data that is expected from TON Connect
            //val result : TonConnectProof200Response = apiInstance.tonConnectProof(tonConnectProofRequest)
            //result shouldBe ("TODO")
        }

    }
}