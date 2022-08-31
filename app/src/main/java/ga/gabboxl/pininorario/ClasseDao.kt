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

    @Transaction
    @Query("DELETE FROM TabellaPeriodi")
    fun deleteAllPeriodi()

    @Query("SELECT * FROM TabellaClassi")
    fun getAllClassi(): LiveData<List<Classe>>

    @Transaction
    @Query("SELECT * FROM TabellaClassi")
    fun getAllClassiWithPeriodi(): LiveData<List<ClasseWithPeriodi>>

    @Transaction
    @Query("SELECT * FROM TabellaClassi WHERE isPinned = 1")
    fun getAllPinnedClassiWithPeriodi(): LiveData<List<ClasseWithPeriodi>>

    //funzione per prendere una classe con i relativi periodi
    @Transaction
    @Query("SELECT * FROM TabellaClassi WHERE codiceClasse = :codiceClasse")
    fun getClasseWithPeriodi(codiceClasse: String): LiveData<List<ClasseWithPeriodi>>

    //check methods

    @Query("SELECT EXISTS(SELECT * FROM TabellaClassi WHERE codiceClasse = :codiceClasse)")
    fun doesClasseExist(codiceClasse: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM TabellaPeriodi WHERE codiceClassePeriodo = :codiceClassePeriodo AND nomePeriodo = :nomePeriodo)")
    fun doesPeriodoExist(codiceClassePeriodo: String, nomePeriodo: String): Boolean
}

