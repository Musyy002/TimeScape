import android.app.Dialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timescape.R
import com.example.timescape.TimeCapsule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: TimeCapsuleAdapter
    private val timeCapsuleList: MutableList<TimeCapsule> = ArrayList()
    private val TAG = "MessageFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.rvTimeCapsules)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch Time Capsules from Firebase
        fetchTimeCapsules()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        checkAppLock()
    }

    private fun checkAppLock() {
        val savedPin = sharedPreferences.getString("app_lock_pin", null)

        if (savedPin != null) {
            showPinDialog(savedPin)
        }
    }

    private fun showPinDialog(savedPin: String) {
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
            val enteredPin = "${etPin1.text}${etPin2.text}${etPin3.text}${etPin4.text}"

            if (enteredPin == savedPin) {
                Toast.makeText(requireContext(), "PIN correct, welcome!", Toast.LENGTH_SHORT).show()
                pinDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Incorrect PIN, try again!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        pinDialog.show()
    }



    private fun fetchTimeCapsules() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("timeCapsules")
            .whereEqualTo("userId", currentUserUid)
            .get()
            .addOnSuccessListener { documents ->
                val capsules = documents.mapNotNull { it.toObject(TimeCapsule::class.java) }
                adapter = TimeCapsuleAdapter(requireContext(), capsules, currentUserUid)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching time capsules", e)
            }
    }
}
