package com.example.exigency

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.textfield.TextInputEditText

class RegisterNumberActivity : AppCompatActivity() {

    private lateinit var number: EditText
    private lateinit var name: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_number)
        number = findViewById(R.id.number)
        name = findViewById(R.id.Name)

        number.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            startActivityForResult(i, 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            var contacturi = data?.data ?: return
            var cols = arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            var rs = contentResolver.query(contacturi, cols, null, null, null)

            if (rs?.moveToFirst()!!) {
                number.setText(rs.getString(0))
                name.setText(rs.getString(1))
            }
        }
    }

    fun saveNumber(view: View) {
        val numberString = number.text.toString()
        if (numberString.length == 10) {
            val sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("ENUM", numberString)
            myEdit.apply()
            this@RegisterNumberActivity.finish()
        } else {
            Toast.makeText(this, "Enter Valid Number!", Toast.LENGTH_SHORT).show()
        }
    }
}
