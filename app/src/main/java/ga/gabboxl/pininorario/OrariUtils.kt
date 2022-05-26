package ga.gabboxl.pininorario

import android.content.Context
import android.util.Base64
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class OrariUtils {
    companion object {
        var classi = arrayListOf<String>()
        var codiciclassi = arrayListOf<String>()
        var periodi = arrayListOf<String>()
        var griglie = arrayListOf<String>()
        private lateinit var listResources: JSONArray
        private var codiceclasse = ""

        suspend fun prendiClassi() {
            withContext(Default) {
                classi.clear()

                val apiResponse =
                    URL("https://gabboxlbot.altervista.org/pininorario/classi.php").readText()


                listResources =
                    JSONArray(Gson().fromJson(apiResponse, arrayListOf<String>().javaClass))

                var counter = 1 //inizio dal secondo elemento siccome il primo contiene il testo "Selezionate una classe"...
                while (listResources.length() - 1 >= counter) {
                    if (listResources.optJSONArray(counter).get(0).toString() == "grClasse") {
                        classi.add(listResources.optJSONArray(counter).get(1).toString())
                        codiciclassi.add(listResources.optJSONArray(counter).get(2).toString())
                    }
                    counter++
                    continue
                }
            }
        }

        suspend fun prendiPeriodi(posizclasse: Int) {
            withContext(Default) {

                val apiResponsePeriodi =
                    URL("https://gabboxlbot.altervista.org/pininorario/periodi.php").readText()


                    val jsonPeriodi =
                        JSONArray(
                            Gson().fromJson(
                                apiResponsePeriodi,
                                arrayListOf<String>().javaClass
                            )
                        )

                    periodi.clear()

                    var contatore = 0
                    while ((listResources.length() - 1) >= contatore) {
                        if (listResources.optJSONArray(contatore).get(1)
                                .toString() == classi[posizclasse]
                        ) {
                            codiceclasse = listResources.optJSONArray(contatore).get(2).toString()
                        }
                        contatore++
                    }      //fine while


                    var contatore2 = 0
                    griglie = arrayListOf()

                    while ((jsonPeriodi.length() - 1) > contatore2) {
                        if (jsonPeriodi.optJSONArray(contatore2).get(0)
                                .toString() == codiceclasse && !jsonPeriodi.optJSONArray(contatore2).get(1).toString().startsWith("Selezionate il periodo")
                        ) {
                            periodi.add(
                                jsonPeriodi.optJSONArray(contatore2).get(1).toString()
                            ) // aggiungo i periodi "EDT N." all'array
                            griglie.add(
                                jsonPeriodi.optJSONArray(contatore2).get(2).toString()
                            ) //aggiungo i link (semi-link) alle griglie all'array dichiarati ad inizio funzione del bottone
                        }
                        contatore2++
                    }


            }
        }


        suspend fun checkLogin(context: Context, username: String = "", password: String = ""): Boolean {
            return withContext(IO) {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val usernamefinal = if (username.isEmpty()) {
                    sharedPreferences.getString("pinin_username", "")
                } else {
                    username
                }

                val passwordfinal = if (password.isEmpty()) {
                    sharedPreferences.getString("pinin_password", "")
                } else {
                    password
                }


                val intranetUrl = URL("https://areariservata.itispininfarina.it/")

                val connection = intranetUrl.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val encoded: String =
                    Base64.encodeToString(
                        "$usernamefinal:$passwordfinal".toByteArray(),
                        Base64.DEFAULT
                    )

                connection.setRequestProperty("Authorization", "Basic $encoded")

                connection.connect()

                connection.responseCode == 200
            }
        }

        fun getUsername(context: Context?): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
            return sharedPreferences.getString("pinin_username", "")
        }

        fun getPassword(context: Context?): String? {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context!!)
            return sharedPreferences.getString("pinin_password", "")
        }
    }
}