package ga.gabboxl.pininorario

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import es.dmoral.toasty.Toasty


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsframe, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // or finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey)

            val listprefshortcut = findPreference<ListPreference>("shortclassi_pref")!!
            val modifyloginpref = findPreference<Preference>("modify_login")!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

                val classiarray = OrariUtils.classi
                val entryvalues = arrayListOf<String>()
                var cont = 1
                while (classiarray.size >= cont) {
                    entryvalues.add(cont.toString())
                    cont++
                }
                listprefshortcut.entries = classiarray.toTypedArray()
                listprefshortcut.entryValues = entryvalues.toTypedArray()


                if (listprefshortcut.entry != null) {
                    listprefshortcut.summary = "%s"
                }

            } else {
                listprefshortcut.isEnabled = false
                listprefshortcut.summary = "Versione di Android non compatibile"
            }

            listprefshortcut.setOnPreferenceChangeListener { preference, newValue ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    val nomeclasseselez = OrariUtils.classi[newValue.toString().toInt() - 1]

                    Toasty.success(
                        this.context!!,
                        newValue.toString(),
                        Toasty.LENGTH_SHORT
                    ).show()


                    val shortcutManager =
                        getSystemService<ShortcutManager>(
                            this.context!!,
                            ShortcutManager::class.java
                        )
                    val shortcut = ShortcutInfo.Builder(context, "shortclasse1")
                        .setShortLabel(nomeclasseselez)
                        .setLongLabel(nomeclasseselez)
                        .setIcon(Icon.createWithResource(context, R.drawable.ic_time))
                        //.setIntent(Intent().setAction(Intent.EXTRA_TEXT).setClass(context!!, MainActivity::class.java).putExtra("classe", OrariUtils.listResources.optJSONArray(listprefshortcut.value.toInt() -1).get(2).toString()))
                        .setIntent(
                            Intent().setAction(Intent.EXTRA_TEXT).setClass(
                                context!!,
                                MainActivity::class.java
                            ).putExtra(
                                "classe",
                                nomeclasseselez
                            )
                        )
                        .build()

                    shortcutManager!!.dynamicShortcuts = listOf(shortcut)

                    listprefshortcut.summary = "%s"
                }


                true
            }



            modifyloginpref.setOnPreferenceClickListener {
                openDialog()
                true
            }


        }


        private fun openDialog(){
            val dialog = LoginDialog()
            dialog.show(activity!!.supportFragmentManager, "login_dialog")
        }
    }
}