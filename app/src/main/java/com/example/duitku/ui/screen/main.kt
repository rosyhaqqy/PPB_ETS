package com.example.duitku.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.math.abs

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf("Welcome to Duitku \uD83D\uDCB5", "Dapat Merekap Semua Pemasukan dan Pengeluaran", "Pencatatan dijamin aman")
    var currentPage by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = pages[currentPage], style = MaterialTheme.typography.headlineMedium)
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .background(
                            if (index == currentPage) Color.DarkGray else Color.LightGray,
                            shape = CircleShape
                        )
                )
            }
        }

        Button(
            onClick = {
                if (currentPage < pages.lastIndex) {
                    currentPage++
                } else {
                    onFinish()  // Go to main screen
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentPage < pages.lastIndex) "Next" else "Get Started")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceOverviewScreen(
    viewModel: TransactionViewModel,
    onAddNewRecord: () -> Unit,
    onOpenChart: () -> Unit
) {
    val recordList by viewModel.allTransactions.collectAsState()
    val incomeTotal by viewModel.totalIncome.collectAsState()
    val expenseTotal by viewModel.totalExpense.collectAsState()
    val netBalance = incomeTotal - expenseTotal

    var incomeGraph by remember { mutableStateOf(0f) }
    var expenseGraph by remember { mutableStateOf(0f) }
    var balanceGraph by remember { mutableStateOf(0f) }
    var pieData by remember { mutableStateOf<List<PieEntry>>(emptyList()) }

    val pageBackground = Color(0xFFB9B7B7)
    val incomeShade = Color(0xFF81C784)
    val expenseShade = Color(0xFFE57373)

    LaunchedEffect(recordList) {
        val income = recordList.filter { it.type == "income" }.sumOf { it.amount }.toFloat()
        val expense = abs(recordList.filter { it.type == "expense" }.sumOf { it.amount }.toFloat())

        incomeGraph = income
        expenseGraph = expense
        balanceGraph = income - expense

        val total = income + expense
        if (total > 0) {
            pieData = listOf(
                PieEntry(income, "Pemasukan"),
                PieEntry(expense, "Pengeluaran")
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ringkasan Keuangan") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewRecord,
                icon = { Icon(Icons.Filled.Add, "Tambah") },
                text = { Text("Tambah Catatan") },
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.primary
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(pageBackground)
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ringkasan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryItem("Pemasukan", incomeTotal.toCurrency(), incomeShade)
                        SummaryItem("Pengeluaran", expenseTotal.toCurrency(), expenseShade)
                        SummaryItem("Saldo", netBalance.toCurrency(), if (netBalance >= 0) incomeShade else expenseShade)
                    }
                }
            }

            // Diagram Pie
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.padding(4.dp)) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        factory = { context ->
                            PieChart(context).apply {
                                description.isEnabled = false
                                setUsePercentValues(true)
                                setDrawEntryLabels(false)
                                setCenterTextSize(16f)
                                setHoleRadius(35f)
                                setTransparentCircleRadius(40f)
                                setHoleColor(android.graphics.Color.WHITE)
                                setTransparentCircleColor(android.graphics.Color.WHITE)

                                legend.apply {
                                    isEnabled = true
                                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                                    orientation = Legend.LegendOrientation.HORIZONTAL
                                    setDrawInside(false)
                                    textSize = 14f
                                    formSize = 16f
                                    xEntrySpace = 10f
                                    yEntrySpace = 8f
                                    setWordWrapEnabled(true)
                                }

                                minimumWidth = 600
                                minimumHeight = 600
                            }
                        },
                        update = { chart ->
                            if (pieData.isNotEmpty()) {
                                val dataSet = PieDataSet(pieData, "").apply {
                                    colors = listOf(incomeShade.toArgb(), expenseShade.toArgb())
                                    valueTextColor = android.graphics.Color.BLACK
                                    valueTextSize = 16f
                                    valueFormatter = PercentageFormatter()
                                    valueLinePart1Length = 0.6f
                                    valueLinePart2Length = 0.4f
                                    valueLineWidth = 2.5f
                                    valueLineColor = android.graphics.Color.DKGRAY
                                    yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                                    sliceSpace = 2f
                                }

                                chart.data = PieData(dataSet)
                                chart.setExtraOffsets(30f, 30f, 30f, 30f)
                                chart.animateY(1200)
                                chart.invalidate()
                            }
                        }
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Riwayat Transaksi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }


            if (recordList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada catatan", color = Color.Gray, fontSize = 16.sp)
                        Text("Gunakan tombol tambah untuk mulai", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
      
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(recordList) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            TransactionItem(
                                transaction = item,
                                onDelete = { viewModel.deleteTransaction(item) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, value: String, color: Color) {
    Column {
        Text(title, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("income") }
    val category by remember { mutableStateOf("") }
    val note by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFFF5F5F5)
    val incomeColor = Color(0xFF81C784) // Hijau lembut
    val expenseColor = Color(0xFFE57373) // Merah lembut

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Transaksi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Pilihan jenis transaksi
                    Text(
                        text = "Jenis Transaksi",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    TransactionTypeSelector(
                        selectedType = type,
                        onTypeSelected = { type = it },
                        incomeColor = incomeColor,
                        expenseColor = expenseColor
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))


                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Transaksi") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = "Judul",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )


                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Nominal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = "Nominal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        prefix = { Text("Rp") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))


            Button(
                onClick = {
                    val transaction = Transaction(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = type,
                        category = category,
                        note = note
                    )
                    viewModel.addTransaction(transaction)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && amount.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "income") incomeColor else expenseColor,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Simpan",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    incomeColor: Color,
    expenseColor: Color
) {
    val options = listOf("Pemasukan" to "income", "Pengeluaran" to "expense")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            val isSelected = selectedType == value
            val chipColor = when {
                isSelected && value == "income" -> incomeColor
                isSelected && value == "expense" -> expenseColor
                else -> MaterialTheme.colorScheme.surface
            }

            ElevatedFilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(value) },
                label = {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.elevatedFilterChipColors(
                    selectedContainerColor = chipColor.copy(alpha = 0.2f),
                    selectedLabelColor = if (value == "income") Color.Green else Color.Red
                ),
                elevation = FilterChipDefaults.elevatedFilterChipElevation(),
                shape = RoundedCornerShape(30.dp)
            )
        }
    }
}
