package ga.gabboxl.pininorario


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class ClasseAdapter : ListAdapter<ClasseWithPeriodi, ClasseAdapter.ClasseHolder>(ClasseAdapter.DIFF_CALLBACK) {
    private lateinit var listener: OnEliminaClickListener
    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<ClasseWithPeriodi> = object :
            DiffUtil.ItemCallback<ClasseWithPeriodi>() {
            override fun areItemsTheSame(oldItem: ClasseWithPeriodi, newItem: ClasseWithPeriodi): Boolean {
                return oldItem.classe.id == newItem.classe.id
            }

            override fun areContentsTheSame(oldItem: ClasseWithPeriodi, newItem: ClasseWithPeriodi): Boolean {
                return oldItem.classe.nomeClasse == newItem.classe.nomeClasse && //da modificare con i dati dei periodi
                        oldItem.classe.codiceClasse == newItem.classe.codiceClasse && oldItem.classe.isPinned == newItem.classe.isPinned
            }
        }
    }

    inner class ClasseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView
        var textViewNomeClasse: TextView
        var optionButton: ImageButton
        var recyclerViewPeriodi: RecyclerView

        init {
            textViewTitle = itemView.findViewById(R.id.text_view_title)
            textViewNomeClasse = itemView.findViewById(R.id.text_view_nomeclasse)
            optionButton = itemView.findViewById(R.id.cardoptionbutton)
            recyclerViewPeriodi = itemView.findViewById(R.id.recyclerview_periodi)

            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            optionButton.setOnClickListener {
                val popup: PopupMenu = PopupMenu(itemView.context, optionButton)
                popup.menuInflater.inflate(R.menu.cardclasse_menu, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.movecardmenuoption -> {

                        }

                        R.id.deletecardmenuoption -> {
                            posizioneitem = absoluteAdapterPosition
                            if (posizioneitem != RecyclerView.NO_POSITION) {
                                listener.onEliminaClick(getItem(posizioneitem))
                            }
                        }
                    }

                    true
                }
                popup.show() //showing popup menu
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClasseHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.classe_card_item, parent, false)
        return ClasseHolder((itemView))
    }

    override fun onBindViewHolder(holder: ClasseHolder, position: Int) {
        val currentClasse: ClasseWithPeriodi = getItem(position)
        holder.textViewTitle.text = currentClasse.classe.codiceClasse
        holder.textViewNomeClasse.text = currentClasse.classe.nomeClasse

        holder.recyclerViewPeriodi.layoutManager = LinearLayoutManager(holder.itemView.context)

        val periodiadapter: PeriodiAdapter = PeriodiAdapter()
        holder.recyclerViewPeriodi.adapter = periodiadapter

        periodiadapter.submitList(currentClasse.periodi)

    }

    fun getClasseAt(position: Int): ClasseWithPeriodi {
        return getItem(position)
    }

    interface OnEliminaClickListener {
        fun onEliminaClick(classeWithPeriodi: ClasseWithPeriodi)
    }

    fun setOnEliminaClickListener(listener: OnEliminaClickListener) {
        this.listener = listener
    }


}