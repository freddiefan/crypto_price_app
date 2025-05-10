package org.example.kmpsamples.presentation.cryptocurrencies

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import kmpsamples.composeapp.generated.resources.Res
import kmpsamples.composeapp.generated.resources.button_start
import kmpsamples.composeapp.generated.resources.statistic

import org.example.kmpsamples.presentation.cryptocurrencies.viewModel.CryptocurrencyViewModel
import org.example.kmpsamples.presentation.cryptocurrencies.viewModel.dtos.KLineDataUIState
import org.example.kmpsamples.presentation.cryptocurrencies.views.CryptocurrencyList
import org.example.kmpsamples.presentation.cryptocurrencies.views.KlineListItem
import org.example.kmpsamples.presentation.cryptocurrencies.views.KlineStatisticListItem
import org.example.kmpsamples.presentation.fillMaxSizeModifier
import org.example.kmpsamples.presentation.fillMaxWidthModifier
import org.example.kmpsamples.presentation.getWindowSizeClass
import org.jetbrains.compose.resources.stringResource

import androidx.compose.ui.text.style.TextAlign
import kotlin.math.roundToInt

@Composable
fun CryptocurrencyScreen(
    modifier: Modifier = Modifier,
    uiState: State<CryptocurrencyViewModel.UIState>,
    onStartCollecting: () -> Unit
) {
    val windowSizeClass = getWindowSizeClass()
    val wideEnough      = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    Column(
        modifier  = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Button(onClick = onStartCollecting) {
            Text(stringResource(Res.string.button_start))
        }

        Spacer(Modifier.height(20.dp))

        /* ----------  TABLE(S) + CHART(S)  ---------- */
        Row(fillMaxSizeModifier.weight(1f)) {

            // how many columns – each symbol gets one, plus statistic column if wide
            val columns = uiState.value.cryptoCurrencies.size + if (wideEnough) 1 else 0
            val colMod  = fillMaxWidthModifier.weight(1f / columns).fillMaxHeight()

            uiState.value.cryptoCurrencies.forEach { listItemUIState ->

                Column(colMod) {

                    /* ➊ live K‑line table */
                    CryptocurrencyList(
                        modifier = Modifier.weight(1f),
                        uiState  = listItemUIState.uiData,
                        title    = listItemUIState.symbol
                    ) { rowItem ->
                        KlineListItem(
                            modifier = fillMaxWidthModifier.wrapContentHeight(),
                            klineData = rowItem
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    /* ➋ lightweight dual‑series chart (high & low) */
                    val points = (listItemUIState.uiData as? org.example.kmpsamples.shared.ResultState.Success)
                        ?.value.orEmpty()

                    PriceChart(
                        data   = points,
                        title  = listItemUIState.symbol.uppercase(),
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            /* optional statistic column */
            if (wideEnough) {
                statisticList(colMod, uiState)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!wideEnough) {
            statisticList(Modifier.wrapContentHeight(), uiState)
        }
    }
}

@Composable
private fun statisticList(modifier: Modifier, uiState: State<CryptocurrencyViewModel.UIState>) {
    CryptocurrencyList(
        modifier,
        uiState.value.statistic,
        stringResource(Res.string.statistic).toUpperCase(Locale.current)
    ) { listItem ->
        KlineStatisticListItem(
            Modifier.fillMaxWidth(0.5f).wrapContentHeight(),
            listItem
        )
    }
}

@Composable
private fun PriceChart(
    modifier: Modifier = Modifier,
    data: List<KLineDataUIState>,
    title: String,
    lineColorHigh: Color = Color(0xFF2E7D32),   // green-700
    lineColorLow:  Color = Color(0xFFD32F2F),   // red-700
    backgroundColor: Color = Color(0xFFF5F5F5)  // light gray
) {
    if (data.isEmpty()) return

    /* ── pre-compute ranges ───────────────────────────────────────────── */
    val maxPrice   = data.maxOf { it.highPrice }
    val minPrice   = data.minOf { it.lowPrice }
    val priceRange = (maxPrice - minPrice).takeIf { it != 0f } ?: 1f
    val stepX      = 1f / (data.lastIndex.coerceAtLeast(1))
    fun xAt(idx: Int, w: Float) = w * (1 - stepX * idx)          // newest → right

    /* ── pick four x-axis ticks ───────────────────────────────────────── */
    val tickCount   = 4
    val tickStep    = (data.lastIndex / (tickCount - 1)).coerceAtLeast(1)
    val tickIndices = (0..data.lastIndex step tickStep).toList()

    /* ── latest candle (index 0 is newest, see xAt()) ─────────────────── */
    val latest = data[0]

    /* ─────────────────────────── UI ──────────────────────────────────── */
    Column(modifier.background(backgroundColor)) {

        /* ① title */
        Text(
            text      = title,
            style     = MaterialTheme.typography.labelLarge,
            modifier  = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp, bottom = 2.dp)
        )

        /* ② chart + right-side price labels */
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            /* canvas with price lines */
            Canvas(Modifier.fillMaxSize()) {
                /* high line */
                val highPath = Path().apply {
                    data.forEachIndexed { idx, p ->
                        val x = xAt(idx, size.width)
                        val y = size.height * (1 - (p.highPrice - minPrice) / priceRange)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path  = highPath,
                    color = lineColorHigh,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                /* low line */
                val lowPath = Path().apply {
                    data.forEachIndexed { idx, p ->
                        val x = xAt(idx, size.width)
                        val y = size.height * (1 - (p.lowPrice  - minPrice) / priceRange)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path  = lowPath,
                    color = lineColorLow,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            /* right-hand latest-price labels */
            Column(
                Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(                         // latest HIGH
                    text      = latest.highPrice.pretty(),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = lineColorHigh,
                    textAlign = TextAlign.End
                )
                Text(                         // latest LOW
                    text      = latest.lowPrice.pretty(),
                    style     = MaterialTheme.typography.labelSmall,
                    color     = lineColorLow,
                    textAlign = TextAlign.End
                )
            }
        }

        /* ③ x-axis tick labels (newest on right) */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            tickIndices.reversed().forEach { idx ->
                val label = data[idx].localDateTime
                    .substringBefore('.')    // strip milliseconds
                    .takeLast(8)             // HH:mm:ss
                Text(
                    text      = label,
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f)
                )
            }
        }
    }
}

/** round to 2 decimal places and turn into a String – works on every target */
private fun Float.pretty(): String =
    ((this * 100).roundToInt() / 100f).toString()