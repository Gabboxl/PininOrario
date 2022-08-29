package ga.gabboxl.pininorario.adapters

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
import ga.gabboxl.pininorario.Periodo
import ga.gabboxl.pininorario.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class PeriodoAdapter :
    ListAdapter<Periodo, PeriodoAdapter.PeriodiHolder>(DIFF_CALLBACK) {

    private lateinit var listenersPeriodoAdapter: OnClickListenersPeriodoAdapter
    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<Periodo> = object :
            DiffUtil.ItemCallback<Periodo>() {
            override fun areItemsTheSame(oldItem: Periodo, newItem: Periodo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Periodo, newItem: Periodo): Boolean {
                return oldItem.nomePeriodo == newItem.nomePeriodo
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


            popupperiodo = PopupMenu(itemView.context, optionPeriodoButton)
            popupperiodo.menuInflater.inflate(R.menu.periodioptions_menu, popupperiodo.menu)
            popupperiodo.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.condividiperiodo_opt -> {

                    }

                    R.id.salvaingalleriaperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                        //    listenersClasseAdapter.onAggiungiPrefClick(getItem(posizioneitem))
                        }
                    }

                    R.id.eliminaperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                        //    listenersClasseAdapter.onRimuoviPrefClick(getItem(posizioneitem))
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
        val currentPeriodo: Periodo = getItem(position)

        holder.scaricaButton.isVisible = !currentPeriodo.isDownloaded
        holder.apriButton.isVisible = currentPeriodo.isDownloaded

        var somma = 0

        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
        val sdf = SimpleDateFormat("dd MM yyyy", Locale.ITALY)
        cal.time = sdf.parse("23 08 2021") as Date // data inizio degli orari


        val p: Pattern = Pattern.compile("\\d+")
        val m: Matcher = p.matcher(currentPeriodo.nomePeriodo)

        while (m.find()) {
            somma += m.group().toInt()
        }

        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)

        cal.add(Calendar.WEEK_OF_YEAR, somma)
        val dateinizio: String = simpleDateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 5)
        val datafine: String = simpleDateFormat.format(cal.time)



        holder.textViewPeriodo.text = "Dal $dateinizio al $datafine"

    }

    fun getClasseAt(position: Int): Periodo {
        return getItem(position)
    }


    interface OnClickListenersPeriodoAdapter {
        fun onPeriodoScaricaButtonClick(periodo: Periodo)
    }

    fun setOnClickListenersPeriodoAdapter(listenersPeriodoAdapter: OnClickListenersPeriodoAdapter) {
        this.listenersPeriodoAdapter = listenersPeriodoAdapter
    }

}