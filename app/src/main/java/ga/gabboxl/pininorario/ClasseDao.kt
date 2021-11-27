package ga.gabboxl.pininorario

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClasseDao {

    //classe
    @Insert
    fun insertClasse(classe: Classe)

    @Update
    fun updateClasse(classe: Classe)

    @Delete
    fun deleteClasse(classe: Classe)

    //periodo
    @Insert
    fun insertPeriodo(periodo: Periodo)

    @Update
    fun updatePeriodo(periodo: Periodo)

    @Delete
    fun deletePeriodo(periodo: Periodo)

    //custom queries

    @Transaction
    @Query("DELETE FROM TabellaClassi")
    fun deleteAllClassi()

    @Query("SELECT * FROM TabellaClassi")
    fun getAllClassi(): LiveData<List<Classe>>

    @Query("SELECT * FROM TabellaClassi WHERE isPinned = true")
    fun getAllPinnedClasses(): LiveData<List<Classe>>

    //funzione per prendere una classe con i relativi periodi
    @Transaction
    @Query("SELECT * FROM TabellaClassi WHERE codiceClasse = :codiceClasse")
    fun getClasseWithPeriodi(codiceClasse: String): LiveData<List<ClasseWithPeriodi>>
}

