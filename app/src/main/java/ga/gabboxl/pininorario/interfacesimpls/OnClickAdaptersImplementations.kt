package ga.gabboxl.pininorario.interfacesimpls

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import es.dmoral.toasty.Toasty
import ga.gabboxl.pininorario.*
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.adapters.PeriodoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException


class OnClickAdaptersImplementations(val context : Context, private val classeViewModel: ClasseViewModel) : PeriodoAdapter.OnClickListenersPeriodoAdapter, ClasseAdapter.OnClickListenersClasseAdapter {


    override fun onPeriodoAvailabilityButtonClick(periodo: PeriodoWithClasse, holder: PeriodoAdapter.PeriodiHolder) {
        if (periodo.periodo.isAvailableOnServer)
        {
            val infoPeriodoDialog = MaterialAlertDialogBuilder(context)
                .setTitle("Info")
                .setMessage("Questo periodo è ancora disponibile sul server per il download.")
                .setPositiveButton("OK") { _, _ ->
                }

             infoPeriodoDialog.create().show()
        } else {
            val infoPeriodoDialog = MaterialAlertDialogBuilder(context)
                .setTitle("Info")
                .setMessage("Questo periodo è stato rimosso dal server della scuola, per cui non è più disponibile per il download.")
                .setPositiveButton("OK") { _, _ ->
                }

            infoPeriodoDialog.create().show()
        }
    }


    override fun onPeriodoScaricaButtonClick(periodo: PeriodoWithClasse, holder: PeriodoAdapter.PeriodiHolder) {

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

        val nomefileorario = periodo.classe.nomeClasse + " " + periodo.periodo.nomePeriodo +".png"

        holder.scaricaButton.visibility = View.INVISIBLE
        holder.scaricaPeriodoProgressBar.visibility = View.VISIBLE

        classeViewModel.viewModelScope.launch(Dispatchers.Default) {
            //scarico l'immagine con okhttp
            val clientok = OkHttpClient()
            val reqimg = Request.Builder()
                .url("https://orario.itispininfarina.it/classi/" + periodo.periodo.periodoSemiLinkImg + ".png")
                .get()
                .build()
            val respok = clientok.newCall(reqimg).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    Toasty.error(context, "Errore: $e", Toasty.LENGTH_LONG).show()

                    holder.scaricaButton.visibility = View.VISIBLE
                    holder.scaricaPeriodoProgressBar.visibility = View.INVISIBLE
                }

                override fun onResponse(call: Call, response: Response) {
                    val filex = File(context.filesDir, nomefileorario)
                    //if (filex.exists()) {
                        //nice
                    //}
                    val fileCreated: Boolean = filex.createNewFile()
                    val sink: BufferedSink = filex.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.close()


                    //aggiorno il database per il periodo
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
                }
            })
        }

        //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
    }

    override fun onPeriodoApriButtonClick(periodo: PeriodoWithClasse) {
        val nomefileorario = periodo.classe.nomeClasse + " " + periodo.periodo.nomePeriodo +".png"

        val file = File(context.filesDir, nomefileorario)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                context.applicationContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            ), "image/png"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    override fun onPeriodoCondividiOptionClick(periodo: PeriodoWithClasse) {
        val nomefileorario = periodo.classe.nomeClasse + " " + periodo.periodo.nomePeriodo +".png"

        val file = File(context.filesDir, nomefileorario)
        val asd = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, asd)
            //setDataAndType(asd, "image/png")
            type = "image/png"
            //flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(Intent.createChooser(shareIntent, "Orario " + periodo.classe.nomeClasse))

        //nun si sa se i permessi di accesso al file rimangono all'app di destinazione scelta ma vabb
    }

    override fun onPeriodoSalvaOptionClick(periodo: PeriodoWithClasse) {
        val nomefileorario = periodo.classe.nomeClasse + " " + periodo.periodo.nomePeriodo +".png"

        classeViewModel.viewModelScope.launch(Dispatchers.Default) {


            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale( // questo check si mette per questo motivo https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati
                            context as Activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        val alertpermesso = MaterialAlertDialogBuilder(context)
                            .setTitle(context.getString(R.string.permesso_richiesto))
                            .setMessage(context.getString(R.string.richiesta_permesso_WRITE_EXTERNAL_STORAGE))
                            .setPositiveButton("Concedi") { _, _ ->
                                ActivityCompat.requestPermissions(  // onlick funzione
                                    context,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                    777
                                )
                            }
                            .setNegativeButton("Annulla") { dialog, _ -> dialog.dismiss() } //onlick funzione

                        withContext(Dispatchers.Main) { alertpermesso.create().show() }
                    } else {
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            777
                        )
                    }

            } else {
                //forse aggiornare mediastore per farlo vedere fin da subito nella galleria?


                val destinationFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/PininOrario/" + nomefileorario)
                //destinationFile.createNewFile() penso sia inutile in ogni caso

                try {
                    val filecopia = File(context.filesDir, nomefileorario)
                        .copyTo(destinationFile)

                    //forse aggiornare mediastore per farlo vedere fin da subito nella galleria? solo per API Q?

                    withContext(Dispatchers.Main) { Toasty.success(context, "Orario salvato nella galleria.", Toasty.LENGTH_SHORT).show() }

                }catch (e: FileAlreadyExistsException){
                    withContext(Dispatchers.Main) { Toasty.info(context, "Orario già salvato in galleria!", Toasty.LENGTH_SHORT).show() }
                }
            }
        }


/*
        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }



        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        */
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