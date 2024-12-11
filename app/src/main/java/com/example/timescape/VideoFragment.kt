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
import com.example.timescape.R
import com.example.timescape.TimeCapsule
import com.example.timescape.VideoAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class VideoFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var etVideoTitle: TextInputEditText
    private lateinit var etVideoDescription: TextInputEditText
    private lateinit var etTargetDate: TextInputEditText
    private lateinit var rvSelectedVideos: RecyclerView
    private lateinit var btnSaveVideos: MaterialButton

    private val calendar = Calendar.getInstance()
    private lateinit var selectedVideos: List<Uri>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_video, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        etVideoTitle = view.findViewById(R.id.etVideoTitle)
        etVideoDescription = view.findViewById(R.id.etVideoDescription)
        etTargetDate = view.findViewById(R.id.etTargetDate)
        rvSelectedVideos = view.findViewById(R.id.rvSelectedVideos)
        btnSaveVideos = view.findViewById(R.id.btnSaveVideos)

        etTargetDate.setOnClickListener {
            showDateTimePicker()
        }

        selectedVideos =
            arguments?.getStringArrayList("selectedVideos")?.map { Uri.parse(it) } ?: emptyList()

        rvSelectedVideos.layoutManager = LinearLayoutManager(requireContext())
        rvSelectedVideos.adapter = VideoAdapter(selectedVideos)

        btnSaveVideos.setOnClickListener {
            saveVideosToFirebase()
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
            TimePickerDialog(
                requireContext(),
                timeListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        DatePickerDialog(
            requireContext(),
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveVideosToFirebase() {
        val title = etVideoTitle.text.toString().trim()
        val description = etVideoDescription.text.toString().trim()
        val targetDate = calendar.time.takeIf { it.after(Date()) }

        if (title.isEmpty() || description.isEmpty() || targetDate == null) {
            Toast.makeText(
                requireContext(),
                "Please fill in all fields and select a valid date.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val videoUrls = mutableListOf<String>()
        val storageReference = FirebaseStorage.getInstance().reference

        selectedVideos.forEachIndexed { index, uri ->
            val videoRef =
                storageReference.child("videos/${auth.currentUser?.uid}/${uri.lastPathSegment ?: "video_$index.mp4"}")

            // Upload the video to Firebase Storage
            val uploadTask = videoRef.putFile(uri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Get the download URL after the video is uploaded
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    videoUrls.add(downloadUrl.toString())

                    // Check if all videos are uploaded
                    if (videoUrls.size == selectedVideos.size) {
                        // All videos are uploaded, now save the TimeCapsule
                        saveTimeCapsuleToFirestore(title, description, targetDate, videoUrls)
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to upload video: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveTimeCapsuleToFirestore(
        title: String,
        description: String,
        targetDate: Date,
        videoUrls: List<String>
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val timeCapsule = TimeCapsule(userId.toString(),title, description, Timestamp(targetDate), videoUrls)

        firestore.collection("timeCapsules")
            .add(timeCapsule)
            .addOnSuccessListener {
//                Toast.makeText(
//                    requireContext(),
//                    "Time capsule saved successfully!",
//                    Toast.LENGTH_SHORT
//                ).show()
                startActivity(Intent(requireContext(),ConfirmationActivity::class.java))
                // Optionally, navigate to another activity or clear input fields
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Error saving time capsule: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        fun newInstance(selectedVideos: ArrayList<String>): VideoFragment {
            val fragment = VideoFragment()
            val args = Bundle()
            args.putStringArrayList("selectedVideos", selectedVideos)
            fragment.arguments = args
            return fragment
        }
    }
}
