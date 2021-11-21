package ga.gabboxl.pininorario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClasseAdapter: RecyclerView.Adapter<ClasseAdapter.ClasseHolder>() {
    private var classi: List<Classe> = ArrayList()


    class ClasseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView = itemView.findViewById(R.id.text_view_title)
        var textViewPriority: TextView = itemView.findViewById(R.id.text_view_priority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClasseHolder {
        var itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.classe_item, parent, false)
        return ClasseHolder((itemView))
    }

    override fun onBindViewHolder(holder: ClasseHolder, position: Int) {
        var currentClasse : Classe = classi.get(position)
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
}