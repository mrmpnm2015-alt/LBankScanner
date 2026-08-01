package com.example.lbankscanner.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

enum class SignalStatus { READY, WATCHLIST, REJECT }

data class CoinAnalysisResult(
    val symbol: String,
    val status: SignalStatus,
    val score: Int,
    val rsi14: Double,
    val macdHistogram: Double,
    val trend1h: String,
    val reason: String
)

data class FullScanSummary(
    val timestamp: Long = System.currentTimeMillis(),
    val totalScanned: Int,
    val readyList: List<CoinAnalysisResult>,
    val watchlistCount: Int,
    val rejectedCount: Int
)

class CandleDeserializer : JsonDeserializer<List<Candle>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<Candle> {
        val candles = mutableListOf<Candle>()
        if (json.isJsonArray) {
            val array = json.asJsonArray
            for (element in array) {
                if (element.isJsonArray) {
                    val item = element.asJsonArray
                    candles.add(
                        Candle(
                            timestamp = item[0].asLong,
                            open = item[1].asDouble,
                            high = item[2].asDouble,
                            low = item[3].asDouble,
                            close = item[4].asDouble,
                            volume = item[5].asDouble
                        )
                    )
                }
            }
        }
        return candles
    }
}
