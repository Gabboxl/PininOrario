package ga.gabboxl.pininorario

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

        CoroutineScope(Main).launch {
            //inizializzo la lista delle classi
            orariutils.prendiClassi()
        }


        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val adapter: ClasseAdapter = ClasseAdapter()
        recyclerView.adapter = adapter

        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)
        classeViewModel.getAllClassi().observe(this,
            { t ->
                //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                adapter.submitList(t)
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
                        orariutils.prendiPeriodi(i)

                        //salvo nel database la classe scelta
                        val nuovaclasse = Classe(0, orariutils.classi[i], orariutils.periodi.toList(), listOf())
                        classeViewModel.insert(nuovaclasse)

                        dialoginterface.dismiss()
                    }


                }
            alertDialogBuilder.show()
        }

        adapter.setOnEliminaClickListener(object : ClasseAdapter.OnEliminaClickListener {
            override fun onEliminaClick(classe: Classe) {
                //Toast.makeText(applicationContext, "onChanged " + adapter.posizioneitem + " " + classe, Toast.LENGTH_SHORT).show()
                classeViewModel.delete(classe)
                //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
            }
        })


    }
}