package ga.gabboxl.pininorario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.AndroidUiDispatcher.Companion.Main
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


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

        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)

        CoroutineScope(Dispatchers.Default).launch {
            //inizializzo i database con le classi e periodi (TO DO)


            val snackaggiornamento = Snackbar.make(findViewById(R.id.secondcoordlayout), "Aggiornamento database classi...", Snackbar.LENGTH_INDEFINITE)
            snackaggiornamento.show()

            orariutils.prendiClassi()

            var contatorewhileclassi = 0
            while (contatorewhileclassi < orariutils.classi.size){

                if(!classeViewModel.doesClasseExist(orariutils.codiciclassi[contatorewhileclassi])) {
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
            while (contatorewhileperiodi < orariutils.classi.size){
                orariutils.prendiPeriodi(contatorewhileperiodi)


                var contatorewhileperiodiegriglie2 = 0
                while(contatorewhileperiodiegriglie2 < orariutils.periodi.size) {

                    if(!classeViewModel.doesPeriodoExist(orariutils.codiciclassi[contatorewhileperiodi], orariutils.periodi[contatorewhileperiodiegriglie2])) {
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


        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val adapterClassi: ClasseAdapter = ClasseAdapter()
        recyclerView.adapter = adapterClassi

        classeViewModel.getAllPinnedClassesWithPeriodi().observe(this,
            { t ->
                //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                adapterClassi.submitList(t)
            })

        val extfab = findViewById<ExtendedFloatingActionButton>(R.id.aggiungi_classe_extfab)
        extfab.setOnClickListener {
            val alertDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                .setTitle("Seleziona una classe")
                //.setPositiveButton("Aggiungi", null)
                .setNeutralButton("Annulla", null)
                .setSingleChoiceItems(
                    orariutils.classi.toTypedArray(), -1
                ) { dialoginterface, i ->
                    Toast.makeText(applicationContext, orariutils.classi[i], Toast.LENGTH_SHORT)
                        .show()

                    //Snackbar.make(findViewById(R.id.secondcoordlayout), "Classe aggiunta!", Snackbar.LENGTH_SHORT)
                    //    .show()

                    //prendo gli orari relativi alla classe
                    CoroutineScope(Main).launch {
                        //orariutils.prendiPeriodi(i)

                        //salvo nel database la classe scelta
                        val updatedpinnedclasse = Classe(i+1, orariutils.classi[i], orariutils.codiciclassi[i], true)
                        classeViewModel.updateClasse(updatedpinnedclasse)

                        dialoginterface.dismiss()

                    }


                }
            alertDialogBuilder.show()
        }

        adapterClassi.setOnEliminaClickListener(object : ClasseAdapter.OnEliminaClickListener {
            override fun onEliminaClick(classeWithPeriodi: ClasseWithPeriodi) {
                //Toast.makeText(applicationContext, "onChanged " + adapter.posizioneitem + " " + classe, Toast.LENGTH_SHORT).show()
                classeViewModel.updateClasse(Classe(classeWithPeriodi.classe.id, classeWithPeriodi.classe.nomeClasse, classeWithPeriodi.classe.codiceClasse, false))
                //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
            }
        })


    }
}