package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClasseRepository(application: Application) {
    private var classeDao: ClasseDao
    private var allClasses: LiveData<List<Classe>>
    private var allPinnedClasses: LiveData<List<ClasseWithPeriodi>>
    private var allClassiWithPeriodi: LiveData<List<ClasseWithPeriodi>>
    private var allNomiClassi: LiveData<List<String>>


    init {
        val database: ClasseDatabase = ClasseDatabase.getInstance(application)
        classeDao = database.classeDao()
        allClasses = classeDao.getAllClassi()
        allPinnedClasses = classeDao.getAllPinnedClassiWithPeriodi()
        allClassiWithPeriodi = classeDao.getAllClassiWithPeriodi()
        allNomiClassi = classeDao.getAllNomiClassi()
    }

    //rip methods
    fun getPeriodiNonSulServer(periodiScaricati: List<String>): List<Periodo>{
        return classeDao.getPeriodiNonSulServer(periodiScaricati)
    }

    fun deletePeriodiMorti(){
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.deletePeriodiMorti()
        }
    }


    //check methods

    fun doesClasseExist(codiceClasse: String): Boolean {
        return classeDao.doesClasseExist(codiceClasse)
    }

    fun doesPeriodoExist(codiceClassePeriodo: String, nomePeriodo: String): Boolean {
        return classeDao.doesPeriodoExist(codiceClassePeriodo, nomePeriodo)
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

    fun deleteAllPeriodi() {
        CoroutineScope(Dispatchers.Default).launch {
            classeDao.deleteAllPeriodi()
        }
    }

    fun getAllClassi(): LiveData<List<Classe>> {
        return allClasses
    }

    fun getAllNomiClassi(): LiveData<List<String>> {
        return allNomiClassi
    }

    fun getAllClassiWithPeriodi(): LiveData<List<ClasseWithPeriodi>> {
        return allClassiWithPeriodi
    }

    fun getAllPinnedClassesWithPeriodi(): LiveData<List<ClasseWithPeriodi>> {
        return allPinnedClasses
    }


}