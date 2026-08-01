package com.example.lbankscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lbankscanner.data.api.RetrofitClient
import com.example.lbankscanner.data.local.AppDatabase
import com.example.lbankscanner.data.model.CoinAnalysisResult
import com.example.lbankscanner.data.model.FullScanSummary
import com.example.lbankscanner.data.repository.ScannerRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val repository = ScannerRepository(RetrofitClient.apiService, db.scanHistoryDao())

        setContent {
            MaterialTheme {
                MainScannerScreen(repository)
            }
        }
    }
}

@Composable
fun MainScannerScreen(repository: ScannerRepository) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("Ready to Scan") }
    var summary by remember { mutableStateOf<FullScanSummary?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF12141C))
            .padding(16.dp)
    ) {
        Text("LBank Futures Scanner", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isScanning = true
                scope.launch {
                    summary = repository.scanMarket { scanned, total ->
                        progressText = "Scanning $scanned / $total ..."
                    }
                    isScanning = false
                    progressText = "Scan Completed!"
                }
            },
            enabled = !isScanning,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2962FF))
        ) {
            Text(if (isScanning) progressText else "Start 15m/1h Scan", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        summary?.let { sum ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("READY: ${sum.readyList.size}", color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                Text("WATCHLIST: ${sum.watchlistCount}", color = Color(0xFFFFAB00), fontWeight = FontWeight.Bold)
                Text("REJECTED: ${sum.rejectedCount}", color = Color(0xFFFF3D00), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(sum.readyList) { coin ->
                    CoinCard(coin)
                }
            }
        }
    }
}

@Composable
fun CoinCard(coin: CoinAnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(coin.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(coin.reason, color = Color.Gray, fontSize = 12.sp)
            }
            Text("Score: ${coin.score}", color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
        }
    }
}
