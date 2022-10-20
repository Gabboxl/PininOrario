package ga.gabboxl.pininorario


import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import es.dmoral.toasty.Toasty
import ga.gabboxl.pininorario.databinding.EditserverlinkAlertdialogBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ServerLinkDialog : DialogFragment() {
    private lateinit var binding: EditserverlinkAlertdialogBinding
    private lateinit var classeViewModel: ClasseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding = EditserverlinkAlertdialogBinding.inflate(layoutInflater)
        classeViewModel = ViewModelProvider(this)[ClasseViewModel::class.java]

        binding.edittextServerlink.setText(sharedPreferences.getString("server_link", ""))


        val inflater = layoutInflater
        val viewtitolo: View = inflater.inflate(R.layout.serverlinkdialog_title_layout, null)

        //imposto il titolo dell'alert
        viewtitolo.findViewById<TextView>(R.id.textview_titolo_editserveralert).text = "Modifica link server orari"


        val dialog = MaterialAlertDialogBuilder(requireContext()).setView(binding.root)
            //.setTitle("Modifica link server orari")
            .setCustomTitle(viewtitolo)
            .setNegativeButton(getString(R.string.annulla)) { dialog, which ->
            }
            //.setNeutralButton("Controlla", null)
            .setPositiveButton(getString(R.string.salva), null) //imposto il listener di default del pulsante su null siccome piu' in basso nel codice ho fatto un override di esso con una mia funzione specifica in modo che il dialog non sparisca al cliccare del pulsante, cosa che succede con il listener di default
            .show()

        //questa funzione permette di prevenire il dismiss del dialog con il pulsante indietro della navbar di sistema
        isCancelable = false //si usa setCancelable() NON sul dialog, ma si usa nella classe perche' essa estende DialogFragment. e dobbiamo dire che il dialogfragment non e' cancellabile

        //cosi' non sparisce se si clicca al di fuori del dialog
        dialog.setCanceledOnTouchOutside(false)


        //imposto un mio comportamento personalizzato per il pulsante positivo in modo che il dialog non sparisca al cliccare di esso
        dialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener { it as Button //imposto il tipo del parametro di default "it" come buttoon, siccome e' considerato solo view per qualche motivo
            lifecycleScope.launch(Dispatchers.IO){
                val pulsantenegativodialog = dialog.getButton(Dialog.BUTTON_NEGATIVE)
                val progressbar = viewtitolo.findViewById<LinearProgressIndicator>(R.id.progressBar_editserveralert)

                withContext(Main) {
                    progressbar.visibility = View.VISIBLE
                    it.isEnabled = false //disabilito anche il pulsante positivo
                    pulsantenegativodialog.isEnabled = false //e anche quello negativo nel dubbio
                    it.text = "Controllo..."
                }

                //controllo che il link inserito sia diverso da quello gia' salvato o meno
                if(binding.edittextServerlink.text.toString() != sharedPreferences.getString("server_link", "")) {

                    if (binding.edittextServerlink.text.toString()
                            .isNotBlank() && !URLUtil.isValidUrl(binding.edittextServerlink.text.toString())
                    ) {
                        withContext(Main) {
                            Toasty.error(
                                requireContext(),
                                "Url inserito non valido.",
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    } else {

                        withContext(Main) {
                            val contextAcaso = requireContext()

                            val cambiareServerConfermaDialog =
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Cambiare server?")
                                    .setMessage(
                                        "Cambiare il server richiede la reinizializzazione del database interno contenente classi e periodi. \n Procedendo il database interno verra' reinizializzato perdendo i preferiti ecc."
                                    )
                                    .setPositiveButton("Procedi") { _, _ ->

                                        lifecycleScope.launch(IO) {
                                            //imposto il server nuovo
                                            sharedPreferences.edit().putString(
                                                "server_link",
                                                binding.edittextServerlink.text.toString()
                                            ).apply()

                                            //elimino tutti i contenuti delle tabelle del database
                                            ClasseDatabase.getInstance(contextAcaso)
                                                .clearAllTables()

                                            withContext(Main) {
                                                Toasty.success(
                                                    contextAcaso,
                                                    "Database reinizializzato.",
                                                    Toasty.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        dismiss() //dismisso anche il dialog di modifica del server al di sotto di questo
                                    }
                                    .setNegativeButton("No") { _, _ ->

                                    }
                            cambiareServerConfermaDialog.create().show()
                        }

                        //dismiss() //fine dialog
                    }
                } else {
                    dismiss()
                }

                withContext(Main){ //rimetto a posto tutto
                    progressbar.visibility = View.INVISIBLE
                    it.isEnabled = true
                    pulsantenegativodialog.isEnabled = true
                    it.text = "Salva"
                }
            }


            /* //old codice
            lifecycleScope.launch(Main) {
                Toast.makeText(requireContext(), "yolo", Toast.LENGTH_SHORT).show()
            }.invokeOnCompletion { cause: Throwable? -> println("debug1: sn mort perchè: $cause")  } */
        }

        return dialog
    }
}