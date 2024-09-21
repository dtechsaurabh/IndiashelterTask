package com.example.indiasheltertask

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.indiasheltertask.adapters.ImageAdapter
import com.example.indiasheltertask.models.ContactModel
import com.example.indiasheltertask.models.ImageItem
import com.google.android.material.carousel.MaskableFrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Suppress("DEPRECATION")
class ContactHome : AppCompatActivity(), ContactAdapter.OnItemClickListener {

    private var arrayList = ArrayList<ContactModel>()
    private var filteredList = ArrayList<ContactModel>()
    private val rcvAdapter by lazy { ContactAdapter(filteredList, this) }
    private lateinit var totalContactsTextView: TextView
    private lateinit var userName: EditText
    private lateinit var userPhone: EditText
    private lateinit var userEmail: EditText
    private lateinit var userSubmit: Button
    private lateinit var userBack: ImageView

    private lateinit var scrollToTopButton: AppCompatImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageRV: RecyclerView
    private lateinit var nestedScrollView: NestedScrollView
    private var isFirstSubmit = true
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadingMessageTextView: TextView

    val imageList = arrayListOf(
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/866/500/500.jpg?hmac=FOptChXpmOmfR5SpiL2pp74Yadf1T_bRhBF1wJZa9hg"
        ),
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/270/500/500.jpg?hmac=MK7XNrBrZ73QsthvGaAkiNoTl65ZDlUhEO-6fnd-ZnY"
        ),
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/320/500/500.jpg?hmac=2iE7TIF9kIqQOHrIUPOJx2wP1CJewQIZBeMLIRrm74s"
        ),
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/798/500/500.jpg?hmac=Bmzk6g3m8sUiEVHfJWBscr2DUg8Vd2QhN7igHBXLLfo"
        ),
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/95/500/500.jpg?hmac=0aldBQ7cQN5D_qyamlSP5j51o-Og4gRxSq4AYvnKk2U"
        ),
        ImageItem(
            UUID.randomUUID().toString(),
            "https://fastly.picsum.photos/id/778/500/500.jpg?hmac=jZLZ6WV_OGRxAIIYPk7vGRabcAGAILzxVxhqSH9uLas"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_home)

        initViews()
        setupSearchView()
        setupRecyclerView()

        if (checkContactPermissions()) {
            showLoading()
            getContacts()
        }

        setupScrollToTopButton()
        setupUserBackButton()
        setupUserSubmitButton()

        val imageAdapter = ImageAdapter()
        imageRV.adapter = imageAdapter
        imageAdapter.submitList(imageList)

    }

    private fun showLoading() {
        loadingMessageTextView.visibility = View.VISIBLE
        loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingMessageTextView.visibility = View.GONE
        loadingProgressBar.visibility = View.GONE
    }

    private fun initViews() {
        totalContactsTextView = findViewById(R.id.totalNumber)
        userName = findViewById(R.id.editTextName)
        userPhone = findViewById(R.id.editTextPhone)
        userEmail = findViewById(R.id.editTextEmail)
        userSubmit = findViewById(R.id.submitButton)
        scrollToTopButton = findViewById(R.id.up_arrow1)
        nestedScrollView = findViewById(R.id.nestedScrollView)
        recyclerView = findViewById(R.id.recyclerView)
        userBack = findViewById(R.id.icon_location)
         imageRV = findViewById(R.id.imageRV)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        loadingMessageTextView = findViewById(R.id.loadingMessageTextView)
        val mergedTextView = findViewById<TextView>(R.id.mergedTextView)
        mergedTextViewColor(mergedTextView)


    }

    private fun setupSearchView() {
        findViewById<SearchView>(R.id.searchView).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?) = filterContacts(newText).let { false }
        })
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = rcvAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) {
                    scrollToTopButton.visibility = View.VISIBLE
                }

                if (!recyclerView.canScrollVertically(-1)) {
                    scrollToTopButton.visibility = View.GONE
                }
            }
        })
    }


    private fun setupScrollToTopButton() {
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY > oldScrollY) {
                scrollToTopButton.visibility = View.VISIBLE
            }

            if (scrollY <= 100) {
                scrollToTopButton.visibility = View.GONE
            }
        })

        scrollToTopButton.setOnClickListener {
            nestedScrollView.smoothScrollTo(0, 0)
            scrollToTopButton.visibility = View.GONE
        }
    }

    private fun setupUserBackButton() {
        userBack.setOnClickListener { onBackPressed() }
    }

    private fun setupUserSubmitButton() {
        userSubmit.setOnClickListener { handleSubmit() }
    }

    private fun handleSubmit() {
        val name = userName.text.toString().trim()
        val phone = userPhone.text.toString().trim()
        val email = userEmail.text.toString().trim()

        when {
            name.isEmpty() || phone.isEmpty() || email.isEmpty() ->
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            !isValidPhoneNumber(phone) ->
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            else -> {
                showSuccessDialog()
                if (isFirstSubmit) {
                    imageRV.visibility = View.VISIBLE
                    isFirstSubmit = false
                }
            }
        }
    }

    private fun isValidPhoneNumber(phone: String) =
        phone.matches("\\d{10}".toRegex()) || phone.matches("\\+\\d{12}".toRegex())

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Success")
            .setMessage("Your refer is done and data is saved!\n\nNote: Don't forget to share with your friends to earn rewards through our Refer and Earn program.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                clearInputFields()
            }
            .setNeutralButton("Learn More") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun clearInputFields() {
        userName.text.clear()
        userPhone.text.clear()
        userEmail.text.clear()
    }

    private fun mergedTextViewColor(mergedTextView: TextView) {
        val line1Color = ContextCompat.getColor(this, R.color.green)
        val line2Color = ContextCompat.getColor(this, R.color.darker_gray)
        val text = getString(R.string.ReferandEarnNotes)
        val spannableString = SpannableString(text)

        val lineBreakIndex = text.indexOf("\n")
        if (lineBreakIndex != -1) {
            spannableString.setSpan(ForegroundColorSpan(line1Color), 0, lineBreakIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(line2Color), lineBreakIndex + 1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            spannableString.setSpan(ForegroundColorSpan(line1Color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        mergedTextView.text = spannableString
    }

    private fun filterContacts(query: String?) {
        filteredList.clear()
        if (!query.isNullOrEmpty()) {
            val searchQuery = query.lowercase()
            filteredList.addAll(arrayList.filter { contact ->
                contact.displayName.lowercase().contains(searchQuery) || contact.number.contains(searchQuery)
            })

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "Data not found. Please retry.", Toast.LENGTH_SHORT).show()
            }
        } else {
            filteredList.addAll(arrayList)
        }

        rcvAdapter.notifyDataSetChanged()
    }

    @SuppressLint("Range")
    private fun getContacts() {
        CoroutineScope(Dispatchers.IO).launch {
            arrayList.clear()
            val cursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )

            val uniqueContacts = mutableSetOf<String>()
            cursor?.use {
                while (it.moveToNext()) {
                    val contactName = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val contactNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).let { formatPhoneNumber(it) }

                    if (uniqueContacts.add(contactNumber)) {
                        val initials = getInitials(contactName)
                        arrayList.add(ContactModel(contactName, contactNumber, initials))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                hideLoading()
                filteredList.addAll(arrayList)
                rcvAdapter.notifyDataSetChanged()
                totalContactsTextView.text = "Swipe up to see: ${arrayList.size} friends you can invite"
            }
        }
    }

    private fun getInitials(name: String): String {
        return name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").take(2)
    }

    private fun formatPhoneNumber(number: String): String {
        return number.replace("[^0-9+]".toRegex(), "")
    }

    private fun checkContactPermissions(): Boolean {
        return if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 100)
            false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access contacts.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(position: Int) {
    }

    override fun onWhatsAppClick(position: Int) {
        val clickedContact = filteredList[position]
        val phoneNumber = clickedContact.number
        sendWhatsAppMessage(formatPhoneNumberForWhatsApp(phoneNumber))
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    private fun sendWhatsAppMessage(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val message = "Hi! This is India Shelter.\n" +
                    "Refer & Earn: Your referral is done, and the data is saved!\n\n" +
                    "Download our app now: <Your App Link>"
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumberForWhatsApp(phoneNumber: String): String {
        return if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }
}
