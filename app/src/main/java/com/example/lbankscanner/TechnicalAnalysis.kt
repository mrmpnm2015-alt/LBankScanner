package com.example.lbankscanner.domain

import com.example.lbankscanner.data.model.Candle

object TechnicalAnalysis {

    fun calculateEMA(candles: List<Candle>, period: Int): Double {
        if (candles.size < period) return 0.0
        val multiplier = 2.0 / (period + 1)
        var ema = candles.take(period).map { it.close }.average()
        for (i in period until candles.size) {
            ema = (candles[i].close - ema) * multiplier + ema
        }
        return ema
    }

    fun calculateRSI(candles: List<Candle>, period: Int = 14): Double {
        if (candles.size < period + 1) return 50.0
        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = candles[i].close - candles[i - 1].close
            if (change >= 0) gains += change else losses -= change
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in period + 1 until candles.size) {
            val change = candles[i].close - candles[i - 1].close
            if (change >= 0) {
                avgGain = (avgGain * 13 + change) / 14
                avgLoss = (avgLoss * 13) / 14
            } else {
                avgGain = (avgGain * 13) / 14
                avgLoss = (avgLoss * 13 - change) / 14
            }
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }
}
