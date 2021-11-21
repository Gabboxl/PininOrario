package ga.gabboxl.pininorario

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.AndroidUiDispatcher.Companion.Main

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.security.AccessController.getContext


class NewActivity : AppCompatActivity() {
    private lateinit var classeViewModel: ClasseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new)

        CoroutineScope(Main).launch {
            //inizializzo la lista delle classi
            OrariUtils.prendiClassi()
        }
        val classiutilsarray = OrariUtils.classi

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val adapter: ClasseAdapter = ClasseAdapter()
        recyclerView.adapter = adapter

        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)
        classeViewModel.getAllClassi().observe(this, object : Observer<List<Classe>> {
            override fun onChanged(t: List<Classe>) {
                Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                adapter.setClassi(t)
            }
        })

        val extfab = findViewById<ExtendedFloatingActionButton>(R.id.aggiungi_classe_extfab)
        extfab.setOnClickListener {
            val alertDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                .setTitle("Seleziona una classe")
                    //.setPositiveButton("Aggiungi", null)
                .setNeutralButton("Annulla", null)
                .setSingleChoiceItems(
                    classiutilsarray.toTypedArray(), -1
                ) {dialoginterface, i ->
                    Toast.makeText(applicationContext, classiutilsarray[i], Toast.LENGTH_SHORT).show()

                    val nuovaclasse = Classe(0, classiutilsarray[i], listOf())
                    classeViewModel.insert(nuovaclasse)
                    dialoginterface.dismiss()
                }
            alertDialogBuilder.show()
        }

    }
}