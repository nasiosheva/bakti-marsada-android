package com.lampung.baktimarsada.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.lampung.baktimarsada.db.dao.EventDao
import com.lampung.baktimarsada.db.dao.EventProgramItemDao
import com.lampung.baktimarsada.db.dao.FinanceReportDao
import com.lampung.baktimarsada.db.dao.MemberDao
import com.lampung.baktimarsada.db.dao.PaymentObligationDao
import com.lampung.baktimarsada.db.dao.WorshipTemplateDao
import com.lampung.baktimarsada.db.dao.WorshipTemplateItemDao
import com.lampung.baktimarsada.db.entity.EventEntity
import com.lampung.baktimarsada.db.entity.EventProgramItemEntity
import com.lampung.baktimarsada.db.entity.FinanceReportEntity
import com.lampung.baktimarsada.db.entity.MemberEntity
import com.lampung.baktimarsada.db.entity.PaymentObligationEntity
import com.lampung.baktimarsada.db.entity.WorshipTemplateEntity
import com.lampung.baktimarsada.db.entity.WorshipTemplateItemEntity

@Database(
    entities = [
        EventEntity::class,
        EventProgramItemEntity::class,
        MemberEntity::class,
        FinanceReportEntity::class,
        PaymentObligationEntity::class,
        WorshipTemplateEntity::class,
        WorshipTemplateItemEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventProgramItemDao(): EventProgramItemDao
    abstract fun memberDao(): MemberDao
    abstract fun financeReportDao(): FinanceReportDao
    abstract fun paymentObligationDao(): PaymentObligationDao
    abstract fun worshipTemplateDao(): WorshipTemplateDao
    abstract fun worshipTemplateItemDao(): WorshipTemplateItemDao
}

// created by Mories Deo Hutapea, S.E.,S.Kom
