package ga.gabboxl.pininorario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class PeriodiAdapter constructor(var datiPeriodo: List<ClasseWithPeriodi>) : ListAdapter<Classe, PeriodiAdapter.PeriodiHolder>(PeriodiAdapter.DIFF_CALLBACK) {


    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<Classe> = object :
            DiffUtil.ItemCallback<Classe>() {
            override fun areItemsTheSame(oldItem: Classe, newItem: Classe): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Classe, newItem: Classe): Boolean {
                return oldItem.nomeClasse.equals(newItem.nomeClasse) //&&
                 //       oldItem.periodi!! == newItem.periodi
            }
        }
    }

    inner class PeriodiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var textViewPeriodo: TextView
        lateinit var scaricaButton: Button

        init {
            textViewPeriodo = itemView.findViewById(R.id.textperiodo)
            scaricaButton = itemView.findViewById(R.id.card_periodoscarica)

            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            scaricaButton.setOnClickListener {
                //scarico l'orario
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodiHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.classe_card_item, parent, false)
        return PeriodiHolder((itemView))
    }

    override fun onBindViewHolder(holder: PeriodiHolder, position: Int) {
        val currentClasse: Classe = getItem(position)


    }

    fun getClasseAt(position: Int): Classe {
        return getItem(position)
    }

}