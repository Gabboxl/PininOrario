package ga.gabboxl.pininorario

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val REQUESTWRITECODE = 777

    private lateinit var listResources: JSONArray

    var griglie = arrayListOf<String>()
    var posizionespinnerperiodi: Int = 0
    var posizionespinnerclassi: Int = 0
    var periodi = arrayListOf<String>()

    private var classi = arrayListOf<String>()

    private var nomefileOrario: String = ""
    private var codiceclasse = ""

    private lateinit var sharedPreferences: SharedPreferences


    override fun onStart() {
        //registro il ricevitore di eventi sull'azione del download completato in modo da triggerare una funzione una volta che l'evento si verifica
        registerReceiver(onCompleteDownloadPhoto, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        super.onStart()
    }

    override fun onStop() {
        //unregistro i ricevitori di eventi
        unregisterReceiver(onCompleteDownloadPhoto)
        super.onStop()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()  //necessario??????????????????????????
        StrictMode.setThreadPolicy(policy) // ??????????????????????????????????????????????????????????????????????????????

        //salvo in una variabile i valori delle impostazioni dell'app (shared preferences) per poi usufruirne più tardi
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)


        //controllo permesso per l'accesso alla memoria
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            richiediWritePermission()
        }


        //imposto la listview dei periodi su scelta singola
        listviewPeriodi.choiceMode = ListView.CHOICE_MODE_SINGLE

        //richiamo la funzione principale
        prendiOrario()


        buttonPeriodifresh.setOnClickListener {
            buttonPeriodifresh.isEnabled = false
            listviewPeriodi.visibility = View.INVISIBLE
            listviewLoadingBar.visibility = View.VISIBLE
            doAsync {
                scaricaPeriodi()

                uiThread {
                    val adattatore =
                        ArrayAdapter(applicationContext, R.layout.listview_row, R.id.textviewperiodi_row, periodi)
                    listviewPeriodi.adapter = adattatore
                    listviewPeriodi.visibility = View.VISIBLE
                    listviewLoadingBar.visibility = View.INVISIBLE
                    buttonPeriodifresh.isEnabled = true
                }

            }
        }

        checkboxNomi.isChecked = sharedPreferences.getBoolean("always_displaynames", false)

        //controllo stato impostazioni
        if (sharedPreferences.getBoolean("checkupdates_startup", false)) {
            //controllo se sono disponibili aggiornamenti
            AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("Gabboxl", "PininOrario")
                .start()
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.options_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        R.id.options_about -> {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    private fun prendiOrario() {
        doAsync {
            classi.clear()

            /*val url = "https://intranet.itispininfarina.it/orario/_ressource.js"
            FileUtils.copyURLToFile(URL(url), File("/data/user/0/ga.gabboxl.pininorario/cache/classi.js"))*/

            val apiResponse = URL("https://gabboxlbot.altervista.org/pininorario/classi.php").readText()

            listResources = JSONArray(Gson().fromJson(apiResponse, arrayListOf<String>().javaClass))

            var counter = 0
            while ((listResources.length() - 1) >= counter) {
                if (listResources.optJSONArray(counter).get(0).toString() == "grClasse") {
                    classi.add(listResources.optJSONArray(counter).get(1).toString())
                }
                counter++
            }

            uiThread {
                val adapter1 = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, classi)
                spinnerClassi.adapter = adapter1
            }


            spinnerClassi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                //variabili da inizializzare per poi essere utilizzate in modo globale nel codice

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    listviewPeriodi.visibility = View.INVISIBLE
                    listviewLoadingBar.visibility = View.VISIBLE

                    doAsync {
                        posizionespinnerclassi = position

                        if (spinnerClassi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                            buttonScarica.visibility = View.INVISIBLE
                        }

                        scaricaPeriodi()

                        uiThread {
                            val adattatore =
                                ArrayAdapter(
                                    applicationContext,
                                    R.layout.listview_row,
                                    R.id.textviewperiodi_row,
                                    periodi
                                )
                            listviewPeriodi.adapter = adattatore
                            listviewPeriodi.visibility = View.VISIBLE
                            listviewLoadingBar.visibility = View.INVISIBLE
                        }
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            listviewPeriodi.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    posizionespinnerperiodi = position

                    if (listviewPeriodi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                        return  //esco dalla funz se no da errore
                    }

                    //controllo se è disponibile l'orario con i nomi
                    val url = URL("https://intranet.itispininfarina.it/orarioint/classi/" + griglie[posizionespinnerperiodi] + ".png")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connect()

                    if(connection.responseCode == 404){
                        checkboxNomi.isChecked = false
                        checkboxNomi.isEnabled = false
                    }else if (connection.responseCode == 200){
                        checkboxNomi.isEnabled = true
                    }


                    buttonScarica.visibility = View.VISIBLE

                    buttonScarica.setOnClickListener {
                        scaricaOrario()
                    }

                    val text = "Hai selezionato: " + listviewPeriodi.getItemAtPosition(position).toString()
                    Toasty.info(this@MainActivity, text, Toast.LENGTH_SHORT, true).show()
                }
            }


        }
    }

    //funzione x scaricare dati periodi
    fun scaricaPeriodi() {
        periodi.clear()

        val apiResponsePeriodi = URL("https://gabboxlbot.altervista.org/pininorario/periodi.php").readText()
        val jsonPeriodi = JSONArray(Gson().fromJson(apiResponsePeriodi, arrayListOf<String>().javaClass))


        var contatore = 0
        while ((listResources.length() - 1) >= contatore) {
            if (listResources.optJSONArray(contatore).get(1).toString() == classi[posizionespinnerclassi]) {
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

            //controllo permesso per l'accesso alla memoria
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                richiediWritePermission()
                return@doAsync
            }

            val urlfoto: String

            if (checkboxNomi.isChecked) {
                urlfoto = "https://intranet.itispininfarina.it/orarioint/classi/"
                nomefileOrario = griglie[posizionespinnerperiodi] + "prof"
            } else {
                urlfoto = "https://intranet.itispininfarina.it/orario/classi/"
                nomefileOrario = griglie[posizionespinnerperiodi]
            }

            //controllo che il file non sia già stato scaricato e quindi propongo di aprirlo
            if (File("/storage/emulated/0/Download/PininOrari//$nomefileOrario.png").exists()) {
                Snackbar.make(
                    findViewById(R.id.myCoordinatorLayout),
                    getString(R.string.orario_presente),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.orario_apri)) { apriOrario() }
                    .show()

                return@doAsync
            }

            Snackbar.make(findViewById(R.id.myCoordinatorLayout), getString(R.string.orario_inDownload), Snackbar.LENGTH_INDEFINITE)
                .show()


            val downloadManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadUrl = Uri.parse(urlfoto + griglie[posizionespinnerperiodi] + ".png")

            val request = DownloadManager.Request(downloadUrl)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle("PininOrario - $nomefileOrario.png")
            request.setDescription("In download $nomefileOrario.png")
            request.setVisibleInDownloadsUi(true)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "/PininOrari//$nomefileOrario.png"
            )

            @Suppress("UNUSED_VARIABLE") var refid = downloadManager.enqueue(request)
        }
    }


    fun apriOrario() {
        val file = File("/storage/emulated/0/Download/PininOrari//$nomefileOrario.png")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            FileProvider.getUriForFile(
                applicationContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            ), "image/png"
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }


    private var onCompleteDownloadPhoto = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            Snackbar.make(findViewById(R.id.myCoordinatorLayout), getString(R.string.orario_scaricato), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.orario_apri)) {
                    apriOrario()
                }
                .show()
        }
    }


    private fun richiediWritePermission() {
        doAsync {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                val alertpermesso = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Permesso richiesto")
                    .setMessage("Questa app ha bisogno del permesso WRITE_EXTERNAL_STORAGE per scaricare l'orario!")
                    .setPositiveButton("ok") { _, _ ->
                        ActivityCompat.requestPermissions(  // onlick funzione
                            this@MainActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTWRITECODE
                        )
                    }
                    .setNegativeButton("indietro") { dialog, _ -> dialog.dismiss() } //onlick funzione

                uiThread { alertpermesso.create().show() }
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUESTWRITECODE
                )
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUESTWRITECODE) {
            Toasty.Config.reset()
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(this@MainActivity, "Permission GRANTED", Toast.LENGTH_SHORT, true).show()
            } else {
                Toasty.warning(this@MainActivity, "Permission DENIED", Toast.LENGTH_SHORT, true).show()
            }
        }
    }


}
