package ga.gabboxl.pininorario

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.io.File
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val REQUESTWRITECODE = 777

    private lateinit var listResources: JSONArray

    var griglie = arrayListOf<String>()
    var posizionespinnerperiodi: Int = 0
    var posizionespinnerclassi: Int = 0
    var periodi = arrayListOf<String>()

    var classis = arrayListOf<String>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()  //necessario??????????????????????????
            StrictMode.setThreadPolicy(policy) // ??????????????????????????????????????????????????????????????????????????????

        //controllo permesso per l'accesso alla memoria rom
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            richiediWritePermission()
        }

        //registro il ricevitore di eventi sull'azione del download completato in modo da triggerare una funzione una volta che l'evento si verifica
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            //imposto la listview dei periodi su scelta singola
            listviewPeriodi.choiceMode = ListView.CHOICE_MODE_SINGLE

            //richiamo la funzione principale
             prendiOrario()

            swiperefreshlayout.setOnRefreshListener {
                prendiOrario()
                swiperefreshlayout.isRefreshing = false
            }

        buttonPeriodifresh.setOnClickListener {
            doAsync {
                scaricaPeriodi()

                uiThread {
                   //  val adapter2 = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, periodi)
                   // spinnerPeriodi.adapter = adapter2
                    val adattatore = ArrayAdapter(applicationContext, R.layout.listview_row, R.id.textviewperiodi_row, periodi)
                    listviewPeriodi.adapter = adattatore
                }

            }
        }


        Log.e("MOOSECA", "sucsaaaaaaaaaa")
        try {
            val packageInfoversion = packageManager.getPackageInfo(packageName, 0).versionName
            Log.e("VERZIONE NOME", packageInfoversion)

            var packageInfo: PackageInfo = packageManager.getPackageInfo(this.packageName, 0)
            var curVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            Log.e("VERZIONE NUMERO a", curVersionCode.toString())
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }


    /*
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }
    */


    private fun prendiOrario() {
        doAsync {
            classis.clear()

            /*val url = "http://intranet.itispininfarina.it/orario/_ressource.js"
            FileUtils.copyURLToFile(URL(url), File("/data/user/0/ga.gabboxl.pininorario/cache/classi.js"))*/

            val apiResponse = URL("http://gabboxlbot.altervista.org/pininorario/classi.php").readText()

            listResources = JSONArray(Gson().fromJson(apiResponse, arrayListOf<String>().javaClass))

            var counter = 0
            while ((listResources.length() - 1) >= counter) {
                if(listResources.optJSONArray(counter).get(0).toString() == "grClasse") {
                    classis.add(listResources.optJSONArray(counter).get(1).toString())
                }
                counter++
            }

                uiThread {
                    val adapter1 = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, classis)
                    spinnerClassi.adapter = adapter1
                }


            spinnerClassi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                //variabili da inizializzare per poi essere utilizzate in modo globale nel codice

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    posizionespinnerclassi = position

                    if (spinnerClassi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                        return  //esco dalla funz se no da errore
                    }

                    scaricaPeriodi()

                    uiThread {
                        // val adapter2 = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, periodi)
                        // spinnerPeriodi.adapter = adapter2
                        val adattatore = ArrayAdapter(applicationContext, R.layout.listview_row, R.id.textviewperiodi_row, periodi)
                        listviewPeriodi.adapter = adattatore
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            /*
            spinnerPeriodi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    posizionespinnerperiodi = position

                    if (spinnerPeriodi.selectedItem.toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                        return  //esco dalla funz se no da errore
                    }

                    buttonScarica.visibility = View.VISIBLE

                    buttonScarica.setOnClickListener {
                        scaricaOrario()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            */

            listviewPeriodi.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    posizionespinnerperiodi = position

                    if (listviewPeriodi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                        return  //esco dalla funz se no da errore
                    }

                    buttonScarica.visibility = View.VISIBLE

                    buttonScarica.setOnClickListener {
                        scaricaOrario()
                    }

                    val text = "Hai selezionato: " + listviewPeriodi.getItemAtPosition(position).toString()
                    val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
                    toast.show()
                }
            }


        }
    }

    //funzione x scaricare dati periodi
    fun scaricaPeriodi() {
            periodi.clear()
            var codiceclasse = ""

            val apiResponsePeriodi = URL("http://gabboxlbot.altervista.org/pininorario/periodi.php").readText()
            val jsonPeriodi = JSONArray(Gson().fromJson(apiResponsePeriodi, arrayListOf<String>().javaClass))


            var contatore = 0
            while ((listResources.length() - 1) >= contatore) {
                if (listResources.optJSONArray(contatore).get(1).toString() == classis[posizionespinnerclassi]) {
                    codiceclasse = listResources.optJSONArray(contatore).get(2).toString()
                }
                contatore++
            }      //fine while

            var contatore2 = 0
            griglie = arrayListOf()

            while ((jsonPeriodi.length() - 1) > contatore2) {
                if (jsonPeriodi.optJSONArray(contatore2).get(0).toString() == codiceclasse) {
                    periodi.add(jsonPeriodi.optJSONArray(contatore2).get(1).toString()) // aggiungo i periodi "EDT N." all'array
                    griglie.add(jsonPeriodi.optJSONArray(contatore2).get(2).toString()) //aggiungo i link (semi-link) alle griglie all'array dichiarati ad inizio funzione del bottone
                }
                contatore2++
            }
    }

    //funzione x scaricare foto dell'orario
    fun scaricaOrario() {
        doAsync {
            //controllo se il permesso per l'accesso alla memoria sia garantito
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                richiediWritePermission()
                return@doAsync
            }

            //controllo che il file non sia già stato scaricato e quindi propongo di aprirlo
            if (File("/storage/emulated/0/Download" + "/PininOrari/" + "/" + griglie[posizionespinnerperiodi] + ".png").exists()) {
                Snackbar.make(
                    findViewById(R.id.myCoordinatorLayout),
                    "Orario già scaricato -->",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Apri") { apriOrario() }
                    .show()

                return@doAsync
            }

            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "In downloadhhh...", Snackbar.LENGTH_INDEFINITE)
                .show()


            val downloadManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUrl =
                Uri.parse("http://intranet.itispininfarina.it/orario/classi/" + griglie[posizionespinnerperiodi] + ".png")

            val request = DownloadManager.Request(downloadUrl)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("PininOrario - Scaricando " + griglie[posizionespinnerperiodi] + ".png")
            request.setDescription("In download " + griglie[posizionespinnerperiodi] + ".png")
            request.setVisibleInDownloadsUi(true)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/PininOrari/" + "/" + griglie[posizionespinnerperiodi] + ".png"
            )

            @Suppress("UNUSED_VARIABLE") var refid = downloadManager.enqueue(request)
        }
    }



    fun apriOrario() {
        val file = File("/storage/emulated/0/Download" + "/PininOrari/" + "/" + griglie[posizionespinnerperiodi] + ".png")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(FileProvider.getUriForFile(applicationContext, BuildConfig.APPLICATION_ID + ".provider", file), "image/png")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }



    private var onComplete = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Download completato.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Apri") {
                        apriOrario()
                    }
                    .show()
        }
    }



    private fun richiediWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            AlertDialog.Builder(this)
                .setTitle("Permesso richiesto")
                .setMessage("Questa app ha bisogno del permesso WRITE_EXTERNAL_STORAGE per scaricare l'orario!")
                .setPositiveButton("ok") { _, _ -> ActivityCompat.requestPermissions(  // onlick funzione
                    this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTWRITECODE)
                }
                .setNegativeButton("indietro") { dialog, _ -> dialog.dismiss() } //onlick funzione
                .create().show()

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTWRITECODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUESTWRITECODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
