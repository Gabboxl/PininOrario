package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ClasseViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: ClasseRepository = ClasseRepository(application)
    private var allClassi: LiveData<List<Classe>> = repository.getAllClassi()
    private var allPinnedClasses: LiveData<List<Classe>> = repository.getAllPinnedClasses()


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

    fun getAllClassi(): LiveData<List<Classe>> {
        return allClassi
    }

    fun getAllPinnedClasses(): LiveData<List<Classe>> {
        return allPinnedClasses
    }
}