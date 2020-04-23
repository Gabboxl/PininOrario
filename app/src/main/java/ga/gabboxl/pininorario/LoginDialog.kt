package ga.gabboxl.pininorario

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class LoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val inflater: LayoutInflater = requireActivity().layoutInflater
        val viewLayoutDialog: View = inflater.inflate(R.layout.login_dialog_layout, null)

        viewLayoutDialog.findViewById<EditText>(R.id.edit_username).setText(sharedPreferences.getString("pinin_username", ""))
        viewLayoutDialog.findViewById<EditText>(R.id.edit_password).setText(sharedPreferences.getString("pinin_password", ""))


        val dialog = AlertDialog.Builder(context).setView(viewLayoutDialog)
            .setTitle(getString(R.string.login))
            .setNegativeButton(getString(R.string.annulla_button)) { dialog, which ->

            }
            .setNeutralButton(getString(R.string.test_login_btn), null)
            .setPositiveButton(getString(R.string.salva)) { dialog, which ->
                sharedPreferences.edit().putString("pinin_username", viewLayoutDialog.findViewById<EditText>(R.id.edit_username).text.toString()).apply()
                sharedPreferences.edit().putString("pinin_password", viewLayoutDialog.findViewById<EditText>(R.id.edit_password).text.toString()).apply()
            }
            .show()

        dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
            lifecycleScope.launch(Main) {
                val textusername =
                    viewLayoutDialog.findViewById<EditText>(R.id.edit_username).text.toString()
                val textpassword =
                    viewLayoutDialog.findViewById<EditText>(R.id.edit_password).text.toString()
                if (OrariUtils.checkLogin(context, textusername, textpassword)) {
                    Toasty.success(requireContext(), getString(R.string.login_riuscito), Toasty.LENGTH_SHORT).show()
                 } else {
                    Toasty.error(requireContext(), getString(R.string.credenziali_errate_toast), Toasty.LENGTH_SHORT).show()
                 }
            }.invokeOnCompletion { cause: Throwable? -> println("debug1: sn mort perchè: $cause")  }
        }

        return dialog
    }
}