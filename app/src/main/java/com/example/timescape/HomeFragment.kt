import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.timescape.AccountsFragment
import com.example.timescape.ConfirmationActivity
import com.example.timescape.R
import com.example.timescape.TimeCapsule
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var btnSaveCapsule: MaterialButton
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSelectVideo: MaterialButton

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private val IMAGE_REQUEST_CODE = 1001
    private val VIDEO_REQUEST_CODE = 1002

    private val calendar = Calendar.getInstance()


    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val toolbar: MaterialToolbar = view.findViewById(R.id.materialToolbar)

        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)


        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etDate = view.findViewById(R.id.etDate)
        btnSaveCapsule = view.findViewById(R.id.btnSaveCapsule)
        btnSelectImage = view.findViewById(R.id.selectImg)
        btnSelectVideo = view.findViewById(R.id.selectVid)

        etDate.setOnClickListener {
            showDateTimePicker()
        }

        btnSelectImage.setOnClickListener {
            selectImage()
        }

        btnSelectVideo.setOnClickListener {
            selectVideo()
        }

        btnSaveCapsule.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val targetDate = getTargetDate()
            val contents = listOf<String>() // Replace with actual content URLs as needed

            if (title.isEmpty() || description.isEmpty() || targetDate == null) {
                Toast.makeText(requireContext(), "Please fill in all fields and select a valid date.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid

            val timeCapsule = TimeCapsule(userId.toString(), title, description, Timestamp(targetDate), contents, createdAt = Timestamp.now())

            firestore.collection("timeCapsules")
                .add(timeCapsule)
                .addOnSuccessListener {
                    val intent = Intent(requireContext(), ConfirmationActivity::class.java)
                    startActivity(intent)
                    // Optionally, navigate to another fragment or clear input fields
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error saving time capsule: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        startActivityForResult(intent, VIDEO_REQUEST_CODE)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> {
                    imageUri = data?.data
                    imageUri?.let { uri ->
                        Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show()
                        val transaction = fragmentManager?.beginTransaction()

                        val selectImagesFragment = ImageFragment.newInstance(arrayListOf(uri.toString()))

                        val layoutTitle = view?.findViewById<TextInputLayout>(R.id.inputLayoutTitle)
                        val layoutDes = view?.findViewById<TextInputLayout>(R.id.inputLayoutDescription)
                        val layoutDate = view?.findViewById<TextInputLayout>(R.id.inputLayoutDate)

                        layoutTitle?.visibility = View.GONE
                        layoutDes?.visibility = View.GONE
                        layoutDate?.visibility = View.GONE
                        btnSaveCapsule.visibility = View.GONE
                        btnSelectImage.visibility = View.GONE
                        btnSelectVideo.visibility = View.GONE

                        transaction?.replace(R.id.homefrag, selectImagesFragment)
                        transaction?.addToBackStack(null)
                        transaction?.commit()
                    }
                }
                VIDEO_REQUEST_CODE -> {
                    videoUri = data?.data
                    videoUri?.let { uri ->
                        Toast.makeText(requireContext(), "Video selected", Toast.LENGTH_SHORT).show()
                        val transaction = fragmentManager?.beginTransaction()

                        val selectVideosFragment = VideoFragment.newInstance(arrayListOf(uri.toString()))

                        val layoutTitle = view?.findViewById<TextInputLayout>(R.id.inputLayoutTitle)
                        val layoutDes = view?.findViewById<TextInputLayout>(R.id.inputLayoutDescription)
                        val layoutDate = view?.findViewById<TextInputLayout>(R.id.inputLayoutDate)

                        layoutTitle?.visibility = View.GONE
                        layoutDes?.visibility = View.GONE
                        layoutDate?.visibility = View.GONE
                        btnSaveCapsule.visibility = View.GONE
                        btnSelectImage.visibility = View.GONE
                        btnSelectVideo.visibility = View.GONE

                        transaction?.replace(R.id.homefrag, selectVideosFragment)
                        transaction?.addToBackStack(null)
                        transaction?.commit()
                    }
                }
            }
        }
    }



    private fun showDateTimePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val dateTime = calendar.time
                etDate.setText(dateTime.toString()) // Format as needed
            }
            TimePickerDialog(requireContext(), timeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(requireContext(), dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun getTargetDate(): Date? {
        return calendar.time.takeIf { it.after(Date()) } // Ensure the date is in the future
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireParentFragment().requireFragmentManager().beginTransaction()
        transaction.replace(R.id.container_frag,fragment)
        transaction.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu,menu)
        super.onCreateOptionsMenu(menu,inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.accnt ->{
                loadFragment(AccountsFragment())
                true
            }
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
