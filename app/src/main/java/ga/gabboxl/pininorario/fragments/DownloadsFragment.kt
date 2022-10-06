package ga.gabboxl.pininorario.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.ClasseViewModel
import ga.gabboxl.pininorario.R
import ga.gabboxl.pininorario.adapters.PeriodoDownloadsAdapter
import ga.gabboxl.pininorario.interfacesimpls.OnClickAdaptersImplementations

class DownloadsFragment : Fragment() {
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
        val fragmentView =  inflater.inflate(R.layout.fragment_downloads, container, false)

        classeViewModel = ViewModelProvider(this).get(ClasseViewModel::class.java)

        val recyclerView: RecyclerView = fragmentView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        val adapterPeriodiDownloads = PeriodoDownloadsAdapter()
        recyclerView.adapter = adapterPeriodiDownloads


        classeViewModel.getAllDownloadedPeriodiWithClasse().observe(viewLifecycleOwner
        ) { t ->
            adapterPeriodiDownloads.submitList(t)

            if (t.isNullOrEmpty()){
                recyclerView.visibility = View.GONE
                fragmentView.findViewById<TextView>(R.id.textemptydownloaded).visibility = View.VISIBLE
            }else{
                recyclerView.visibility = View.VISIBLE
                fragmentView.findViewById<TextView>(R.id.textemptydownloaded).visibility = View.GONE
            }
        }


        adapterPeriodiDownloads.setOnClickListenersPeriodoDownloadsAdapter(OnClickAdaptersImplementations(requireContext(), classeViewModel))


        return fragmentView
    }
}