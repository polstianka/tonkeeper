package com.tonapps.tonkeeper.ui.screen.dialog.encrypted

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonapps.tonkeeper.password.PasscodeRepository
import com.tonapps.wallet.data.account.WalletRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.block.AddrStd
import org.ton.crypto.Ed25519
import org.ton.crypto.hex
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

class EncryptedCommentViewModel(
    private val walletRepository: WalletRepository,
    private val passcodeRepository: PasscodeRepository
): ViewModel() {

    fun decrypt(
        context: Context,
        cipherText: String,
        senderAddress: String
    ) {
        Log.d("EncryptedCommentViewModel", "decrypt")
        walletRepository.activeWalletFlow.combine(passcodeRepository.confirmationFlow(context)) { wallet, _ ->
            val privateKey = walletRepository.getPrivateKey(wallet.id)
            decryptMessageComment(privateKey, wallet.publicKey, hex(cipherText), senderAddress)
            privateKey
        }.catch {
            Log.e("EncryptedCommentViewModel", "catch", it)
        }.onEach {
            Log.d("EncryptedCommentViewModel", "onEach")
        }.take(1).launchIn(viewModelScope)
    }

    private fun decryptMessageComment(
        privateKey: PrivateKeyEd25519,
        publicKey: PublicKeyEd25519,
        data: ByteArray,
        senderAddress: String
    ) {
        val address = AddrStd(senderAddress).toString(bounceable = true, urlSafe = true)
        decryptData(data, publicKey.key.toByteArray(), privateKey.key.toByteArray(), address.toByteArray())

    }

    private fun decryptData(
        data: ByteArray,
        publicKey: ByteArray,
        privateKey: ByteArray,
        salt: ByteArray
    ): ByteArray {

        val theirPublicKey = ByteArray(publicKey.size)
        for (i in publicKey.indices) {
            theirPublicKey[i] = data[i] xor publicKey[i]
        }
        val sharedSecret = Ed25519.sharedKey(theirPublicKey, privateKey)
        return decryptDataImpl(data.sliceArray(publicKey.size until data.size), sharedSecret, salt)
    }

    private fun decryptDataImpl(
        encryptedData: ByteArray,
        sharedSecret: ByteArray,
        salt: ByteArray
    ): ByteArray {
        val msgKey = encryptedData.sliceArray(0 until 16)
        val data = encryptedData.sliceArray(16 until encryptedData.size)
        val cbcStateSecret = hmacSha512(sharedSecret, msgKey)
        return byteArrayOf(0)
    }

    private fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA512")
        val keySpec = SecretKeySpec(key, "HmacSHA512")
        mac.init(keySpec)
        return mac.doFinal(data)
    }

    /*
    if (encryptedData.length < 16) throw new Error('Failed to decrypt: data is too small');
  if (encryptedData.length % 16 !== 0)
    throw new Error('Failed to decrypt: data size is not divisible by 16');
  const msgKey = encryptedData.slice(0, 16);
  const data = encryptedData.slice(16);
  const cbcStateSecret = await combineSecrets(sharedSecret, msgKey);
  const res = await doDecrypt(cbcStateSecret, msgKey, data, salt);

  return res;
     */
}