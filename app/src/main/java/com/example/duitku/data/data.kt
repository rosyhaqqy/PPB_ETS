package com.example.duitku.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

// TransactionApp.kt (misal nama filenya)

package com.example.mymoneyapp

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.util.Date

// --- Entity ---
@Entity(tableName = "transactions")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true) val recordId: Int = 0,
    val description: String,
    val amountValue: Double,
    val recordType: String, // "income" atau "expense"
    val label: String,
    val timestamp: Long = Date().time,
    val additionalNote: String? = null
)

// --- DAO ---
@Dao
interface FinanceDao {
    @Insert
    suspend fun addRecord(record: FinanceRecord)

    @Delete
    suspend fun removeRecord(record: FinanceRecord)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun fetchAllRecords(): Flow<List<FinanceRecord>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income'")
    fun calculateTotalIncome(): Flow<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense'")
    fun calculateTotalExpense(): Flow<Double>
}

// --- Database ---
@Database(entities = [FinanceRecord::class], version = 1)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getInstance(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_db"
                ).build()
                INSTANCE = dbInstance
                dbInstance
            }
        }
    }
}

// --- Repository ---
class FinanceRepository(private val dao: FinanceDao) {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    val recordsFlow: StateFlow<List<FinanceRecord>> =
        dao.fetchAllRecords()
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeFlow: StateFlow<Double> =
        dao.calculateTotalIncome()
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val expenseFlow: StateFlow<Double> =
        dao.calculateTotalExpense()
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), 0.0)

    suspend fun add(record: FinanceRecord) = dao.addRecord(record)
    suspend fun delete(record: FinanceRecord) = dao.removeRecord(record)
}
