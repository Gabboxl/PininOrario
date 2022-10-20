package ga.gabboxl.pininorario

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.elevation.SurfaceColors


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsframe, SettingsFragment())
            .commit()

        setSupportActionBar(findViewById(R.id.mainCustomBarraTop))
        supportActionBar?.title = this.title

        WindowCompat.setDecorFitsSystemWindows(window, false)
        //colori appbarra
        val color = SurfaceColors.SURFACE_2.getColor(this)
        findViewById<AppBarLayout>(R.id.mainCustomBarLayout).setBackgroundColor(color)

        //tolgo il textview della data dell'ultimo aggiornamento
        findViewById<TextView>(R.id.textAggiornamentoAppBar).visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // or finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey)

            val editserverlinkpref = findPreference<Preference>("edit_server_link")!!

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            editserverlinkpref.summary = sharedPreferences.getString("server_link", "")


            editserverlinkpref.setOnPreferenceClickListener {
                openDialog()
                true
            }
        }


        private fun openDialog(){
            val dialog = ServerLinkDialog()
            dialog.show(requireActivity().supportFragmentManager, "serverlink_dialog")
        }
    }
}