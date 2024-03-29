package ga.gabboxl.pininorario.adapters

import android.content.res.Resources.Theme
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.ConnectivityUtils
import ga.gabboxl.pininorario.PeriodoWithClasse
import ga.gabboxl.pininorario.R
import ga.gabboxl.pininorario.utils.DatePeriodoParsing


class PeriodoAdapter : ListAdapter<PeriodoWithClasse, PeriodoAdapter.PeriodoHolder>(DIFF_CALLBACK) {

    private lateinit var listenersPeriodoAdapter: OnClickListenersPeriodoAdapter
    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<PeriodoWithClasse> = object :
            DiffUtil.ItemCallback<PeriodoWithClasse>() {
            override fun areItemsTheSame(
                oldItem: PeriodoWithClasse,
                newItem: PeriodoWithClasse
            ): Boolean {
                return oldItem.periodo.id == newItem.periodo.id
            }

            override fun areContentsTheSame(
                oldItem: PeriodoWithClasse,
                newItem: PeriodoWithClasse
            ): Boolean {
                return oldItem.periodo == newItem.periodo
            }
        }
    }


    inner class PeriodoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewPeriodo: TextView
        var scaricaButton: Button
        var apriButton: Button
        var scaricaPeriodoProgressBar: ProgressBar
        var periodoButtonAvailability: ImageButton
        val optionPeriodoButton: ImageButton
        val popupperiodo: PopupMenu

        init {
            scaricaPeriodoProgressBar = itemView.findViewById(R.id.progressBarDownloadPeriodo)
            textViewPeriodo = itemView.findViewById(R.id.textperiodo)
            scaricaButton = itemView.findViewById(R.id.card_periodoscarica)
            apriButton = itemView.findViewById(R.id.card_periodoapri)
            optionPeriodoButton = itemView.findViewById(R.id.periodoButtonOpzioni)
            periodoButtonAvailability = itemView.findViewById(R.id.periodoButtonAvailability)


            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            scaricaButton.setOnClickListener {
                posizioneitem = absoluteAdapterPosition

                listenersPeriodoAdapter.onPeriodoScaricaButtonClick(getItem(posizioneitem), this)
            }

            apriButton.setOnClickListener {
                posizioneitem = absoluteAdapterPosition
                listenersPeriodoAdapter.onPeriodoApriButtonClick(getItem(posizioneitem))
            }

            periodoButtonAvailability.setOnClickListener {
                posizioneitem = absoluteAdapterPosition
                listenersPeriodoAdapter.onPeriodoAvailabilityButtonClick(
                    getItem(posizioneitem)
                )
            }


            popupperiodo = PopupMenu(itemView.context, optionPeriodoButton)
            //popupperiodo.setForceShowIcon(true) does not work in API levels <29 so let's use a workaround
            popupperiodo.menuInflater.inflate(R.menu.periodioptions_menu, popupperiodo.menu)

            //that's the workaround, it's not ideal/official but it works
            val pop = PopupMenu::class.java.getDeclaredField("mPopup")
            pop.isAccessible = true
            val menupop = pop.get(popupperiodo)
            menupop.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menupop, true)

            popupperiodo.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.condividiperiodo_opt -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                            listenersPeriodoAdapter.onPeriodoCondividiOptionClick(
                                getItem(
                                    posizioneitem
                                )
                            )
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
                            listenersPeriodoAdapter.onPeriodoEliminaOptionClick(
                                getItem(
                                    posizioneitem
                                )
                            )
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeriodoHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.periodo_card_item, parent, false)
        return PeriodoHolder((itemView))
    }

    override fun onBindViewHolder(holder: PeriodoHolder, position: Int) {
        val currentPeriodo: PeriodoWithClasse = getItem(position)

        holder.scaricaButton.isVisible = !currentPeriodo.periodo.isDownloaded
        holder.apriButton.isVisible = currentPeriodo.periodo.isDownloaded
        holder.optionPeriodoButton.isVisible = currentPeriodo.periodo.isDownloaded


        ConnectivityUtils.isInternetAvailable.observe(holder.itemView.context as LifecycleOwner) { isConnected ->
            if (isConnected) {
                if (currentPeriodo.periodo.isAvailableOnServer) {
                    holder.scaricaButton.isEnabled = true

                    //getDrawable(holder.itemView.context, R.drawable.ic_baseline_cloud_queue_24)!!.setTint(getColor(holder.itemView.context, R.color.md_theme_dark_primary))

                    val unwrappedDrawable =
                        getDrawable(holder.itemView.context, R.drawable.ic_baseline_cloud_queue_24)

                    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)


                    val typedValue = TypedValue()
                    val theme: Theme = holder.itemView.context.theme
                    theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                    val color: Int = typedValue.data

                    DrawableCompat.setTint(wrappedDrawable, color)


                    holder.periodoButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_queue_24)
                } else {
                    holder.scaricaButton.isEnabled = false

                    /*

                    getDrawable(holder.itemView.context, R.drawable.ic_baseline_cloud_off_24)!!.setTint(getColor(holder.itemView.context, R.color.md_theme_light_error))
                    */

                    holder.periodoButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                    holder.periodoButtonAvailability.drawable.setTint(
                        getColor(
                            holder.itemView.context,
                            R.color.md_theme_light_error
                        )
                    )
                }

            } else {
                holder.scaricaButton.isEnabled = false

                /*
                getDrawable(holder.itemView.context, R.drawable.ic_baseline_cloud_off_24)!!.setTint(getColor(holder.itemView.context, R.color.CustomColor1))
                */

                holder.periodoButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                holder.periodoButtonAvailability.drawable.setTint(
                    getColor(
                        holder.itemView.context,
                        R.color.CustomColor1
                    )
                )
            }
        }


        /*
        var somma = 0

        val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))
        val sdf = SimpleDateFormat("dd MM yyyy", Locale.ITALY)
        cal.time = sdf.parse("5 07 2021") as Date // data inizio degli orari
        //TODO("Magari prendere la data di inizio da server in modo da non dover aggiornare l'app in caso di modifica")


        val p: Pattern = Pattern.compile("\\d+")
        val m: Matcher = p.matcher(currentPeriodo.periodo.nomePeriodo)

        while (m.find()) {
            somma += m.group().toInt()
        }

        val pattern = "dd/MM/yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.ITALY)

        cal.add(Calendar.WEEK_OF_YEAR, somma)
        val dateinizio: String = simpleDateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, 5)
        val datafine: String = simpleDateFormat.format(cal.time)

        holder.textViewPeriodo.text = "$dateinizio -> $datafine" */

        //imposto il titolo al textview del periodo
        DatePeriodoParsing.parseAndSetTitles(currentPeriodo, holder.textViewPeriodo)


        //holder.textViewPeriodo.text = currentPeriodo.periodo.nomePeriodo
    }

    fun getPeriodoAt(position: Int): PeriodoWithClasse {
        return getItem(position)
    }


    interface OnClickListenersPeriodoAdapter {
        fun onPeriodoScaricaButtonClick(periodo: PeriodoWithClasse, holder: PeriodoHolder)
        fun onPeriodoApriButtonClick(periodo: PeriodoWithClasse)
        fun onPeriodoCondividiOptionClick(periodo: PeriodoWithClasse)
        fun onPeriodoSalvaOptionClick(periodo: PeriodoWithClasse)
        fun onPeriodoEliminaOptionClick(periodo: PeriodoWithClasse)
        fun onPeriodoAvailabilityButtonClick(periodo: PeriodoWithClasse)
    }

    fun setOnClickListenersPeriodoAdapter(listenersPeriodoAdapter: OnClickListenersPeriodoAdapter) {
        this.listenersPeriodoAdapter = listenersPeriodoAdapter
    }

}