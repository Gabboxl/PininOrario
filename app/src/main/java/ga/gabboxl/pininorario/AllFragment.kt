package ga.gabboxl.pininorario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.adapters.PeriodoAdapter


class AllFragment : Fragment() {
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
        val fragmentView = inflater.inflate(R.layout.fragment_all, container, false)




        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)


        val recyclerView: RecyclerView = fragmentView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)


        val adapterClassi: ClasseAdapter = ClasseAdapter()
        recyclerView.adapter = adapterClassi


        classeViewModel.getAllClassiWithPeriodi().observe(viewLifecycleOwner,
            { t ->
                //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                adapterClassi.submitList(t)
            })


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
                classeViewModel.updateClasse(
                    Classe(
                        classeWithPeriodi.classe.id,
                        classeWithPeriodi.classe.nomeClasse,
                        classeWithPeriodi.classe.codiceClasse,
                        true
                    )
                )
            }
        })

        adapterClassi.setOnClickListenersPeriodoAdapter(object :
            PeriodoAdapter.OnClickListenersPeriodoAdapter {
            override fun onPeriodoScaricaButtonClick(periodo: Periodo) {
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