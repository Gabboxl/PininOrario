package ga.gabboxl.pininorario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ga.gabboxl.pininparse.PininParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewActivity : AppCompatActivity() {
    private lateinit var classeViewModel: ClasseViewModel


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)


        //TODO("colori forse da levare e implementare una custom materialappbar con magari animazioni allo scorrimento")
        val color = SurfaceColors.SURFACE_2.getColor(this)
        window.statusBarColor = color
        window.navigationBarColor = color

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)


        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)


        //inizializzo il controllo della connettivita'
        ConnectivityUtils.init(this)

        ConnectivityUtils.isInternetAvailable.observe(this, Observer { isConnected ->
            if (isConnected != null && isConnected) {

                //TODO("levare inizializzaorari() e mettere una funzione che controlla l'ultima data di aggiornamento del sito cosi' si risparmia energia preziosa")
                inizializzaOrari()
            } else if (isConnected == false) {
                val snackaggiornamento = Snackbar.make(
                    findViewById(R.id.fragmentContainerView),
                    "Nessuna connessione ad internet.",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("OK") {}
                    .setBehavior(NoSwipeBehavior())

                val contentLay: ViewGroup =
                    snackaggiornamento.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
                contentLay.setPadding(24, 24, 24, 24)

                snackaggiornamento.show()
            }
        })


        //levo i periodi rippati TODO("forse da mettere in un onStart dell'app ?? + un controllo se esistono periodi rippati prima di eseguire?")
        classeViewModel.deletePeriodiMorti()
    }


    fun inizializzaOrari() {
        classeViewModel.viewModelScope.launch(Dispatchers.Default) {

            //inizializzo i database con le classi e periodi (forse utilizzare un metodo migliore per l'aggiunta al database)

            val snackaggiornamento = Snackbar.make(
                findViewById(R.id.fragmentContainerView),
                "Aggiornamento database classi...",
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


            var contatorewhileclassi = 0
            while (contatorewhileclassi < PininParse.Classi.list().size) {

                if (!classeViewModel.doesClasseExist(PininParse.Classi.list()[contatorewhileclassi][2])) { //controllo il codice classe
                    classeViewModel.insertClasse(
                        Classe(
                            contatorewhileclassi,
                            PininParse.Classi.list()[contatorewhileclassi][1], //nome classe
                            PininParse.Classi.list()[contatorewhileclassi][2], //codice classe
                            false
                        )
                    )
                }
                contatorewhileclassi++
            }


            val listaPeriodi = mutableListOf<String>()

            for ((indexattuale, periodo) in PininParse.Periodi.list().withIndex()) {
                if (!classeViewModel.doesPeriodoExist(
                        periodo[0], //codice classe periodo
                        periodo[1] //nome periodo
                    )
                ) {
                    classeViewModel.insertPeriodo(
                        Periodo(
                            indexattuale,
                            periodo[0],
                            periodo[1], //nome periodo
                            periodo[2], //nome griglia
                            isAvailableOnServer = true,
                            isDownloaded = false
                        )
                    )
                }

                listaPeriodi.add(periodo[2])
            }


            /*     classeViewModel.insertPeriodo(
                     Periodo(
                         777,
                         "oliver",
                         "heldens", //nome periodo
                         "jojo", //nome griglia
                         isAvailableOnServer = true,
                         isDownloaded = true
                     )
                 ) */


            val periodidalevare = classeViewModel.getPeriodiNonSulServer(listaPeriodi)

            //imposto i periodi morti come non piu' disponibili per il download
            for (periodo in periodidalevare) {
                classeViewModel.updatePeriodo(
                    Periodo(
                        periodo.id,
                        periodo.codiceClassePeriodo,
                        periodo.nomePeriodo,
                        periodo.periodoSemiLinkImg,
                        isAvailableOnServer = false,
                        periodo.isDownloaded
                    )
                )
            }

            withContext(Dispatchers.Main) {
                if (periodidalevare.isNotEmpty()) {
                    val infoRipPeriodoDialog = MaterialAlertDialogBuilder(this@NewActivity)
                        .setTitle("Info")
                        .setMessage(
                            "Sono stati trovati degli orari non più disponibili sul server per il download. " +
                                    "\nAl prossimo avvio dell'app verranno rimossi dal database soltanto quelli non scaricati." +
                                    "\nQuelli già scaricati rimarranno intatti."
                        )
                        .setPositiveButton("OK") { _, _ ->
                        }
                    infoRipPeriodoDialog.create().show()
                }
            }

            snackaggiornamento.dismiss()
        }
    }


    class NoSwipeBehavior : BaseTransientBottomBar.Behavior() {
        override fun canSwipeDismissView(child: View): Boolean {
            return false
        }
    }
}