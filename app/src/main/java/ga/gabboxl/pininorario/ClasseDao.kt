package ga.gabboxl.pininorario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

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









    @Transaction
    @Query("SELECT TabellaClassi.* FROM TabellaClassi JOIN TabellaPeriodi ON TabellaPeriodi.codiceClassePeriodo = TabellaClassi.codiceClasse WHERE NOT EXISTS (SELECT 1 FROM TabellaPeriodi WHERE TabellaPeriodi.codiceClassePeriodo = TabellaClassi.codiceClasse AND TabellaPeriodi.isDownloaded = 1) AND TabellaClassi.isAvailableOnServer = 1 GROUP BY TabellaClassi.codiceClasse")
    fun getClassiWithPeriodiScaricati(): List<ClasseWithPeriodi>



    @Transaction
    @Query("SELECT TabellaClassi.* " +
            "  FROM \n" +
            "    TabellaClassi \n" +
            "  WHERE\n" +
            "   NOT EXISTS (SELECT 1 FROM TabellaPeriodi WHERE TabellaPeriodi.codiceClassePeriodo = TabellaClassi.codiceClasse AND TabellaPeriodi.isDownloaded = 1)\n" +
            " AND TabellaClassi.isAvailableOnServer = 0")
    fun getClassiWithPeriodiScaricatiDark(): List<ClasseWithPeriodi>
    //thx to https://stackoverflow.com/questions/52156124/filtering-out-parent-records-based-on-conditions-on-child-records
    /* il motivo per cui è meglio utilizzare SELECT 1 negli EXISTS https://stackoverflow.com/questions/7171041/what-does-it-mean-by-select-1-from-table
    in poche parole esegue meno operazioni nel contesto degli EXISTS etc */
    //anche https://stackoverflow.com/questions/49576680/what-do-i-have-to-select-in-a-where-exist-clause


    //rip classi methods

    @Query("SELECT * FROM TabellaClassi WHERE codiceClasse NOT IN (:codiciClassiScaricateNuove)")
    fun getClassiNonInLista(codiciClassiScaricateNuove: List<String>): List<Classe>

    @Query("DELETE FROM TabellaClassi WHERE isAvailableOnServer = 0 AND  NOT EXISTS (SELECT 1 FROM TabellaPeriodi WHERE TabellaPeriodi.codiceClassePeriodo = TabellaClassi.codiceClasse)")
    fun deleteClassiRippateSenzaPeriodi() //elimino solo le classi senza una eliminazione a cascata dei periodi associati ad essa perche i periodi non ci sono piu siccome all'avvio dell'app in newactivity levo prima gli orari rippati e poi le classi rippate, che di conseguenza sono gia' senza orari



    //check methods TODO("Forse da levare perche' si puo' utilizzare il risolutore di conflitti durante l'inserimento di una classe/periodo")

    @Query("SELECT EXISTS(SELECT * FROM TabellaClassi WHERE codiceClasse = :codiceClasse)")
    fun doesClasseExist(codiceClasse: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM TabellaPeriodi WHERE codiceClassePeriodo = :codiceClassePeriodo AND nomePeriodo = :nomePeriodo)")
    fun doesPeriodoExist(codiceClassePeriodo: String, nomePeriodo: String): Boolean

    //rip periodi methods

    @Query("SELECT * FROM TabellaPeriodi WHERE periodoSemiLinkImg NOT IN (:semilinkPeriodiScaricatiNuovi)")
    fun getPeriodiNonSulServer(semilinkPeriodiScaricatiNuovi: List<String>): List<Periodo>

    @Query("DELETE FROM TabellaPeriodi WHERE isAvailableOnServer = 0 AND isDownloaded = 0")
    fun deletePeriodiMorti()

    //aggiornamenti methods

    @Query("SELECT dataAggiornamento FROM TabellaAggiornamenti ORDER BY ID DESC LIMIT 1") //also https://stackoverflow.com/a/5191525/9008381
    fun getLatestMetaAggiornamentoDateSync(): String?

    @Query("SELECT dataAggiornamento FROM TabellaAggiornamenti ORDER BY ID DESC LIMIT 1")
    fun getLatestMetaAggiornamentoDateAsync(): LiveData<String?>
}

