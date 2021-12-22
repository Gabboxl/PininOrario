package ga.gabboxl.pininorario

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "TabellaClassi")
data class Classe(
    val id: Int = 0,
    val nomeClasse: String,
@PrimaryKey val codiceClasse: String,
    var isPinned: Boolean,

)

@Entity(tableName = "TabellaPeriodi", primaryKeys = ["codiceClassePeriodo", "nomePeriodo"])
data class Periodo(
    val id: Int = 0,
    var codiceClassePeriodo: String,
    var nomePeriodo: String,
    var periodoSemiLinkImg: String,
    var isAvailableOnServer: Boolean,
    var isDownloaded: Boolean,

)


data class ClasseWithPeriodi(
    @Embedded val classe: Classe,
    @Relation(
        parentColumn = "codiceClasse",
        entityColumn = "codiceClassePeriodo"
    ) val periodi: List<Periodo>
)