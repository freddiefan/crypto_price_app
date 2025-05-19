package org.example.kmpsamples.presentation.cryptocurrencies

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kmpsamples.composeapp.generated.resources.Res
import kmpsamples.composeapp.generated.resources.statistic
import org.example.kmpsamples.presentation.cryptocurrencies.viewModel.CryptocurrencyViewModel
import org.example.kmpsamples.presentation.cryptocurrencies.viewModel.dtos.KLineDataUIState
import org.example.kmpsamples.presentation.cryptocurrencies.views.CryptocurrencyList
import org.example.kmpsamples.presentation.cryptocurrencies.views.KlineListItem
import org.example.kmpsamples.presentation.cryptocurrencies.views.KlineStatisticListItem
import org.example.kmpsamples.presentation.fillMaxSizeModifier
import org.example.kmpsamples.presentation.fillMaxWidthModifier
import org.jetbrains.compose.resources.stringResource

@Composable
fun CryptocurrencyScreen(
    modifier: Modifier = Modifier,
    uiState: State<CryptocurrencyViewModel.UIState>,
    onStartCollecting: () -> Unit
) {
    LaunchedEffect(Unit) {
        onStartCollecting()
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Row(
            fillMaxSizeModifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val displayedCryptos = uiState.value.cryptoCurrencies.take(5)
            val colMod = fillMaxWidthModifier.weight(1f / displayedCryptos.size).fillMaxHeight()

            displayedCryptos.forEach { listItemUIState ->
                val trimmedPoints =
                    (listItemUIState.uiData as? org.example.kmpsamples.shared.ResultState.Success)
                        ?.value?.take(2).orEmpty()
                val trimmedPointsChart =
                    (listItemUIState.uiData as? org.example.kmpsamples.shared.ResultState.Success)
                        ?.value?.take(5).orEmpty()

                Card(
                    modifier = colMod
                        .padding(8.dp)
                        .shadow(4.dp, shape = MaterialTheme.shapes.medium),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            text = listItemUIState.symbol.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(Modifier.height(4.dp))

                        CryptocurrencyList(
                            modifier = Modifier.wrapContentHeight(),
                            uiState = org.example.kmpsamples.shared.ResultState.Success(
                                trimmedPoints
                            ),
                            title = ""
                        ) { rowItem ->
                            KlineListItem(
                                modifier = fillMaxWidthModifier.wrapContentHeight(),
                                klineData = rowItem
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        PriceChart(
                            data = trimmedPointsChart,
                            title = "",
                            modifier = Modifier
                                .height(150.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        statisticList(Modifier.wrapContentHeight(), uiState)
    }
}

@Composable
private fun statisticList(modifier: Modifier, uiState: State<CryptocurrencyViewModel.UIState>) {
    CryptocurrencyList(
        modifier,
        uiState.value.statistic,
        stringResource(Res.string.statistic).uppercase()
    ) { listItem ->
        KlineStatisticListItem(
            Modifier
                .fillMaxWidth(0.5f)
                .wrapContentHeight(),
            listItem
        )
    }
}

@Composable
private fun PriceChart(
    modifier: Modifier = Modifier,
    data: List<KLineDataUIState>,
    title: String,
    lineColorHigh: Color = Color(0xFF2E7D32),
    lineColorLow: Color = Color(0xFFD32F2F),
    backgroundColor: Color = Color(0xFFF5F5F5)
) {
    if (data.isEmpty()) return

    val maxPrice = data.maxOf { it.highPrice }
    val minPrice = data.minOf { it.lowPrice }
    val priceRange = (maxPrice - minPrice).takeIf { it != 0f } ?: 1f
    val stepX = 1f / (data.lastIndex.coerceAtLeast(1))
    fun xAt(idx: Int, w: Float) = w * (1 - stepX * idx)

    val latest = data[0]

    Column(modifier.background(backgroundColor).padding(8.dp)) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val highPath = Path().apply {
                    data.forEachIndexed { idx, p ->
                        val x = xAt(idx, size.width)
                        val y = size.height * (1 - (p.highPrice - minPrice) / priceRange)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = highPath,
                    color = lineColorHigh,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val lowPath = Path().apply {
                    data.forEachIndexed { idx, p ->
                        val x = xAt(idx, size.width)
                        val y = size.height * (1 - (p.lowPrice - minPrice) / priceRange)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = lowPath,
                    color = lineColorLow,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            Column(
                Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = latest.highPrice.pretty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = lineColorHigh,
                    textAlign = TextAlign.End
                )
                Text(
                    text = latest.lowPrice.pretty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = lineColorLow,
                    textAlign = TextAlign.End
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            data.take(2).reversed().forEach { point ->
                val label = point.localDateTime.substringBefore('.').takeLast(8)
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun Float.pretty(): String =
    ((this * 100).roundToInt() / 100f).toString()


