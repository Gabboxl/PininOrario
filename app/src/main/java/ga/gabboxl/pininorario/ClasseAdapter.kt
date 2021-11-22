package ga.gabboxl.pininorario


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import kotlin.coroutines.coroutineContext


class ClasseAdapter: RecyclerView.Adapter<ClasseAdapter.ClasseHolder>() {
    private var classi: List<Classe> = ArrayList()
    private lateinit var listener: OnEliminaClickListener
    var posizioneitem: Int = -1

    inner class ClasseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var textViewTitle: TextView
        lateinit var textViewPriority: TextView
        lateinit var optionButton: ImageButton

        init {
            textViewTitle = itemView.findViewById(R.id.text_view_title)
            textViewPriority = itemView.findViewById(R.id.text_view_priority)
            optionButton = itemView.findViewById(R.id.cardoptionbutton)

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
                            if (listener != null && posizioneitem != RecyclerView.NO_POSITION) {
                                listener.onEliminaClick(classi.get(posizioneitem))
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
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.classe_card_item, parent, false)
        return ClasseHolder((itemView))
    }

    override fun onBindViewHolder(holder: ClasseHolder, position: Int) {
        val currentClasse : Classe = classi.get(position)
        holder.textViewTitle.text = currentClasse.nomeClasse
        holder.textViewPriority.text = currentClasse.nomeClasse

    }



    override fun getItemCount(): Int {
        return classi.size
    }

    fun setClassi(classi: List<Classe>){
        this.classi = classi
        notifyDataSetChanged()
    }

    interface OnEliminaClickListener{
        fun onEliminaClick(classe: Classe)
    }

    fun setOnEliminaClickListener(listener: OnEliminaClickListener){
        this.listener = listener
    }

}