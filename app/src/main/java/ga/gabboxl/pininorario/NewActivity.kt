package ga.gabboxl.pininorario

import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ga.gabboxl.pininparse.PininParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)


        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)


        val connectivity = ConnectivityCheck(this)
        connectivity.observe(this, Observer {
                isConnected ->
            if(isConnected){
                inizializzaOrari()
            }else{
                val snackaggiornamento = Snackbar.make(
                    findViewById(R.id.fragmentContainerView),
                    "Niente internett",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nice"){}
                    .setBehavior(NoSwipeBehavior())

                val contentLay: ViewGroup =
                    snackaggiornamento.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
                contentLay.setPadding(24, 24, 24, 24)

                snackaggiornamento.show()
            }
        })
    }





    fun inizializzaOrari() {
        classeViewModel.viewModelScope.launch(Dispatchers.Default) {
            //inizializzo i database con le classi e periodi (TO DO)


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


            var contatorewhileperiodi = 0
            while (contatorewhileperiodi < PininParse.Periodi.list().size) {

                if (!classeViewModel.doesPeriodoExist(
                        PininParse.Periodi.list()[contatorewhileperiodi][0], //codice classe periodo
                        PininParse.Periodi.list()[contatorewhileperiodi][1] //nome periodo
                    )
                ) {
                    classeViewModel.insertPeriodo(
                        Periodo(
                            contatorewhileperiodi,
                            PininParse.Periodi.list()[contatorewhileperiodi][0],
                            PininParse.Periodi.list()[contatorewhileperiodi][1], //nome periodo
                            PininParse.Periodi.list()[contatorewhileperiodi][2], //nome griglia
                            isAvailableOnServer = true,
                            isDownloaded = false
                        )
                    )
                }

                contatorewhileperiodi++
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