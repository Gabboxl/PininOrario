package ga.gabboxl.pininorario

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Classe::class, Periodo::class, MetaAggiornamento::class], version = 9)
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


        var roomCallback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

            }
        }
    }
}