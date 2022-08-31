package ga.gabboxl.pininorario.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.ClasseWithPeriodi
import ga.gabboxl.pininorario.Periodo
import ga.gabboxl.pininorario.PeriodoWithClasse
import ga.gabboxl.pininorario.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class PeriodoAdapter : ListAdapter<PeriodoWithClasse, PeriodoAdapter.PeriodiHolder>(DIFF_CALLBACK) {

    private lateinit var listenersPeriodoAdapter: OnClickListenersPeriodoAdapter
    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<PeriodoWithClasse> = object :
            DiffUtil.ItemCallback<PeriodoWithClasse>() {
            override fun areItemsTheSame(oldItem: PeriodoWithClasse, newItem: PeriodoWithClasse): Boolean {
                return oldItem.periodo.id == newItem.periodo.id
            }

            override fun areContentsTheSame(oldItem: PeriodoWithClasse, newItem: PeriodoWithClasse): Boolean {
                return oldItem.periodo.nomePeriodo == newItem.periodo.nomePeriodo
            }
        }
    }

    // TODO(to rename in PeriodoHolder)
    inner class PeriodiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var textViewPeriodo: TextView
        lateinit var scaricaButton: Button
        lateinit var apriButton: Button
        val optionPeriodoButton: ImageButton
        val popupperiodo: PopupMenu

        init {
            textViewPeriodo = itemView.findViewById(R.id.textperiodo)
            scaricaButton = itemView.findViewById(R.id.card_periodoscarica)
            apriButton = itemView.findViewById(R.id.card_periodoapri)
            optionPeriodoButton = itemView.findViewById(R.id.periodoButtonOpzioni)




            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            scaricaButton.setOnClickListener {
                posizioneitem = absoluteAdapterPosition
                listenersPeriodoAdapter.onPeriodoScaricaButtonClick(getItem(posizioneitem))
            }

            apriButton.setOnClickListener {
                posizioneitem = absoluteAdapterPosition
                listenersPeriodoAdapter.onPeriodoApriButtonClick(getItem(posizioneitem))
            }


            popupperiodo = PopupMenu(itemView.context, optionPeriodoButton)
            //popupperiodo.setForceShowIcon(true) does not work in API levels <29 so let's use a workaround
            popupperiodo.menuInflater.inflate(R.menu.periodioptions_menu, popupperiodo.menu)


            //that's the workaround, it's not ideal/official but it works
            val pop = PopupMenu::class.java.getDeclaredField("mPopup")
            pop.isAccessible = true
            val menupop = pop.get(popupperiodo)
            menupop.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menupop, true)

            popupperiodo.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.condividiperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                            listenersPeriodoAdapter.onPeriodoCondividiOptionClick(getItem(posizioneitem))
                        }
                    }

                    R.id.salvaingalleriaperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                        listenersPeriodoAdapter.onPeriodoSalvaOptionClick(getItem(posizioneitem))
                        }
                    }

                    R.id.eliminaperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                        listenersPeriodoAdapter.onPeriodoEliminaOptionClick(getItem(posizioneitem))
                        }
                    }
                }
                true
            }
            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            optionPeriodoButton.setOnClickListener {
                popupperiodo.show() //showing popup menu
            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodiHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.periodi_card_item, parent, false)
        return PeriodiHolder((itemView))
    }

    override fun onBindViewHolder(holder: PeriodiHolder, position: Int) {
        val currentPeriodo: PeriodoWithClasse = getItem(position)

        holder.scaricaButton.isVisible = !currentPeriodo.periodo.isDownloaded
        holder.apriButton.isVisible = currentPeriodo.periodo.isDownloaded
        holder.optionPeriodoButton.isVisible = currentPeriodo.periodo.isDownloaded


        var somma = 0

        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
        val sdf = SimpleDateFormat("dd MM yyyy", Locale.ITALY)
        cal.time = sdf.parse("5 07 2021") as Date // data inizio degli orari


        val p: Pattern = Pattern.compile("\\d+")
        val m: Matcher = p.matcher(currentPeriodo.periodo.nomePeriodo)

        while (m.find()) {
            somma += m.group().toInt()
        }

        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)

        cal.add(Calendar.WEEK_OF_YEAR, somma)
        val dateinizio: String = simpleDateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 5)
        val datafine: String = simpleDateFormat.format(cal.time)



        holder.textViewPeriodo.text = "$dateinizio -> $datafine"

        //holder.textViewPeriodo.text = currentPeriodo.periodo.nomePeriodo

    }

    fun getPeriodoAt(position: Int): PeriodoWithClasse {
        return getItem(position)
    }


    interface OnClickListenersPeriodoAdapter {
        fun onPeriodoScaricaButtonClick(periodo: PeriodoWithClasse)
        fun onPeriodoApriButtonClick(periodo: PeriodoWithClasse)
        fun onPeriodoCondividiOptionClick(periodo: PeriodoWithClasse)
        fun onPeriodoSalvaOptionClick(periodo: PeriodoWithClasse)
        fun onPeriodoEliminaOptionClick(periodo: PeriodoWithClasse)
    }

    fun setOnClickListenersPeriodoAdapter(listenersPeriodoAdapter: OnClickListenersPeriodoAdapter) {
        this.listenersPeriodoAdapter = listenersPeriodoAdapter
    }

}