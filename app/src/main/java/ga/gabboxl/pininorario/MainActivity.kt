package ga.gabboxl.pininorario

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File


class MainActivity : AppCompatActivity() {
    private val REQUESTWRITECODE = 777

    var posizionespinnerperiodi: Int = 0
    var posizionespinnerclassi: Int = 0


    private var nomefileOrario: String = ""
    private var urlfoto: String = ""

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

        //salvo in una variabile i valori delle impostazioni dell'app (shared preferences) per poi usufruirne più tardi
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

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

        //richiamo la funzione per prendere le classi
        CoroutineScope(Main).launch {
            OrariUtils.prendiClassi()
            val adapter1 =
                ArrayAdapter(baseContext, R.layout.support_simple_spinner_dropdown_item, OrariUtils.classi)
            spinnerClassi.adapter = adapter1
        }


        if(intent.getStringExtra("classe") != null) {
            spinnerClassi.setSelection(OrariUtils.classi.indexOf(intent.getStringExtra("classe")!!))
        }


        buttonPeriodifresh.setOnClickListener {
            CoroutineScope(Main).launch {

                buttonPeriodifresh.isEnabled = false
                listviewPeriodi.visibility = View.INVISIBLE
                listviewLoadingBar.visibility = View.VISIBLE
                OrariUtils.prendiPeriodi(posizionespinnerclassi)

                val adattatore =
                    ArrayAdapter(
                        baseContext,
                        R.layout.listview_row,
                        R.id.textviewperiodi_row,
                        OrariUtils.periodi
                    ) //utilizzo basecontext per utilizzare il contesto iniziale (Mainactivity) siccome sto eseguendo il codice all'interno di un altro contesto asincrono (anko)
                listviewPeriodi.adapter = adattatore
                listviewPeriodi.visibility = View.VISIBLE
                listviewLoadingBar.visibility = View.INVISIBLE
                buttonPeriodifresh.isEnabled = true
            }
        }

        buttonScarica.setOnClickListener {
            scaricaOrario()
        }

        buttonApri.setOnClickListener {
            apriOrario()
        }

        checkboxNomi.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                urlfoto = "https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/"
                if(OrariUtils.griglie.isNotEmpty()) {
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi] + "prof"
                }
            } else {
                urlfoto = "https://intranet.itispininfarina.it/orario/classi/"
                if(OrariUtils.griglie.isNotEmpty()) {
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi]
                }
            }

            //controllo che il file sia già stato scaricato e quindi propongo di aprirlo
            if (File("/storage/emulated/0/Download/PininOrari//$nomefileOrario.png").exists()) {
                buttonScarica.visibility = View.INVISIBLE
                buttonApri.visibility = View.VISIBLE
            } else {
                buttonScarica.visibility = View.VISIBLE
                buttonApri.visibility = View.INVISIBLE
            }
        }

        checkboxNomi.isChecked = sharedPreferences.getBoolean("always_displaynames", false)

        //controllo stato impostazioni
        if (sharedPreferences.getBoolean("checkupdates_startup", true)) {
            //controllo se sono disponibili aggiornamenti
            AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://pinin.gabboxl.ga/versions/update.json")
                .setWebviewChangelog(true)
                .setButtonDoNotShowAgainClickListener { dialog, which ->
                    sharedPreferences.edit().putBoolean("checkupdates_startup", false).apply()
                    Toasty.info(this, getString(R.string.info_modifica_scelta_aggiornamenti)).show()
                }
                .start()
        }



        spinnerClassi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //variabili da inizializzare per poi essere utilizzate in modo globale nel codice

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                posizionespinnerclassi = position

                listviewPeriodi.visibility = View.INVISIBLE
                listviewLoadingBar.visibility = View.VISIBLE
                CoroutineScope(IO).launch {


                    if (spinnerClassi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                        buttonScarica.visibility = View.INVISIBLE
                    }

                    OrariUtils.prendiPeriodi(posizionespinnerclassi)
                    withContext(Main) {

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

                val pininusername = OrariUtils.getUsername(applicationContext)
                val pininpassword = OrariUtils.getPassword(applicationContext)

                posizionespinnerperiodi = position

                if (listviewPeriodi.getItemAtPosition(position).toString().startsWith("Selezionate")) {
                    buttonScarica.visibility = View.INVISIBLE
                    buttonApri.visibility = View.INVISIBLE
                    return  //esco dalla funz se no da errore
                }


                orarioInternoLoadingBar.visibility = View.VISIBLE
                statoText.visibility = View.INVISIBLE
                CoroutineScope(Default).launch {
                    //controllo login nell'intranet per l'orario interno (con i nomi)
                    if (OrariUtils.checkLogin(applicationContext)) {

                        //controllo se è disponibile l'orario con i nomi
                        val clientok = OkHttpClient()

                        val encodedlogin: String = Base64.encodeToString(
                            "$pininusername:$pininpassword".toByteArray(),
                            Base64.NO_WRAP
                        )

                        val reqok = Request.Builder()
                            .url("https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/" + OrariUtils.griglie[position] + ".png")
                            .get()
                            .header("Authorization", "Basic $encodedlogin")
                            .build()
                        val respok = withContext(IO) { clientok.newCall(reqok).execute() }

                        if (respok.code == 200) {//è disponibile
                            withContext(Main) {
                                checkboxNomi.isEnabled = true
                                statoText.setTextColor(Color.GREEN)
                                statoText.text = getString(R.string.orario_interno_disponibile)
                            }
                        } else {//non è disponibile
                            withContext(Main) {
                                checkboxNomi.isChecked = false
                                checkboxNomi.isEnabled = false
                                statoText.setTextColor(Color.RED)
                                statoText.text = getString(R.string.orario_interno_nondisponibile)
                            }
                        }
                    } else {
                        withContext(Main) {
                            checkboxNomi.isChecked = false
                            checkboxNomi.isEnabled = false
                            statoText.setTextColor(Color.RED)
                            statoText.text = getString(R.string.login_fallito)
                        }
                    }

                    runOnUiThread {
                        orarioInternoLoadingBar.visibility = View.INVISIBLE
                        statoText.visibility = View.VISIBLE
                    }
                }


                if (checkboxNomi.isChecked) {
                    urlfoto = "https://intranet.itispininfarina.it/intrane/Orario/Interno/classi/"
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi] + "prof"
                } else {
                    urlfoto = "https://intranet.itispininfarina.it/orario/classi/"
                    nomefileOrario = OrariUtils.griglie[posizionespinnerperiodi]
                }

                //se il file non è stato ancora scaricato propongo di aprirlo
                if (File("/storage/emulated/0/Download/PininOrari//$nomefileOrario.png").exists()) {
                    buttonScarica.visibility = View.INVISIBLE
                    buttonApri.visibility = View.VISIBLE
                } else {
                    buttonScarica.visibility = View.VISIBLE
                    buttonApri.visibility = View.INVISIBLE
                }
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
    private fun scaricaOrario() {
        CoroutineScope(IO).launch {

            //controllo permesso per l'accesso alla memoria
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                richiediWritePermission()
                return@launch
            }

            Snackbar.make(findViewById(R.id.myCoordinatorLayout), getString(R.string.orario_in_download), Snackbar.LENGTH_INDEFINITE)
                .show()


            val downloadManager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val downloadUrl = Uri.parse(urlfoto + OrariUtils.griglie[posizionespinnerperiodi] + ".png")

            val pininusername = OrariUtils.getUsername(applicationContext)
            val pininpassword = OrariUtils.getPassword(applicationContext)

            val encoded: String = Base64.encodeToString("$pininusername:$pininpassword".toByteArray(), Base64.DEFAULT)

            val request = DownloadManager.Request(downloadUrl)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            //request.addRequestHeader("Authorization", "Basic $encoded")
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
        CoroutineScope(Default).launch {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                val alertpermesso = AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.permesso_richiesto))
                    .setMessage(getString(R.string.richiesta_permesso_WRITE_EXTERNAL_STORAGE))
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        ActivityCompat.requestPermissions(  // onlick funzione
                            this@MainActivity,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTWRITECODE
                        )
                    }
                    .setNegativeButton(getString(R.string.indietro)) { dialog, _ -> dialog.dismiss() } //onlick funzione

                withContext(Main) { alertpermesso.create().show() }
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