import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.timescape.R
import com.example.timescape.TimeCapsule
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeCapsuleAdapter(
    private val context: Context,
    private val timeCapsules: List<TimeCapsule>,
    private val currentUid: String
) : RecyclerView.Adapter<TimeCapsuleAdapter.TimeCapsuleViewHolder>() {

    private val filteredCapsules: List<TimeCapsule> = timeCapsules.filter { it.userId == currentUid }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeCapsuleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_capsule, parent, false)
        return TimeCapsuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeCapsuleViewHolder, position: Int) {
        val timeCapsule = filteredCapsules[position]
        holder.bind(timeCapsule)


    }

    override fun getItemCount(): Int {
        return filteredCapsules.size
    }

    inner class TimeCapsuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvDescription)
        private val targetDateTextView: TextView = itemView.findViewById(R.id.tvOpensOn)
        private val uploadDateTextView: TextView = itemView.findViewById(R.id.tvUploadDate)
        private val imageView: ImageView = itemView.findViewById(R.id.ivMediaPreview)
        private val videoIcon: ImageView = itemView.findViewById(R.id.videoIcon)
        private val lockImageView: ImageView = itemView.findViewById(R.id.ivLock)


        fun bind(timeCapsule: TimeCapsule) {
            val currentDate = Date()
            val canBeOpened = timeCapsule.targetDate?.toDate()?.before(currentDate) ?: false

            if (canBeOpened) {
                itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))

                titleTextView.text = timeCapsule.title
                descriptionTextView.text = timeCapsule.description
                targetDateTextView.text = "Opens on: ${formatDate(timeCapsule.targetDate?.toDate())}"

                val uploadTime = timeCapsule.createdAt?.toDate()?.let { formatDate(it) }
                uploadDateTextView.text = "Uploaded on: $uploadTime"

                titleTextView.visibility = View.VISIBLE
                descriptionTextView.visibility = View.VISIBLE
                targetDateTextView.visibility = View.VISIBLE
                uploadDateTextView.visibility = View.VISIBLE
                lockImageView.visibility = View.GONE
                targetDateTextView.visibility = View.GONE

                // Handle image display
                timeCapsule.contents?.let { contents ->
                    if (contents.isNotEmpty()) {
                        // Assuming `contents` is a list of URLs
                        for (contentUrl in contents) {
                            if (contentUrl.endsWith(".jpg") || contentUrl.endsWith(".png")) {
                                imageView.visibility = View.VISIBLE
                                Picasso.get()
                                    .load(contentUrl)
                                    .placeholder(R.drawable.ic_video_placeholder)
                                    .error(R.drawable.error_image)
                                    .into(imageView)
                            } else if (contentUrl.endsWith(".mp4") || contentUrl.endsWith(".gif")) {
                                videoIcon.visibility = View.VISIBLE
                                videoIcon.setOnClickListener {
                                    // Handle video playback
                                    Toast.makeText(context, "Play video", Toast.LENGTH_SHORT).show()
                                    playVideo(contentUrl)
                                }
                            }
                        }
                    } else {
                        // No media available
                        imageView.visibility = View.GONE
                        videoIcon.visibility = View.GONE
                    }
                }

            }

            else {
                // Display lock placeholder and target date
                titleTextView.visibility = View.GONE
                descriptionTextView.visibility = View.GONE
                imageView.visibility = View.GONE
                videoIcon.visibility = View.GONE

                lockImageView.visibility = View.VISIBLE
                targetDateTextView.visibility = View.VISIBLE
                targetDateTextView.text = "Opens on: ${formatDate(timeCapsule.targetDate?.toDate())}"

                lockImageView.setOnClickListener {
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.popup_capsule_locked, null)

                    // Create the dialog and set the view
                    val dialog = Dialog(context)
                    dialog.setContentView(view)

                    // Set the dialog window properties
                    dialog.window?.setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    // Initialize the views in the pop-up
                    val tvLockedMessage: TextView = view.findViewById(R.id.tvLockedMessage)
                    val tvOpensOnDate: TextView = view.findViewById(R.id.tvOpensOnDate)
                    val btnOkay: Button = view.findViewById(R.id.btnOkay)
                    val lockimg: ImageView = view.findViewById(R.id.imgLock)

                    // Set the "Opens On" date
                    tvOpensOnDate.text = "Opens on: ${formatDate(timeCapsule.targetDate?.toDate())}"

                    // Set the click listener for the OKAY button to close the dialog
                    btnOkay.setOnClickListener {
                        dialog.dismiss()
                    }

                    // Show the dialog
                    dialog.show()
                    Toast.makeText(context, "This capsule opens on ${formatDate(timeCapsule.targetDate?.toDate())}", Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnLongClickListener {
                // Show pop-up menu
                showPopupMenu(timeCapsule)
                true
            }


        }

        fun showPopupMenu(timeCapsule: TimeCapsule) {
            val popupMenu = PopupMenu(context, itemView)
            popupMenu.menuInflater.inflate(R.menu.capsule_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_share -> {
                        // Handle sharing logic here
                        // You can call a function in MessageFragment if needed
                        //MessageFragment.shareCapsule(timeCapsule)
                        true
                    }
                    R.id.menu_delete -> {
                        // Confirm deletion with a dialog
                        AlertDialog.Builder(context)
                            .setTitle("Delete Capsule")
                            .setMessage("Are you sure you want to delete this capsule?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                // Call the delete function in MessageFragment
                                //MessageFragment.deleteCapsule(timeCapsule)
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun downloadFile(fileUrl: String, fileName: String, isImage: Boolean) {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl)
            val localFile = if (isImage) {
                File(context.cacheDir, "$fileName.jpg")
            } else {
                File(context.cacheDir, "$fileName.mp4")
            }

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    if (isImage) {
                        displayImage(localFile)
                    } else {
                        displayVideo(localFile)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Download", "Failed to download file", e)
                }
        }

        private fun displayImage(file: File) {
            imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(file)
                .into(imageView)
        }

        private fun displayVideo(file: File) {
            videoIcon.visibility = View.VISIBLE
            videoIcon.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.absolutePath))
                intent.setDataAndType(Uri.parse(file.absolutePath), "video/*")
                context.startActivity(intent)
            }
        }


        private fun formatDate(date: Date?): String {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return date?.let { dateFormat.format(it) } ?: "Unknown"
        }
    }

    private fun playVideo(videoUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
        intent.setDataAndType(Uri.parse(videoUrl), "video/*")
        context.startActivity(intent)
    }

}

