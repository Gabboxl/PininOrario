package ga.gabboxl.pininorario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class ClasseViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: ClasseRepository = ClasseRepository(application)
    private var allClassi: LiveData<List<Classe>> = repository.getAllClassi()


    fun insert(classe: Classe) {
        repository.insert(classe)
    }

    fun update(classe: Classe) {
        repository.update(classe)
    }

    fun delete(classe: Classe) {
        repository.delete(classe)
    }

    fun deleteAllClassi() {
        repository.deleteAllClassi()
    }

    fun getAllClassi(): LiveData<List<Classe>> {
        return allClassi
    }
}