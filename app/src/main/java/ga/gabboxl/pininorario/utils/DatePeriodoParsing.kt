package ga.gabboxl.pininorario.utils

import android.widget.TextView
import ga.gabboxl.pininorario.PeriodoWithClasse
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DatePeriodoParsing {

    companion object{
        fun parseAndSetTitles(currentPeriodo: PeriodoWithClasse, textViewPeriodo: TextView){



            try {
                val patternnumeri = Regex("""[0-9]+""") //https://regex101.com/r/29Hof6/1
                val matchesnumerititoloperiodo = patternnumeri.findAll(currentPeriodo.periodo.titoloPeriodo)

                if(matchesnumerititoloperiodo.count() == 2) {
                    val patternDatetitoloperiodo =
                        Regex("""<<Nome>>\s-\s<<([^.]*)([^.]*)\s-\s([^.]*) ([^.]*)>>""") //https://regex101.com/r/N0AeGM/1
                    val gruppidatetitoloperiodo =
                        patternDatetitoloperiodo.find(currentPeriodo.periodo.titoloPeriodo)!!


                    val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))

                    val simpleDateFormat = SimpleDateFormat(
                        "dd MMMMM yyyy",
                        Locale.ITALY
                    ) //imposto la lingua italiana per i nomi dei mesi


                    fun data1(): String { //a volte il nome del mese i tizi che fanno i titoli dell'orario lo omettono se e' so stesso della datafinoal per cui utilizziamo il nome del mese della datafinoal.
                        if (gruppidatetitoloperiodo.groupValues[2].isBlank()) {
                            return "${gruppidatetitoloperiodo.groupValues[1]} ${gruppidatetitoloperiodo.groupValues[4]}"
                        }

                        return "${gruppidatetitoloperiodo.groupValues[1]} ${gruppidatetitoloperiodo.groupValues[2]}"
                    }

                    val data2 =
                        "${gruppidatetitoloperiodo.groupValues[3]} ${gruppidatetitoloperiodo.groupValues[4]}"


                    val dataDal = simpleDateFormat.parse(data1() + " " + cal.get(Calendar.YEAR))
                    val dataFinoal = simpleDateFormat.parse(data2 + " " + cal.get(Calendar.YEAR))


                    //applico il formato di destinazione che voglio per le date
                    val patternFinalesdf = "dd/MM/yyyy"
                    simpleDateFormat.applyPattern(patternFinalesdf)

                    val dataDalfixata = simpleDateFormat.format(dataDal!!)
                    val dataFinoalfixata = simpleDateFormat.format(dataFinoal!!)



                    textViewPeriodo.text = "$dataDalfixata -> $dataFinoalfixata"

                } else if (matchesnumerititoloperiodo.count() == 3) {
                    val giornoinizio = matchesnumerititoloperiodo.elementAt(0).value.toInt() //uso value al posto di groupValues[index] perche' tanto ogni match contiene solo un solo valore
                    val giornofinale = matchesnumerititoloperiodo.elementAt(1).value.toInt()
                    val mese = matchesnumerititoloperiodo.elementAt(2).value.toInt()

                    //val cal: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"))

                    //queste classi LocalDate e DateTimeFormatter sono piu' recenti rispetto a Calendar e simpledateformat, per cui e' meglio utilizzare queste d'ora in avanti. richiedono anche meno righe di codice quindi tanta roba
                    val datainizio = LocalDate.of(LocalDate.now().year, mese, giornoinizio).format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    val datafine = LocalDate.of(LocalDate.now().year, mese, giornofinale).format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"))


                    textViewPeriodo.text = "$datainizio -> $datafine"
                } else {
                    //lancio un'eccezione perche' altrimenti non entra nel catch per eseguire il regex di fallback
                    throw Exception()
                }

            } catch (e: Exception){ //regex titolo di fallback in caso dovesse fallire il regex quello bello

                try {
                    val patternTitoloFallback =
                        Regex(""".*<<(.*)>>""") //https://regex101.com/r/3mhZ8O/1  ///// si potrebbe utilizzare anche questo pero' non si sa mai - https://regex101.com/r/3mhZ8O/1
                    val gruppidatetitoloperiodofallback =
                        patternTitoloFallback.find(currentPeriodo.periodo.titoloPeriodo)!!

                    //funzione inline per capitalizzare ogni parola
                    //fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

                    textViewPeriodo.text = gruppidatetitoloperiodofallback.groupValues[1].lowercase().capitalize()


                } catch (e: Exception) { //in caso facciano gli infami stravolgendo il pattern almeno siamo a posto

                    textViewPeriodo.text = currentPeriodo.periodo.titoloPeriodo
                }
            }

        }
    }
}