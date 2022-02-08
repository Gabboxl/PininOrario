package ga.gabboxl.pininorario

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import es.dmoral.toasty.Toasty
import ga.gabboxl.pininorario.databinding.LoginDialogLayoutBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class LoginDialog : DialogFragment() {
    private lateinit var binding: LoginDialogLayoutBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding = LoginDialogLayoutBinding.inflate(layoutInflater)

        binding.editUsername.setText(sharedPreferences.getString("pinin_username", ""))
        binding.editPassword.setText(sharedPreferences.getString("pinin_password", ""))


        val dialog = AlertDialog.Builder(context).setView(binding.root)
            .setTitle(getString(R.string.login))
            .setNegativeButton(getString(R.string.annulla_button)) { dialog, which ->

            }
            .setNeutralButton(getString(R.string.test_login_btn), null)
            .setPositiveButton(getString(R.string.salva)) { dialog, which ->
                sharedPreferences.edit().putString("pinin_username", binding.editUsername.text.toString()).apply()
                sharedPreferences.edit().putString("pinin_password", binding.editPassword.text.toString()).apply()
            }
            .show()

        dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
            lifecycleScope.launch(Main) {
                val textusername =
                    binding.editUsername.text.toString()
                val textpassword =
                    binding.editPassword.text.toString()
                if (OrariUtils.checkLogin(requireContext(), textusername, textpassword)) {
                    Toasty.success(requireContext(), getString(R.string.login_riuscito), Toasty.LENGTH_SHORT).show()
                 } else {
                    Toasty.error(requireContext(), getString(R.string.credenziali_errate_toast), Toasty.LENGTH_SHORT).show()
                 }
            }.invokeOnCompletion { cause: Throwable? -> println("debug1: sn mort perchè: $cause")  }
        }

        return dialog
    }
}