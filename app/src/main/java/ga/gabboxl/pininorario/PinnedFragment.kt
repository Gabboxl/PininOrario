package ga.gabboxl.pininorario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PinnedFragment : Fragment() {

    companion object {
        private lateinit var classeViewModel: ClasseViewModel
        private val orariutils = OrariUtils
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentView = inflater.inflate(R.layout.fragment_pinned, container, false)



        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)


        val recyclerView: RecyclerView = fragmentView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)


        val adapterClassi: ClasseAdapter = ClasseAdapter()
        recyclerView.adapter = adapterClassi


        classeViewModel.getAllPinnedClassesWithPeriodi().observe(viewLifecycleOwner,
            { t ->
                //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                adapterClassi.submitList(t)
            })

        val extfab =
            fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.aggiungi_classe_extfab)
        extfab.setOnClickListener {
            val alertDialogBuilder: MaterialAlertDialogBuilder =
                MaterialAlertDialogBuilder(context!!)
                    .setTitle("Seleziona una classe")
                    //.setPositiveButton("Aggiungi", null)
                    .setNeutralButton("Annulla", null)
                    .setSingleChoiceItems(
                        orariutils.classi.toTypedArray(), -1
                    ) { dialoginterface, i ->
                        Toast.makeText(context, orariutils.classi[i], Toast.LENGTH_SHORT)
                            .show()

                        //Snackbar.make(findViewById(R.id.secondcoordlayout), "Classe aggiunta!", Snackbar.LENGTH_SHORT)
                        //    .show()

                        //prendo gli orari relativi alla classe
                        CoroutineScope(AndroidUiDispatcher.Main).launch {
                            //orariutils.prendiPeriodi(i)

                            //salvo nel database la classe scelta
                            val updatedpinnedclasse = Classe(
                                i + 1,
                                orariutils.classi[i],
                                orariutils.codiciclassi[i],
                                true
                            )
                            classeViewModel.updateClasse(updatedpinnedclasse)

                            dialoginterface.dismiss()

                        }


                    }
            alertDialogBuilder.show()
        }

        adapterClassi.setOnClickListenersClasseAdapter(object : ClasseAdapter.OnClickListenersClasseAdapter {
            override fun onRimuoviPrefClick(classeWithPeriodi: ClasseWithPeriodi) {
                //Toast.makeText(applicationContext, "onChanged " + adapter.posizioneitem + " " + classe, Toast.LENGTH_SHORT).show()
                classeViewModel.updateClasse(
                    Classe(
                        classeWithPeriodi.classe.id,
                        classeWithPeriodi.classe.nomeClasse,
                        classeWithPeriodi.classe.codiceClasse,
                        false
                    )
                )
                //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
            }

            override fun onAggiungiPrefClick(classeWithPeriodi: ClasseWithPeriodi) {
                //nothing happens here because in the pinned section of the app you shouldn't add classes from a card
            }
        })

        adapterClassi.setOnPeriodoButtonClickListener(object :
            PeriodiAdapter.OnPeriodoButtonClickListener {
            override fun OnPeriodoButtonClick(periodo: Periodo) {
                Toast.makeText(
                    context,
                    "per: " + periodo.nomePeriodo + "\n classe: ",
                    Toast.LENGTH_SHORT
                ).show()

                //huge thanks to https://www.youtube.com/watch?v=dYbbTGiZ2sA
            }
        })



        return fragmentView
    }


}