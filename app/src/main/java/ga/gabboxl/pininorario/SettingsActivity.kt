package ga.gabboxl.pininorario

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.elevation.SurfaceColors
import es.dmoral.toasty.Toasty


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
                onBackPressed() // or finish();
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey)



        }

    }
}