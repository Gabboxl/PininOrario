package ga.gabboxl.pininorario

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import android.os.StrictMode
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class OrariUtils {
    var periodi = arrayListOf<String>()
    var griglie = arrayListOf<String>()
    private lateinit var listResources: JSONArray
    public var classi = arrayListOf<String>()
    private var codiceclasse = ""

    fun OrariUtils(context: Context){

        
    }


    fun scaricaPeriodi(classe: Int = 0) {

        val apiResponsePeriodi =
            URL("https://gabboxlbot.altervista.org/pininorario/periodi.php").readText()
        val jsonPeriodi =
            JSONArray(Gson().fromJson(apiResponsePeriodi, arrayListOf<String>().javaClass))

        Activity().runOnUiThread {
            periodi.clear()

            var contatore = 0
            while ((listResources.length() - 1) >= contatore) {
                if (listResources.optJSONArray(contatore).get(1).toString() == classi[classe]) {
                    codiceclasse = listResources.optJSONArray(contatore).get(2).toString()
                }
                contatore++
            }      //fine while

            var contatore2 = 0
            griglie = arrayListOf()

            while ((jsonPeriodi.length() - 1) > contatore2) {
                if (jsonPeriodi.optJSONArray(contatore2).get(0).toString() == codiceclasse) {
                    periodi.add(jsonPeriodi.optJSONArray(contatore2).get(1).toString()) // aggiungo i periodi "EDT N." all'array
                    griglie.add(jsonPeriodi.optJSONArray(contatore2).get(2).toString()) //aggiungo i link (semi-link) alle griglie all'array dichiarati ad inizio funzione del bottone
                }
                contatore2++
            }
        }

    }


    fun prendiOrario(yay: String) {
        doAsync {

            classi.clear()

            /*val url = "https://intranet.itispininfarina.it/orario/_ressource.js"
            FileUtils.copyURLToFile(URL(url), File("/data/user/0/ga.gabboxl.pininorario/cache/classi.js"))*/

            var apiResponse =
                URL("https://gabboxlbot.altervista.org/pininorario/classi.php").readText()


            listResources = JSONArray(Gson().fromJson(yay, arrayListOf<String>().javaClass))

            var counter = 0
            while (90 >= counter) {
                if (listResources.optJSONArray(counter).get(0).toString() == "grClasse") {
                    classi.add(listResources.optJSONArray(counter).get(1).toString())
                }
                Log.e("asd", listResources.length().toString())
                counter++
                continue
            }


        }
    }
}