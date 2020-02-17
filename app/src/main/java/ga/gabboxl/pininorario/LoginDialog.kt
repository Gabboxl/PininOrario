package ga.gabboxl.pininorario

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty

class LoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val inflater: LayoutInflater = activity!!.layoutInflater
        val viewLayoutDialog: View = inflater.inflate(R.layout.login_dialog_layout, null)

        try {
            viewLayoutDialog.findViewById<EditText>(R.id.edit_username).setText(sharedPreferences.getString("pinin_username", ""))
            viewLayoutDialog.findViewById<EditText>(R.id.edit_password).setText(sharedPreferences.getString("pinin_password", ""))
        } catch (e: NullPointerException){ }

        val dialog = AlertDialog.Builder(activity).setView(viewLayoutDialog)
            .setTitle(getString(R.string.login_dialog_title))
            .setNegativeButton(getString(R.string.annulla_button)) { dialog, which ->

            }
            .setNeutralButton(getString(R.string.test_login_button), null)
            .setPositiveButton(getString(R.string.salva_button)) { dialog, which ->
                sharedPreferences.edit().putString("pinin_username", viewLayoutDialog.findViewById<EditText>(R.id.edit_username).text.toString()).apply()
                sharedPreferences.edit().putString("pinin_password", viewLayoutDialog.findViewById<EditText>(R.id.edit_password).text.toString()).apply()
            }
            .show()

        dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
            val textusername = viewLayoutDialog.findViewById<EditText>(R.id.edit_username).text.toString()
            val textpassword = viewLayoutDialog.findViewById<EditText>(R.id.edit_password).text.toString()
            if(OrariUtils.checkLogin(context, textusername, textpassword)){
            Toasty.success(context!!, getString(R.string.login_riuscito_toast), Toasty.LENGTH_SHORT).show()
        } else{
            Toasty.error(context!!, getString(R.string.credenziali_errate_toast), Toasty.LENGTH_SHORT).show()
        }
        }

        return dialog
    }
}