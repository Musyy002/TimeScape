package com.example.timescape

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class AccountsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var emTextView: TextView

    private lateinit var btnLogout: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accounts, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()


        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val dltbtn = view.findViewById<Button>(R.id.dltaccnt)
        emTextView = view.findViewById(R.id.emailTxt)

        // Log statement for debugging
        Log.d("AccountsFragment", "Logout button initialized")

        btnLogout.setOnClickListener {
            Log.d("AccountsFragment", "Logout button clicked")
            logoutUser()
        }

        dltbtn.setOnClickListener({
            confirmAccountDeletion()
        })

        val currentUser = auth.currentUser
        currentUser?.let {
            val email = it.email
            emTextView.text=email
        }
    }

    private fun confirmAccountDeletion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val userDocRef = firestore.collection("users").document(userId)

            userDocRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "User data deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to delete user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}