package com.lampung.baktimarsada.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lampung.baktimarsada.db.entity.EventEntity
import com.lampung.baktimarsada.db.entity.FinanceReportEntity
import com.lampung.baktimarsada.db.entity.MemberEntity
import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY scheduled_at ASC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("DELETE FROM events")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<EventEntity>)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY full_name ASC")
    fun observeAll(): Flow<List<MemberEntity>>

    @Query("DELETE FROM members")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MemberEntity>)
}

@Dao
interface FinanceReportDao {
    @Query("SELECT * FROM finance_reports ORDER BY period_label DESC")
    fun observeAll(): Flow<List<FinanceReportEntity>>

    @Query("DELETE FROM finance_reports")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FinanceReportEntity>)
}

@Dao
interface PaymentObligationDao {
    @Query("SELECT * FROM payment_obligations ORDER BY due_date ASC")
    fun observeAll(): Flow<List<PaymentObligationEntity>>

    @Query("DELETE FROM payment_obligations")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PaymentObligationEntity>)
}
