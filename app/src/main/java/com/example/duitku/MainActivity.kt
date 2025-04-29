package com.example.duitku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.duitku.ui.theme.DuitkuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMoneyNotesTheme {
                val transactionDao = AppDatabase.getInstance(applicationContext).transactionDao()
                val transactionRepo = TransactionRepository(transactionDao)
                val transactionViewModel: TransactionViewModel = viewModel(
                    factory = TransactionViewModel.provideFactory(transactionRepo)
                )

                NavigationHost(viewModel = transactionViewModel)
            }
        }
    }
}

enum class AppScreen {
    ONBOARDING, TRANSACTIONS, ADD_ENTRY
}

@Composable
fun NavigationHost(viewModel: TransactionViewModel) {
    var activeScreen by remember { mutableStateOf(AppScreen.ONBOARDING) }

    when (activeScreen) {
        AppScreen.ONBOARDING -> OnboardingScreen(
            onFinish = { activeScreen = AppScreen.TRANSACTIONS }
        )

        AppScreen.TRANSACTIONS -> TransactionListScreen(
            viewModel = viewModel,
            onAddTransaction = { activeScreen = AppScreen.ADD_ENTRY },
        )

        AppScreen.ADD_ENTRY -> AddTransactionScreen(
            viewModel = viewModel,
            onBack = { activeScreen = AppScreen.TRANSACTIONS }
        )
    }
}
