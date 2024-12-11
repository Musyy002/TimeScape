package com.example.timescape

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var applock: TextView
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())


        applock = view.findViewById(R.id.applock)
        applock.setOnClickListener({
            val options = arrayOf("Set a PIN", "Disable App Lock")

            AlertDialog.Builder(requireContext())
                .setTitle("App Lock")
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> showSetPinDialog() // Show the Set PIN dialog
                        1 -> disableAppLock()    // Disable the App Lock
                    }
                }
                .show()

        })
    }

    private fun disableAppLock() {
        // Remove the saved PIN from SharedPreferences
        sharedPreferences.edit().remove("app_lock_pin").apply()
        Toast.makeText(requireContext(), "App lock disabled", Toast.LENGTH_SHORT).show()
    }

    private fun showSetPinDialog() {
        val pinDialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_set_pin, null)

        pinDialog.setContentView(view)
        pinDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etPin1 = view.findViewById<Button>(R.id.btnNum1)
        val etPin2 = view.findViewById<Button>(R.id.btnNum2)
        val etPin3 = view.findViewById<Button>(R.id.btnNum3)
        val etPin4 = view.findViewById<Button>(R.id.btnNum4)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmPin)

        btnConfirm.setOnClickListener {
            val pin = "${etPin1.text}${etPin2.text}${etPin3.text}${etPin4.text}"

            if (pin.length == 4) {
                // Save the PIN to SharedPreferences
                sharedPreferences.edit().putString("app_lock_pin", pin).apply()
                Toast.makeText(requireContext(), "PIN set successfully!", Toast.LENGTH_SHORT).show()
                pinDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid 4-digit PIN", Toast.LENGTH_SHORT).show()
            }
        }

        pinDialog.show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}