import com.squareup.moshi.*
import io.tonapi.models.MarketPair

class MarketPairAdapter : JsonAdapter<MarketPair>() {

    @FromJson
    override fun fromJson(reader: JsonReader): MarketPair? {

        reader.beginArray()

        val first = if (reader.hasNext()) reader.nextString() else throw JsonDataException("Expected first item in array")

        val second = if (reader.hasNext()) reader.nextString() else throw JsonDataException("Expected second item in array")

        reader.endArray()

        return MarketPair(first, second)
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: MarketPair?) {

        writer.beginArray()

        writer.value(value?.first)

        writer.value(value?.second)

        writer.endArray()
    }
}
