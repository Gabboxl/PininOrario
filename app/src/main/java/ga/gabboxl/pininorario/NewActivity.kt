package ga.gabboxl.pininorario

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import ga.gabboxl.pininparse.PininParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class NewActivity : AppCompatActivity() {
    private lateinit var classeViewModel: ClasseViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var menuOptions: Menu


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        menuOptions = menu

        ConnectivityUtils.isInternetAvailable.observe(this) { isConnected ->
            if (isConnected != null && isConnected) {
                menu.findItem(R.id.appbar_option_refreshallorari).isEnabled = true
                menu.findItem(R.id.appbar_option_checkdataaggiornamentoorari).isEnabled = true
            } else {
                menu.findItem(R.id.appbar_option_refreshallorari).isEnabled = false
                menu.findItem(R.id.appbar_option_checkdataaggiornamentoorari).isEnabled = false
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.appbar_option_checkdataaggiornamentoorari -> {
            classeViewModel.viewModelScope.launch(Dispatchers.Default) {
                if (isAggiornamentoOrariDisponibile()) {
                    inizializzaOrari()
                }
            }
            true
        }

        R.id.appbar_option_refreshallorari -> {
            classeViewModel.viewModelScope.launch(Dispatchers.Default) {
                withContext(Dispatchers.Main) {
                    item.isEnabled = false
                    menuOptions.findItem(R.id.appbar_option_checkdataaggiornamentoorari).isEnabled = false
                }

                inizializzaOrari()

                withContext(Dispatchers.Main) {
                    item.isEnabled = true
                    menuOptions.findItem(R.id.appbar_option_checkdataaggiornamentoorari).isEnabled = true
                }
            }
            true
        }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        setSupportActionBar(findViewById(R.id.mainCustomBarraTop))

        WindowCompat.setDecorFitsSystemWindows(window, false)


        //TODO("colori forse da levare e implementare una custom materialappbar con magari animazioni allo scorrimento")
        val color = SurfaceColors.SURFACE_2.getColor(this)
        //window.statusBarColor = Color.TRANSPARENT
        //window.navigationBarColor = color
        findViewById<AppBarLayout>(R.id.mainCustomBarLayout).setBackgroundColor(color)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)


        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)


        //levo i periodi rippati TODO("forse da mettere in un onStart dell'app ?? + un controllo se esistono periodi rippati prima di eseguire?")
        //nono anzi forse lasciarlo prima dell'aggiornamento del database altrimenti elimina anche gli orari appena segnati come non disponibili senza che l'utente ne venga informato
        classeViewModel.deletePeriodiMorti()
        classeViewModel.deleteClassiRippateSenzaPeriodi()


        //inizializzo il controllo della connettivita'
        ConnectivityUtils.init(this)

        ConnectivityUtils.isInternetAvailable.observe(this) { isConnected ->
            if (isConnected != null && isConnected) {

                classeViewModel.viewModelScope.launch(Dispatchers.Default) {
                    if (isAggiornamentoOrariDisponibile()) {
                        inizializzaOrari()
                    }
                }

                if (sharedPreferences.getBoolean("checkupdates_startup", true)) {
                    //controllo se sono disponibili aggiornamenti
                    AppUpdater(this)
                        .setDisplay(Display.DIALOG)
                        .setUpdateFrom(UpdateFrom.JSON)
                        .setUpdateJSON("https://pinin.gabboxl.ga/versions/update.json")
                        .setWebviewChangelog(true)
                        .setButtonDoNotShowAgainClickListener { dialog, which ->
                            sharedPreferences.edit().putBoolean("checkupdates_startup", false)
                                .apply()
                            Toasty.info(
                                this,
                                getString(R.string.info_modifica_scelta_aggiornamenti)
                            ).show()
                        }
                        .start()
                }
            } else if (isConnected == false) {
                val snackaggiornamento = Snackbar.make(
                    findViewById(R.id.fragmentContainerView),
                    getString(R.string.nessuna_connessione_internet),
                    Snackbar.LENGTH_LONG
                )
                    .setAction("OK") {}

                val contentLay: ViewGroup =
                    snackaggiornamento.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
                contentLay.setPadding(24, 24, 24, 24)

                snackaggiornamento.show()
            }
        }

        //imposto l'ultima data di aggiornamento degli orari del server nel textview dell'appbar
        classeViewModel.getLatestMetaAggiornamentoDateAsync()
            .observe(this) { stringDataAggiornamento ->
                findViewById<TextView>(R.id.textAggiornamentoAppBar).text =
                    getString(R.string.orari_server_aggiornati_al_data, stringDataAggiornamento)
            }

    }


    suspend fun isAggiornamentoOrariDisponibile(): Boolean {
        //snackbar
        val snackaggiornamento = Snackbar.make(
            findViewById(R.id.fragmentContainerView),
            getString(R.string.controllo_aggiornamenti_snackbar),
            Snackbar.LENGTH_INDEFINITE
        )
            .setBehavior(NoSwipeBehavior())
        val contentLay: ViewGroup =
            snackaggiornamento.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
        val item = ProgressBar(applicationContext).also { it.setPadding(24, 24, 24, 24) }
        contentLay.addView(item)
        snackaggiornamento.show()


        PininParse.Update.init()

        val serveraggiornamento = PininParse.Update.list()!!

        val latestSavedMetaAggiornamento = classeViewModel.getLatestMetaAggiornamentoDateSync()

        if (latestSavedMetaAggiornamento == null) {
            classeViewModel.insertMetaAggiornamento(
                MetaAggiornamento(
                    0,
                    serveraggiornamento
                )
            )

            snackaggiornamento.dismiss()
            return true
        }

        val sdf = SimpleDateFormat("dd/mm/yyyy", Locale.ITALY)
        val timedb = sdf.parse(latestSavedMetaAggiornamento)
        val timeserver = sdf.parse(serveraggiornamento)

        if (timeserver!!.compareTo(timedb) == 0) {
            snackaggiornamento.dismiss()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@NewActivity,
                    getString(R.string.orari_aggiornati_toast),
                    Toast.LENGTH_SHORT
                ).show()
            }
            return false
        }

        snackaggiornamento.dismiss()
        return true
    }


    suspend fun inizializzaOrari() {

        //inizializzo i database con le classi e periodi (forse utilizzare un metodo migliore per l'aggiunta al database)

        val snackaggiornamento = Snackbar.make(
            findViewById(R.id.fragmentContainerView),
            getString(R.string.aggiornamento_database_classi_snackbar),
            Snackbar.LENGTH_INDEFINITE
        )
            .setBehavior(NoSwipeBehavior())

        val contentLay: ViewGroup =
            snackaggiornamento.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
        val item = ProgressBar(applicationContext).also { it.setPadding(24, 24, 24, 24) }
        contentLay.addView(item)

        snackaggiornamento.show()

        PininParse.Classi.init()
        PininParse.Periodi.init()

        val listaCodiciClassiNuovi = mutableListOf<String>()

        for ((indexattuale, classe) in PininParse.Classi.list().withIndex()) {

            if (!classeViewModel.doesClasseExist(classe[2])) { //controllo il codice classe
                classeViewModel.insertClasse(
                    Classe(
                        indexattuale,
                        classe[1], //nome classe
                        classe[2], //codice classe
                        isAvailableOnServer = true,
                        isPinned = false
                    )
                )
            }

            listaCodiciClassiNuovi.add(classe[2])
        }


        val listaSemiLinkPeriodiNuovi = mutableListOf<String>()

        for ((indexattuale, periodo) in PininParse.Periodi.list().withIndex()) {
            if (!classeViewModel.doesPeriodoExist(
                    periodo[0], //codice classe periodo
                    periodo[1] //nome periodo
                )
            ) {
                classeViewModel.insertPeriodo(
                    Periodo(
                        id = indexattuale,
                        codiceClassePeriodo = periodo[0],
                        nomePeriodo = periodo[1], //nome periodo
                        periodoSemiLinkImg = periodo[2], //nome griglia
                        titoloPeriodo = periodo[3],
                        isAvailableOnServer = true,
                        isDownloaded = false
                    )
                )
            }

            listaSemiLinkPeriodiNuovi.add(periodo[2])
        }

        /* classeViewModel.insertClasse(
             Classe(
                 777,
                 "oliverclasse",
                 "heldens",
                 isAvailableOnServer = true,
                 isPinned = false
             )
         )

         classeViewModel.insertPeriodo(
          Periodo(
              777,
              "heldens",
              "darksideperiodo", //nome periodo
              "jojo", //nome griglia
              isAvailableOnServer = true,
              isDownloaded = false
          )
      )
            */


        val classidalevare = classeViewModel.getClassiNonInLista(listaCodiciClassiNuovi)

        for (classe in classidalevare) {
            classeViewModel.updateClasse(
                Classe(
                    classe.id,
                    classe.nomeClasse,
                    classe.codiceClasse,
                    isAvailableOnServer = false,
                    classe.isPinned
                )
            )
        }


        val periodidalevare = classeViewModel.getPeriodiNonSulServer(listaSemiLinkPeriodiNuovi)

        //imposto i periodi morti come non piu' disponibili per il download
        for (periodo in periodidalevare) {
            classeViewModel.updatePeriodo(
                Periodo(
                    id = periodo.id,
                    codiceClassePeriodo = periodo.codiceClassePeriodo,
                    nomePeriodo = periodo.nomePeriodo,
                    periodoSemiLinkImg = periodo.periodoSemiLinkImg,
                    titoloPeriodo = periodo.titoloPeriodo,
                    isAvailableOnServer = false,
                    isDownloaded = periodo.isDownloaded
                )
            )
        }


        withContext(Dispatchers.Main) {
            if (classidalevare.isNotEmpty()) {
                val infoRipPeriodoDialog = MaterialAlertDialogBuilder(this@NewActivity)
                    .setTitle(getString(R.string.info_classi_titolo_alert))
                    .setMessage(
                        getString(R.string.classi_non_piu_disponibili_messaggio_alert)
                    )
                    .setPositiveButton(getString(R.string.OK)) { _, _ ->
                    }
                infoRipPeriodoDialog.create().show()
            }

            if (periodidalevare.isNotEmpty()) {
                val infoRipPeriodoDialog = MaterialAlertDialogBuilder(this@NewActivity)
                    .setTitle(getString(R.string.info_periodi_titolo_alert))
                    .setMessage(
                        getString(R.string.periodi_non_piu_disponibili_messaggio_alert)
                    )
                    .setPositiveButton(getString(R.string.OK)) { _, _ ->
                    }
                infoRipPeriodoDialog.create().show()
            }
        }

        snackaggiornamento.dismiss()
    }


    class NoSwipeBehavior : BaseTransientBottomBar.Behavior() {
        override fun canSwipeDismissView(child: View): Boolean {
            return false
        }
    }
}