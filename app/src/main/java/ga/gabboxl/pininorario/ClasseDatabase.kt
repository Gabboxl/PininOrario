package ga.gabboxl.pininorario

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Classe::class, Periodo::class, MetaAggiornamento::class], version = 12, exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 10, to = 11),
        AutoMigration (from = 11, to = 12),
    ])
@TypeConverters(Converters::class)
abstract class ClasseDatabase : RoomDatabase() {

    abstract fun classeDao(): ClasseDao

    companion object {
        private var instance: ClasseDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ClasseDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClasseDatabase::class.java, "classi_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()
            }
            return instance as ClasseDatabase
        }


        var roomCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

            }
        }
    }
}