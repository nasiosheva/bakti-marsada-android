package com.lampung.baktimarsada.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lampung.baktimarsada.db.dao.EventDao
import com.lampung.baktimarsada.db.dao.FinanceReportDao
import com.lampung.baktimarsada.db.dao.MemberDao
import com.lampung.baktimarsada.db.dao.PaymentObligationDao
import com.lampung.baktimarsada.db.entity.EventEntity
import com.lampung.baktimarsada.db.entity.FinanceReportEntity
import com.lampung.baktimarsada.db.entity.MemberEntity
import com.lampung.baktimarsada.db.entity.PaymentObligationEntity

@Database(
    entities = [
        EventEntity::class,
        MemberEntity::class,
        FinanceReportEntity::class,
        PaymentObligationEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun memberDao(): MemberDao
    abstract fun financeReportDao(): FinanceReportDao
    abstract fun paymentObligationDao(): PaymentObligationDao
}
