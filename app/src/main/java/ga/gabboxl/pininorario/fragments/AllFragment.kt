package ga.gabboxl.pininorario.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.ClasseViewModel
import ga.gabboxl.pininorario.R
import ga.gabboxl.pininorario.adapters.ClasseAdapter
import ga.gabboxl.pininorario.interfacesimpls.OnClickAdaptersImplementations
import me.zhanghai.android.fastscroll.FastScrollerBuilder


class AllFragment : Fragment() {
    companion object {
        private lateinit var classeViewModel: ClasseViewModel
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



        val adapterClassi = ClasseAdapter()
        recyclerView.adapter = adapterClassi


        classeViewModel.getAllClassiWithPeriodi().observe(viewLifecycleOwner
        ) { t ->
            //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
            adapterClassi.submitList(t)


            if (t.isNullOrEmpty()){
                recyclerView.visibility = View.GONE
                fragmentView.findViewById<TextView>(R.id.textemptyall).visibility = View.VISIBLE
            }else{
                recyclerView.visibility = View.VISIBLE
                fragmentView.findViewById<TextView>(R.id.textemptyall).visibility = View.GONE

                //scrollbar lib
                FastScrollerBuilder(recyclerView)
                    .setPopupTextProvider { position -> t[position].classe.nomeClasse }
                    .useMd2Style()
                    .build()
            }
        }



        /*classeViewModel.viewModelScope.launch(Dispatchers.Default){
            sleep(6000)

            withContext(Dispatchers.Main){
                classeViewModel.getAllPinnedClassesWithPeriodi().observe(viewLifecycleOwner
                ) { t ->
                    //Toast.makeText(applicationContext, "onChanged", Toast.LENGTH_SHORT).show()
                    adapterClassi.submitList(t)
                }
            }
        }*/

        //listeners per gli adapters

        adapterClassi.setOnClickListenersClasseAdapter(OnClickAdaptersImplementations(requireContext(), classeViewModel))

        adapterClassi.setOnClickListenersPeriodoAdapter(OnClickAdaptersImplementations(requireContext(), classeViewModel))



        return fragmentView
    }


}