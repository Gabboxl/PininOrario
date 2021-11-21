package ga.gabboxl.pininorario

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classipreferite")
data class Classe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val nomeClasse: String?,
    val periodiScaricati: List<String>?,
)