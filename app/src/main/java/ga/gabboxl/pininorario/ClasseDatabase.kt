package ga.gabboxl.pininorario

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Classe::class], version = 1)
@TypeConverters(Converters::class)
abstract class ClasseDatabase: RoomDatabase() {

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

                CoroutineScope(Dispatchers.Default).launch {
                        instance!!.classeDao().insert(Classe(1, "Classe 1", listOf()))
                    instance!!.classeDao().insert(Classe(2, "Classe 2", listOf()))
                    instance!!.classeDao().delete(Classe(3, "Classe 3", listOf()))

                }
            }
        }
    }
}