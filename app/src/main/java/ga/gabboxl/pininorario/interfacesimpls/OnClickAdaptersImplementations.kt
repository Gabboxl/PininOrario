package ga.gabboxl.pininorario.interfacesimpls

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import es.dmoral.toasty.Toasty
import ga.gabboxl.pininorario.*
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.adapters.PeriodoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException


class OnClickAdaptersImplementations(val context : Context?, private val classeViewModel: ClasseViewModel) : PeriodoAdapter.OnClickListenersPeriodoAdapter, ClasseAdapter.OnClickListenersClasseAdapter {

    override fun onPeriodoScaricaButtonClick(periodo: PeriodoWithClasse) {
        Toast.makeText(
            context,
            "haha yes: classe: " + periodo.classe.nomeClasse ,
            Toast.LENGTH_SHORT
        ).show()


/*      Con downloadmanager non è possibile salvare nella cartella dedicata dell'app: https://stackoverflow.com/a/71341789/9008381


        var nomefileOrario = periodo.periodoSemiLinkImg

        val url = Uri.parse("https://orario.itispininfarina.it/classi/" + periodo.periodoSemiLinkImg + ".png")

        val file = File(context?.filesDir, "darkside.png")

        val request = DownloadManager.Request(url)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        request.setDestinationUri(Uri.fromFile(file))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(true)
        request.setAllowedOverMetered(true)
        request.setTitle("PininOrario - $nomefileOrario.png")
        request.setDescription("In download $nomefileOrario.png")

        val downloadManager: DownloadManager = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadManager.enqueue(request)*/

        classeViewModel.viewModelScope.launch(Dispatchers.Default) {
            //scarico l'immagine con okhttp
            val clientok = OkHttpClient()
            val reqimg = Request.Builder()
                .url("https://orario.itispininfarina.it/classi/" + periodo.periodo.periodoSemiLinkImg + ".png")
                .get()
                .build()
            val respok = clientok.newCall(reqimg).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Toasty.error(context!!, "haha yes $e", Toasty.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    val filex = File(context?.filesDir, "darkside.png")
                    //if (filex.exists()) {
                        //nice
                    //}
                    val fileCreated: Boolean = filex.createNewFile()
                    val sink: BufferedSink = filex.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.close()
                }
            })
        }


        classeViewModel.updatePeriodo(
            Periodo(
                periodo.periodo.id,
                periodo.periodo.codiceClassePeriodo,
                periodo.periodo.nomePeriodo,
                periodo.periodo.periodoSemiLinkImg,
                isAvailableOnServer = true,
                isDownloaded = true
            )
        )

        //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
    }

    override fun onPeriodoApriButtonClick(periodo: PeriodoWithClasse) {
        val file = File(context?.filesDir, "darkside.png")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                context!!.applicationContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            ), "image/png"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    override fun onPeriodoCondividiOptionClick(periodo: PeriodoWithClasse) {
        Toast.makeText(context, "condividix", Toast.LENGTH_SHORT).show()
    }

    override fun onPeriodoSalvaOptionClick(periodo: PeriodoWithClasse) {
        Toast.makeText(context, "salvax", Toast.LENGTH_SHORT).show()
    }

    override fun onPeriodoEliminaOptionClick(periodo: PeriodoWithClasse) {
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