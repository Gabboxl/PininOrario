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
import android.util.Base64
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
import androidx.preference.PreferenceManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private val REQUESTWRITECODE = 777

    var posizionespinnerperiodi: Int = 0
    var posizionespinnerclassi: Int = 0


    private var nomefileOrario: String = ""
    private var urlfoto: String = ""

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pininusername: String
    private lateinit var pininpassword: String



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

        //salvo in una variabile i valori delle impostazioni dell'app (shared preferences) per poi usufruirne più tardi
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pininusername = sharedPreferences.getString("pinin_username", "")!!
        pininpassword = sharedPreferences.getString("pinin_password", "")!!

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()  //necessario??????????????????????????
        StrictMode.setThreadPolicy(policy) // ??????????????????????????????????????????????????????????????????????????????


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

        OrariUtils.prendiOrario()
        val adapter1 = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, OrariUtils.classi)
        spinnerClassi.adapter = adapter1


        if(intent.getStringExtra("classe") != null) {
            spinnerClassi.setSelection(OrariUtils.classi.indexOf(intent.getStringExtra("classe")!!))
        }


        buttonPeriodifresh.setOnClickListener {
            buttonPeriodifresh.isEnabled = false
            listviewPeriodi.visibility = View.INVISIBLE
            listviewLoadingBar.visibility = View.VISIBLE
            doAsync {
                OrariUtils.scaricaPeriodi(posizionespinnerclassi)

                uiThread {
                    val adattatore =
                        ArrayAdapter(baseContext, R.layout.listview_row, R.id.textviewperiodi_row, OrariUtils.periodi) //utilizzo basecontext per utilizzare il contesto iniziale (Mainactivity) siccome sto eseguendo il codice all'interno di un altro contesto asincrono (anko)
                    listviewPeriodi.adapter = adattatore
                    listviewPeriodi.visibility = View.VISIBLE
                    listviewLoadingBar.visibility = View.INVISIBLE
                    buttonPeriodifresh.isEnabled = true
                }

            }
        }

        checkboxNomi.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                urlfoto = "https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/"
                if(OrariUtils.griglie.isNotEmpty()) {
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi] + "prof"
                }
                Toasty.success(applicationContext, "checkato", Toasty.LENGTH_SHORT).show()
            } else {
                urlfoto = "https://intranet.itispininfarina.it/orario/classi/"
                if(OrariUtils.griglie.isNotEmpty()) {
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi]
                }
                Toasty.warning(applicationContext, "non checkato", Toasty.LENGTH_SHORT).show()
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



        spinnerClassi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //variabili da inizializzare per poi essere utilizzate in modo globale nel codice

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                posizionespinnerclassi = position

                listviewPeriodi.visibility = View.INVISIBLE
                listviewLoadingBar.visibility = View.VISIBLE
                doAsync {


                    if (spinnerClassi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                    }

                    OrariUtils.scaricaPeriodi(posizionespinnerclassi)
                    uiThread {

                        val adattatore =
                            ArrayAdapter(
                                baseContext,
                                R.layout.listview_row,
                                R.id.textviewperiodi_row,
                                OrariUtils.periodi
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
                    buttonApri.visibility = View.INVISIBLE
                    return  //esco dalla funz se no da errore
                }

                //controllo login per l'orario interno (con i nomi)
                if(OrariUtils.checkLogin(applicationContext)) {
                    //controllo se è disponibile l'orario con i nomi
                    val url =
                        URL("https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/" + OrariUtils.griglie[position] + ".png")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val encoded: String = Base64.encodeToString("$pininusername:$pininpassword".toByteArray(), Base64.DEFAULT)
                    connection.setRequestProperty("Authorization", "Basic $encoded")
                    connection.connect()

                    if (connection.responseCode == 200) {
                        checkboxNomi.isEnabled = true
                    } else {
                        checkboxNomi.isChecked = false
                        checkboxNomi.isEnabled = false
                    }
                } else {
                    checkboxNomi.isChecked = false
                    checkboxNomi.isEnabled = false
                }


                if (checkboxNomi.isChecked) {
                    urlfoto = "https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/"
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi] + "prof"
                } else {
                    urlfoto = "https://intranet.itispininfarina.it/orario/classi/"
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi]
                }

                //controllo che il file sia già stato scaricato e quindi propongo di aprirlo
                if (File("/storage/emulated/0/Download/PininOrari//$nomefileOrario.png").exists()) {
                    buttonScarica.visibility = View.INVISIBLE
                    buttonApri.visibility = View.VISIBLE

                    buttonApri.setOnClickListener {
                        apriOrario()
                    }

                } else {
                    buttonScarica.visibility = View.VISIBLE
                    buttonApri.visibility = View.INVISIBLE

                    buttonScarica.setOnClickListener {
                        scaricaOrario()
                    }
                }

                val text = "Hai selezionato: " + listviewPeriodi.getItemAtPosition(position).toString()
                Toasty.info(this@MainActivity, text, Toast.LENGTH_SHORT, true).show()
            }
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

            Snackbar.make(findViewById(R.id.myCoordinatorLayout), getString(R.string.orario_inDownload), Snackbar.LENGTH_INDEFINITE)
                .show()


            val downloadManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadUrl = Uri.parse(urlfoto + OrariUtils.griglie[posizionespinnerperiodi] + ".png")

            val encoded: String = Base64.encodeToString("$pininusername:$pininpassword".toByteArray(), Base64.DEFAULT)

            val request = DownloadManager.Request(downloadUrl)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.addRequestHeader("Authorization", "Basic $encoded")
            request.setAllowedOverRoaming(false)
            request.setTitle("PininOrario - $nomefileOrario.png")
            request.setDescription("In download $nomefileOrario.png")
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

            buttonScarica.visibility = View.INVISIBLE
            buttonApri.visibility = View.VISIBLE
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