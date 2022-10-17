package ga.gabboxl.pininorario.adapters

import android.content.res.Resources.Theme
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
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
import ga.gabboxl.pininorario.interfacesimpls.OnClickAdaptersImplementations
import java.text.SimpleDateFormat
import java.util.*


class PeriodoDownloadsAdapter : ListAdapter<PeriodoWithClasse, PeriodoDownloadsAdapter.PeriodoHolder>(DIFF_CALLBACK) {

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
                return oldItem.periodo.nomePeriodo == newItem.periodo.nomePeriodo
            }
        }
    }


    inner class PeriodoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewNomePeriodo: TextView
        var textViewNomeClasse: TextView
        var apriButton: Button
        var periodoButtonAvailability: ImageButton
        val optionPeriodoButton: ImageButton
        val popupperiodo: PopupMenu

        init {
            textViewNomePeriodo = itemView.findViewById(R.id.text_NomePeriodoDownloads)
            textViewNomeClasse = itemView.findViewById(R.id.text_nomeClassePeridoDownloads)
            apriButton = itemView.findViewById(R.id.card_periodoapri)
            optionPeriodoButton = itemView.findViewById(R.id.periodoButtonOpzioni)
            periodoButtonAvailability = itemView.findViewById(R.id.periodoButtonAvailability)

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
            LayoutInflater.from(parent.context).inflate(R.layout.standaloneperiododonwloaded_card_item, parent, false)
        return PeriodoHolder((itemView))
    }

    override fun onBindViewHolder(holder: PeriodoHolder, position: Int) {
        val currentPeriodo: PeriodoWithClasse = getItem(position)

        holder.apriButton.isVisible = currentPeriodo.periodo.isDownloaded
        holder.optionPeriodoButton.isVisible = currentPeriodo.periodo.isDownloaded


        ConnectivityUtils.isInternetAvailable.observe(holder.itemView.context as LifecycleOwner) { isConnected ->
            if (isConnected) {
                if (currentPeriodo.periodo.isAvailableOnServer) {

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


        try {
            val patternDatetitoloperiodo = Regex("""<<Nome>>\s-\s<<([^.]*)([^.]*)\s-\s([^.]*) ([^.]*)>>""") //https://regex101.com/r/N0AeGM/1
            val gruppidatetitoloperiodo = patternDatetitoloperiodo.find(currentPeriodo.periodo.titoloPeriodo)!!


            val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))

            val simpleDateFormat = SimpleDateFormat("dd MMMMM yyyy", Locale.ITALY) //imposto la lingua italiana per i nomi dei mesi


            fun data1(): String { //a volte il nome del mese i tizi che fanno i titoli dell'orario lo omettono se e' so stesso della datafinoal per cui utilizziamo il nome del mese della datafinoal.
                if(gruppidatetitoloperiodo.groupValues[2].isBlank()){
                    return "${gruppidatetitoloperiodo.groupValues[1]} ${gruppidatetitoloperiodo.groupValues[4]}"
                }

                return "${gruppidatetitoloperiodo.groupValues[1]} ${gruppidatetitoloperiodo.groupValues[2]}"
            }

            val data2 = "${gruppidatetitoloperiodo.groupValues[3]} ${gruppidatetitoloperiodo.groupValues[4]}"


            val dataDal = simpleDateFormat.parse(data1() + " " + cal.get(Calendar.YEAR))
            val dataFinoal = simpleDateFormat.parse(data2 + " " + cal.get(Calendar.YEAR))


            //applico il formato di destinazione che voglio per le date
            val patternFinalesdf = "dd/MM/yyyy"
            simpleDateFormat.applyPattern(patternFinalesdf)

            val dataDalfixata = simpleDateFormat.format(dataDal!!)
            val dataFinoalfixata = simpleDateFormat.format(dataFinoal!!)

            holder.textViewNomePeriodo.text = "$dataDalfixata -> $dataFinoalfixata"

        } catch (e: Exception){ //regex titolo di fallback in caso dovesse fallire il regex quello bello

            val patternTitoloFallback =
                Regex(""".*<<(.*)>>""") //https://regex101.com/r/3mhZ8O/1  ///// si potrebbe utilizzare anche questo pero' non si sa mai - https://regex101.com/r/3mhZ8O/1
            val gruppidatetitoloperiodofallback =
                patternTitoloFallback.find(currentPeriodo.periodo.titoloPeriodo)!!

            holder.textViewNomePeriodo.text = gruppidatetitoloperiodofallback.groupValues[1]
        }

        //textview nome classe
        holder.textViewNomeClasse.text = "[" + currentPeriodo.classe.nomeClasse + "]"
    }

    fun getPeriodoAt(position: Int): PeriodoWithClasse {
        return getItem(position)
    }


    interface OnClickListenersPeriodoAdapter: PeriodoAdapter.OnClickListenersPeriodoAdapter

    fun setOnClickListenersPeriodoDownloadsAdapter(listenersPeriodoAdapter: OnClickAdaptersImplementations) {
        this.listenersPeriodoAdapter = listenersPeriodoAdapter
    }

}