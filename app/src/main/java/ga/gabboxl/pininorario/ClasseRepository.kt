package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClasseRepository(application: Application) {
    private var classeDao: ClasseDao
    private var allClasses: LiveData<List<Classe>>
    private var allPinnedClasses: LiveData<List<Classe>>


    init {
        val database: ClasseDatabase = ClasseDatabase.getInstance(application)
        classeDao = database.classeDao()
        allClasses = classeDao.getAllClassi()
        allPinnedClasses = classeDao.getAllPinnedClasses()
    }

    fun insertClasse(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.insertClasse(classe)
        }
    }

    fun updateClasse(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.updateClasse(classe)
        }
    }

    fun deleteClasse(classe: Classe) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.deleteClasse(classe)
        }
    }

    fun insertPeriodo(periodo: Periodo) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.insertPeriodo(periodo)
        }
    }

    fun updatePeriodo(periodo: Periodo) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.updatePeriodo(periodo)
        }
    }

    fun deletePeriodo(periodo: Periodo) {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.deletePeriodo(periodo)
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

    fun getAllPinnedClasses(): LiveData<List<Classe>> {
        return allPinnedClasses
    }


}