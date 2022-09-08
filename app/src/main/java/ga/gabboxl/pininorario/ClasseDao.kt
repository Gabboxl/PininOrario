package ga.gabboxl.pininorario

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClasseDao {

    //metaaggiornamento
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMetaAggiornamento(metaAggiornamento: MetaAggiornamento)

    @Update
    fun updateMetaAggiornamento(metaAggiornamento: MetaAggiornamento)

    @Delete
    fun deleteMetaAggiornamento(metaAggiornamento: MetaAggiornamento)

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

    @Query("SELECT nomeClasse FROM TabellaClassi")
    fun getAllNomiClassi(): LiveData<List<String>> //forse string nun va bene

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

    //check methods TODO("Forse da levare perche' si puo' utilizzare il risolutore di conflitti durante l'inserimento di una classe/periodo")

    @Query("SELECT EXISTS(SELECT * FROM TabellaClassi WHERE codiceClasse = :codiceClasse)")
    fun doesClasseExist(codiceClasse: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM TabellaPeriodi WHERE codiceClassePeriodo = :codiceClassePeriodo AND nomePeriodo = :nomePeriodo)")
    fun doesPeriodoExist(codiceClassePeriodo: String, nomePeriodo: String): Boolean

    //rip periodi methods
    @Query("SELECT * FROM TabellaPeriodi WHERE periodoSemiLinkImg NOT IN (:periodiScaricati)")
    fun getPeriodiNonSulServer(periodiScaricati: List<String>): List<Periodo>

    @Query("DELETE FROM TabellaPeriodi WHERE isAvailableOnServer = 0 AND isDownloaded = 0")
    fun deletePeriodiMorti()

    //aggiornamenti methods
    @Query("SELECT dataAggiornamento FROM TabellaAggiornamenti ORDER BY ID DESC LIMIT 1") //also https://stackoverflow.com/a/5191525/9008381
    fun getLatestMetaAggiornamentoDate(): String?
}

