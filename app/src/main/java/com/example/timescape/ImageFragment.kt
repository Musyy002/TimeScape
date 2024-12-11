// SelectImagesFragment.kt

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.timescape.ConfirmationActivity
import com.example.timescape.ImageAdapter
import com.example.timescape.R
import com.example.timescape.TimeCapsule
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date
import java.util.UUID

class ImageFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var etImageTitle: TextInputEditText
    private lateinit var etImageDescription: TextInputEditText
    private lateinit var etTargetDate: TextInputEditText
    private lateinit var rvSelectedImages: RecyclerView
    private lateinit var btnSaveImages: MaterialButton

    private val calendar = Calendar.getInstance()
    private lateinit var selectedImages: List<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        etImageTitle = view.findViewById(R.id.etImageTitle)
        etImageDescription = view.findViewById(R.id.etImageDescription)
        etTargetDate = view.findViewById(R.id.etTargetDate)
        rvSelectedImages = view.findViewById(R.id.rvSelectedImages)
        btnSaveImages = view.findViewById(R.id.btnSaveImages)

        etTargetDate.setOnClickListener {
            showDateTimePicker()
        }

        rvSelectedImages = view.findViewById(R.id.rvSelectedImages)

        selectedImages = arguments?.getStringArrayList("selectedImages")?.map { Uri.parse(it) } ?: emptyList()

        rvSelectedImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSelectedImages.adapter = ImageAdapter(selectedImages)



        btnSaveImages.setOnClickListener {
            saveImagesToFirebase()
        }

        return view
    }

    private fun showDateTimePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val dateTime = calendar.time
                etTargetDate.setText(dateTime.toString()) // Format as needed
            }
            TimePickerDialog(requireContext(), timeListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(requireContext(), dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveImagesToFirebase() {
        val title = etImageTitle.text.toString().trim()
        val description = etImageDescription.text.toString().trim()
        val targetDate = calendar.time.takeIf { it.after(Date()) } // Ensure the date is in the future

        if (title.isEmpty() || description.isEmpty() || targetDate == null) {
            Toast.makeText(requireContext(), "Please fill in all fields and select a valid date.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = storage.reference

        for (imageUri in selectedImages) {
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
            imageRef.putFile(imageUri).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val imageUrl = downloadUrl.toString()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val timeCapsule = TimeCapsule(userId.toString(),title, description, Timestamp(targetDate), listOf(imageUrl))
                    firestore.collection("timeCapsules")
                        .add(timeCapsule)
                        .addOnSuccessListener {
//                            Toast.makeText(requireContext(), "Image saved successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(),ConfirmationActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(selectedImages: ArrayList<String>): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putStringArrayList("selectedImages", selectedImages)
            fragment.arguments = args
            return fragment
        }
    }
}
