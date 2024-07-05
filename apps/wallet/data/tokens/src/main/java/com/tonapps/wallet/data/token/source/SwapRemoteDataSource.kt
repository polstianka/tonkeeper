import android.util.Log
import com.tonapps.wallet.api.API
import io.tonapi.models.Asset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

@Deprecated("")
internal class SwapRemoteDataSource(
    private val api: API
) {

    suspend fun load(
        testnet: Boolean
    ): List<Asset> = withContext(Dispatchers.IO) {
        val assetListDeferred = async {
            api.getAssets(testnet)
        }

        val pairListDeferred = async {
            api.getMarketPairs(testnet)
        }

        val assetList = assetListDeferred.await()
        val pairList = pairListDeferred.await()

        val assetMap: MutableMap<String, Asset> =
            assetList.associateBy { it.contractAddress }.toMutableMap()



        Log.d("asset-get", "SwapRemoteDataSource load: ${assetList}")

        assetList
    }

}