package ga.gabboxl.pininorario.fragments

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
import ga.gabboxl.pininorario.Classe
import ga.gabboxl.pininorario.ClasseViewModel
import ga.gabboxl.pininorario.R
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.interfaces.OnClickAdaptersImplementations
import ga.gabboxl.pininparse.PininParse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PinnedFragment : Fragment() {

    companion object {
        private lateinit var classeViewModel: ClasseViewModel
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


        classeViewModel.getAllPinnedClassesWithPeriodi().observe(viewLifecycleOwner
        ) { t ->
            //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
            adapterClassi.submitList(t)
        }

        val extfab =
            fragmentView.findViewById<ExtendedFloatingActionButton>(R.id.aggiungi_classe_extfab)
        extfab.setOnClickListener {
            val alertDialogBuilder: MaterialAlertDialogBuilder =
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Seleziona una classe")
                    //.setPositiveButton("Aggiungi", null)
                    .setNeutralButton("Annulla", null)
                    .setSingleChoiceItems(
                        PininParse.Classi.listNomiClassi().toTypedArray(), -1
                    ) { dialoginterface, i ->
                        Toast.makeText(context, PininParse.Classi.list()[i][1], Toast.LENGTH_SHORT)
                            .show()

                        //Snackbar.make(findViewById(R.id.secondcoordlayout), "Classe aggiunta!", Snackbar.LENGTH_SHORT)
                        //    .show()

                        //prendo gli orari relativi alla classe
                        CoroutineScope(AndroidUiDispatcher.Main).launch {
                            //orariutils.prendiPeriodi(i)

                            //salvo nel database la classe scelta
                            val updatedpinnedclasse = Classe(
                                i + 1,
                                PininParse.Classi.list()[i][1], //nome classe
                                PininParse.Classi.list()[i][2], //codice classe
                                true
                            )
                            classeViewModel.updateClasse(updatedpinnedclasse)

                            dialoginterface.dismiss()

                        }


                    }
            alertDialogBuilder.show()
        }

        //listeners per gli adapters

        adapterClassi.setOnClickListenersClasseAdapter(OnClickAdaptersImplementations(context, classeViewModel))

        adapterClassi.setOnClickListenersPeriodoAdapter(OnClickAdaptersImplementations(context, classeViewModel))



        return fragmentView
    }


}