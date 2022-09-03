package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ClasseViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: ClasseRepository = ClasseRepository(application)
    private var allClassi: LiveData<List<Classe>> = repository.getAllClassi()
    private var allPinnedClassesWithPeriodi: LiveData<List<ClasseWithPeriodi>> =
        repository.getAllPinnedClassesWithPeriodi()
    private var allClassiWithPeriodi: LiveData<List<ClasseWithPeriodi>> =
        repository.getAllClassiWithPeriodi()

    private var allNomiClassi: LiveData<List<String>> = repository.getAllNomiClassi()

    fun doesClasseExist(codiceClasse: String): Boolean {
        return repository.doesClasseExist(codiceClasse)
    }

    fun doesPeriodoExist(codiceClassePeriodo: String, nomePeriodo: String): Boolean {
        return repository.doesPeriodoExist(codiceClassePeriodo, nomePeriodo)
    }


    fun insertClasse(classe: Classe) {
        repository.insertClasse(classe)
    }

    fun updateClasse(classe: Classe) {
        repository.updateClasse(classe)
    }

    fun deleteClasse(classe: Classe) {
        repository.deleteClasse(classe)
    }


    fun insertPeriodo(periodo: Periodo) {
        repository.insertPeriodo(periodo)
    }

    fun updatePeriodo(periodo: Periodo) {
        repository.updatePeriodo(periodo)
    }

    fun deletePeriodo(periodo: Periodo) {
        repository.deletePeriodo(periodo)
    }


    fun deleteAllClassi() {
        repository.deleteAllClassi()
    }

    fun deleteAllPeriodi() {
        repository.deleteAllPeriodi()
    }

    fun getAllClassi(): LiveData<List<Classe>> {
        return allClassi
    }

    fun getAllNomiClassi(): LiveData<List<String>> {
        return allNomiClassi
    }

    fun getAllClassiWithPeriodi(): LiveData<List<ClasseWithPeriodi>> {
        return allClassiWithPeriodi
    }

    fun getAllPinnedClassesWithPeriodi(): LiveData<List<ClasseWithPeriodi>> {
        return allPinnedClassesWithPeriodi
    }
}