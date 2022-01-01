package ga.gabboxl.pininorario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewActivity : AppCompatActivity() {
    private lateinit var classeViewModel: ClasseViewModel
    private val orariutils = OrariUtils


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)


        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)



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




            orariutils.prendiClassi()

            var contatorewhileclassi = 0
            while (contatorewhileclassi < orariutils.classi.size) {

                if (!classeViewModel.doesClasseExist(orariutils.codiciclassi[contatorewhileclassi])) {
                    classeViewModel.insertClasse(
                        Classe(
                            contatorewhileclassi,
                            orariutils.classi[contatorewhileclassi],
                            orariutils.codiciclassi[contatorewhileclassi],
                            false
                        )
                    )
                }
                contatorewhileclassi++
            }


            var contatorewhileperiodi = 0
            while (contatorewhileperiodi < orariutils.classi.size) {
                orariutils.prendiPeriodi(contatorewhileperiodi)


                var contatorewhileperiodiegriglie2 = 0
                while (contatorewhileperiodiegriglie2 < orariutils.periodi.size) {

                    if (!classeViewModel.doesPeriodoExist(
                            orariutils.codiciclassi[contatorewhileperiodi],
                            orariutils.periodi[contatorewhileperiodiegriglie2]
                        )
                    ) {
                        classeViewModel.insertPeriodo(
                            Periodo(
                                contatorewhileperiodi,
                                orariutils.codiciclassi[contatorewhileperiodi],
                                orariutils.periodi[contatorewhileperiodiegriglie2],
                                orariutils.griglie[contatorewhileperiodiegriglie2],
                                isAvailableOnServer = true,
                                isDownloaded = false
                            )
                        )
                    }

                    contatorewhileperiodiegriglie2++
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