package ga.gabboxl.pininorario.interfacesimpls

import android.content.Context
import android.widget.Toast
import ga.gabboxl.pininorario.Classe
import ga.gabboxl.pininorario.ClasseViewModel
import ga.gabboxl.pininorario.ClasseWithPeriodi
import ga.gabboxl.pininorario.Periodo
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.adapters.PeriodoAdapter

class OnClickAdaptersImplementations(val context : Context?, private val classeViewModel: ClasseViewModel) : PeriodoAdapter.OnClickListenersPeriodoAdapter, ClasseAdapter.OnClickListenersClasseAdapter {

    override fun onPeriodoScaricaButtonClick(periodo: Periodo) {
        Toast.makeText(
            context,
            "darkkk: " + periodo.nomePeriodo + "\n classe: ",
            Toast.LENGTH_SHORT
        ).show()


        classeViewModel.updatePeriodo(
            Periodo(
                periodo.id,
                periodo.codiceClassePeriodo,
                periodo.nomePeriodo,
                periodo.periodoSemiLinkImg,
                isAvailableOnServer = true,
                isDownloaded = true
            )
        )

        //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
    }

    override fun onPeriodoApriButtonClick(periodo: Periodo) {
        Toast.makeText(context, "aprix", Toast.LENGTH_SHORT).show()
    }

    override fun onPeriodoCondividiOptionClick(periodo: Periodo) {
        Toast.makeText(context, "condividix", Toast.LENGTH_SHORT).show()
    }

    override fun onPeriodoSalvaOptionClick(periodo: Periodo) {
        Toast.makeText(context, "salvax", Toast.LENGTH_SHORT).show()
    }

    override fun onPeriodoEliminaOptionClick(periodo: Periodo) {
        Toast.makeText(context, "eliminax", Toast.LENGTH_SHORT).show()
    }

    override fun onRimuoviPrefClick(classeWithPeriodi: ClasseWithPeriodi) {
        //Toast.makeText(applicationContext, "onChanged " + adapter.posizioneitem + " " + classe, Toast.LENGTH_SHORT).show()
        classeViewModel.updateClasse(
            Classe(
                classeWithPeriodi.classe.id,
                classeWithPeriodi.classe.nomeClasse,
                classeWithPeriodi.classe.codiceClasse,
                false
            )
        )
        //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
    }

    override fun onAggiungiPrefClick(classeWithPeriodi: ClasseWithPeriodi) {
        classeViewModel.updateClasse(
            Classe(
                classeWithPeriodi.classe.id,
                classeWithPeriodi.classe.nomeClasse,
                classeWithPeriodi.classe.codiceClasse,
                true
            )
        )
    }

}