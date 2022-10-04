package ga.gabboxl.pininorario.adapters


import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ga.gabboxl.pininorario.ClasseWithPeriodi
import ga.gabboxl.pininorario.ConnectivityUtils
import ga.gabboxl.pininorario.R


class ClasseAdapter :
    ListAdapter<ClasseWithPeriodi, ClasseAdapter.ClasseHolder>(DIFF_CALLBACK) {
    private lateinit var listenersClasseAdapter: OnClickListenersClasseAdapter
    private lateinit var listenersPeriodoAdapter: PeriodoAdapter.OnClickListenersPeriodoAdapter
    var posizioneitem: Int = -1

    companion object {

        private var DIFF_CALLBACK: DiffUtil.ItemCallback<ClasseWithPeriodi> = object :
            DiffUtil.ItemCallback<ClasseWithPeriodi>() {
            override fun areItemsTheSame(
                oldItem: ClasseWithPeriodi,
                newItem: ClasseWithPeriodi
            ): Boolean {
                return oldItem.classe.id == newItem.classe.id
            }

            override fun areContentsTheSame(
                oldItem: ClasseWithPeriodi,
                newItem: ClasseWithPeriodi
            ): Boolean {
                return oldItem.classe == newItem.classe &&
                        oldItem.classe.codiceClasse == newItem.classe.codiceClasse && oldItem.classe.isPinned == newItem.classe.isPinned &&
                        oldItem.classe.isAvailableOnServer == newItem.classe.isAvailableOnServer && oldItem.periodi == newItem.periodi //questo check dei periodi controlla che la lista dei periodi a cui sono associati alla classe non siano cambiati, altrimenti aggiorna la lsita dei periodi
            }
        }
    }

    inner class ClasseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView
        var textViewNomeClasse: TextView
        var optionButton: ImageButton
        var recyclerViewPeriodi: RecyclerView
        val popupclasse: PopupMenu
        val classeButtonAvailability: ImageButton


        init {
            textViewTitle = itemView.findViewById(R.id.text_view_title)
            textViewNomeClasse = itemView.findViewById(R.id.text_view_nomeclasse)
            optionButton = itemView.findViewById(R.id.cardoptionbutton)
            recyclerViewPeriodi = itemView.findViewById(R.id.recyclerview_periodi)
            classeButtonAvailability = itemView.findViewById(R.id.classeButtonAvailability)


            classeButtonAvailability.setOnClickListener{
                posizioneitem = absoluteAdapterPosition
                listenersClasseAdapter.onClasseAvailabilityButtonClick(getItem(posizioneitem), this)
            }


            //la creazione del popup meglio lasciarla fuori dall'onclick listener del pulsante opzioni, altrimenti il codice della creazione verrebbe eseguito ogni volta premuto il pulsante. Avviamo soltanto la visualizzazaione piuttosto con il metodo .show()
            popupclasse = PopupMenu(itemView.context, optionButton)
            popupclasse.menuInflater.inflate(R.menu.cardclasse_menu, popupclasse.menu)

            //that's the workaround, it's not ideal/official but it works
            val pop = PopupMenu::class.java.getDeclaredField("mPopup")
            pop.isAccessible = true
            val menupop = pop.get(popupclasse)
            menupop.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(menupop, true)


            popupclasse.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    //R.id.movecardmenuoption -> {}

                    R.id.addcardmenuoption -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                            listenersClasseAdapter.onAggiungiPrefClick(getItem(posizioneitem))
                        }
                    }

                    R.id.deletecardmenuoption -> {
                        posizioneitem = absoluteAdapterPosition
                        if (posizioneitem != RecyclerView.NO_POSITION) {
                            listenersClasseAdapter.onRimuoviPrefClick(getItem(posizioneitem))
                        }
                    }
                }

                true
            }

            //i setonclicklistener si devono implementeare nel viewholder e non nel onbind perche altrimenti verrebbe l'onbind chiamato ogni volta che il recyclerview deve visualizzare un nuovo elemento scorrendo verso il basso/alto, implementandolo nel viewholder viene implementato una sola volta per item
            optionButton.setOnClickListener {

                popupclasse.show() //showing popup menu
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

        //imposto la visibilita' del pulsante opzione rimuovi/aggiungi ai preferiti in base alla situazione della classe attuale
        holder.popupclasse.menu.findItem(R.id.addcardmenuoption).isVisible = !currentClasse.classe.isPinned //inverto il valore
        holder.popupclasse.menu.findItem(R.id.deletecardmenuoption).isVisible = currentClasse.classe.isPinned


        ConnectivityUtils.isInternetAvailable.observe(holder.itemView.context as LifecycleOwner) { isConnected ->
            if (isConnected) {
                if (currentClasse.classe.isAvailableOnServer) {

                    //getDrawable(holder.itemView.context, R.drawable.ic_baseline_cloud_queue_24)!!.setTint(getColor(holder.itemView.context, R.color.md_theme_dark_primary))

                    val unwrappedDrawable = AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_baseline_cloud_queue_24
                    )

                    val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)


                    val typedValue = TypedValue()
                    val theme: Resources.Theme = holder.itemView.context.theme
                    theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                    val color: Int = typedValue.data

                    DrawableCompat.setTint(wrappedDrawable,color)


                    holder.classeButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_queue_24)
                } else {
                    //TODO("Magari colorare la scheda di colore rosso se non [ disponibile?")

           /*         AppCompatResources.getDrawable(
                        holder.itemView.context,
                        R.drawable.ic_baseline_cloud_off_24
                    )!!.setTint(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.md_theme_light_error
                        )
                    )*/

                    holder.classeButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                    holder.classeButtonAvailability.drawable.setTint(ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.md_theme_light_error
                    ))
                }

            } else {
                /*
                AppCompatResources.getDrawable(
                    holder.itemView.context,
                    R.drawable.ic_baseline_cloud_off_24
                )!!.setTint(ContextCompat.getColor(holder.itemView.context, R.color.CustomColor1)) */

                holder.classeButtonAvailability.setImageResource(R.drawable.ic_baseline_cloud_off_24)
                holder.classeButtonAvailability.drawable.setTint(ContextCompat.getColor(holder.itemView.context, R.color.CustomColor1))
            }
        }



        val periodiadapter = PeriodoAdapter()
        holder.recyclerViewPeriodi.adapter = periodiadapter

        periodiadapter.submitList(currentClasse.periodi)

        //il context per il recyclerview dei periodi nestato lo prendo da holder.itemView.context
        periodiadapter.setOnClickListenersPeriodoAdapter(listenersPeriodoAdapter)

    }

    fun getClasseAt(position: Int): ClasseWithPeriodi {
        return getItem(position)
    }


    //pulsante rimuovi dai preferiti
    interface OnClickListenersClasseAdapter {
        fun onRimuoviPrefClick(classeWithPeriodi: ClasseWithPeriodi)
        fun onAggiungiPrefClick(classeWithPeriodi: ClasseWithPeriodi)
        fun onClasseAvailabilityButtonClick(classeWithPeriodi: ClasseWithPeriodi, holder: ClasseAdapter.ClasseHolder)
    }

    fun setOnClickListenersClasseAdapter(listenersClasseAdapter: OnClickListenersClasseAdapter) {
        this.listenersClasseAdapter = listenersClasseAdapter
    }


    //listeners x periodi

    fun setOnClickListenersPeriodoAdapter(listenersPeriodoAdapter: PeriodoAdapter.OnClickListenersPeriodoAdapter) {
        this.listenersPeriodoAdapter = listenersPeriodoAdapter
    }


}