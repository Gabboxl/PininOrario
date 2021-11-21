package ga.gabboxl.pininorario

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClasseDao {

    @Insert
    fun insert(classe: Classe)

    @Update
    fun update(classe: Classe)

    @Delete
    fun delete(classe: Classe)

    @Query("DELETE FROM classipreferite")
    fun deleteAllClassi()

    @Query("SELECT * FROM classipreferite")
    fun getAllClassi(): LiveData<List<Classe>>
}
