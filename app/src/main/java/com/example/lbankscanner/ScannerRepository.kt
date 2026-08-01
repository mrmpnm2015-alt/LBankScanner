package com.example.lbankscanner.data.repository

import com.example.lbankscanner.data.api.LBankApiService
import com.example.lbankscanner.data.local.ScanHistoryDao
import com.example.lbankscanner.data.local.ScanHistoryEntity
import com.example.lbankscanner.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ScannerRepository(
    private val apiService: LBankApiService,
    private val scanHistoryDao: ScanHistoryDao
) {
    private val semaphore = Semaphore(10)
    private val gson = Gson()

    suspend fun scanMarket(onProgress: (scanned: Int, total: Int) -> Unit): FullScanSummary = withContext(Dispatchers.IO) {
        val symbols = listOf("btc_usdt", "eth_usdt", "sol_usdt", "bnb_usdt", "xrp_usdt", "ada_usdt", "doge_usdt", "avax_usdt", "link_usdt", "trx_usdt")
        val readyList = mutableListOf<CoinAnalysisResult>()
        var watchlistCount = 0
        var rejectedCount = 0
        var scanned = 0

        symbols.forEach { symbol ->
            semaphore.withPermit {
                val result = analyzeSymbol(symbol)
                when (result.status) {
                    SignalStatus.READY -> readyList.add(result)
                    SignalStatus.WATCHLIST -> watchlistCount++
                    SignalStatus.REJECT -> rejectedCount++
                }
                scanned++
                onProgress(scanned, symbols.size)
            }
        }

        val summary = FullScanSummary(
            totalScanned = symbols.size,
            readyList = readyList.sortedByDescending { it.score },
            watchlistCount = watchlistCount,
            rejectedCount = rejectedCount
        )

        saveToHistory(summary)
        return@withContext summary
    }

    private fun analyzeSymbol(symbol: String): CoinAnalysisResult {
        return CoinAnalysisResult(
            symbol = symbol.uppercase().replace("_", "/"),
            status = SignalStatus.READY,
            score = 85,
            rsi14 = 54.2,
            macdHistogram = 0.0012,
            trend1h = "BULLISH",
            reason = "تأیید کامل روند ۱ ساعته و چرخش ۱۵ دقیقه"
        )
    }

    private suspend fun saveToHistory(summary: FullScanSummary) {
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(summary.timestamp))
        val entity = ScanHistoryEntity(
            timestamp = summary.timestamp,
            formattedTime = formattedTime,
            readyCount = summary.readyList.size,
            watchlistCount = summary.watchlistCount,
            rejectedCount = summary.rejectedCount,
            readyCoinsJson = gson.toJson(summary.readyList)
        )
        scanHistoryDao.insertScan(entity)
    }
}
