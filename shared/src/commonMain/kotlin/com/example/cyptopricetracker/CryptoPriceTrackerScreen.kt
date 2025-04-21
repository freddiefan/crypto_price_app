package com.example.cyptopricetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CryptoPriceTrackerScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crypto Price Tracker", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("BTC: $50,000", fontSize = 18.sp)
        Text("ETH: $3,000", fontSize = 18.sp)
    }
}