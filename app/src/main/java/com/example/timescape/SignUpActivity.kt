package com.example.timescape

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.timescape.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signin.setOnClickListener({
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        })

        binding.signup.setOnClickListener({
            val email = binding.email.text.toString()
            val pass = binding.password.text.toString()
            val confirmpass = binding.confpassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmpass.isNotEmpty()){
                if (pass == confirmpass){
                    firebaseAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener {
                        if (it.isSuccessful){
                            val intent = Intent(this,MainUserInterface::class.java)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this,"The password does not match!", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"The fields are empty!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}