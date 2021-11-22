package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClasseRepository(application: Application) {
    private var classeDao: ClasseDao
    private var allClasses: LiveData<List<Classe>>


    init {
        val database: ClasseDatabase = ClasseDatabase.getInstance(application)
        classeDao = database.classeDao()
        allClasses = classeDao.getAllClassi()
    }

    fun insert(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.insert(classe)
        }
    }

    fun update(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.update(classe)
        }
    }

    fun delete(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.delete(classe)
        }
    }

    fun deleteAllClassi() {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.deleteAllClassi()
        }
    }

    fun getAllClassi(): LiveData<List<Classe>> {
        return allClasses
    }


}