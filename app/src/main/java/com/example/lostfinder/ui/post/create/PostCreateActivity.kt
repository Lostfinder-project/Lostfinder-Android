package com.example.lostfinder.ui.post.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lostfinder.R
import com.example.lostfinder.data.model.post.PostCreateRequest
import com.example.lostfinder.ui.map.MapSelectActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostCreateActivity : ComponentActivity() {

    private lateinit var viewModel: PostCreateViewModel
    private var imageUri: Uri? = null

    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    /** 이미지 선택 */
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                findViewById<ImageView>(R.id.imgPreview).setImageURI(uri)
            }
        }

    /** 지도에서 좌표 받기 */
    private val mapSelectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedLat = result.data?.getDoubleExtra("lat", 0.0)
                selectedLng = result.data?.getDoubleExtra("lng", 0.0)

                Toast.makeText(
                    this,
                    "위치 선택 완료: $selectedLat, $selectedLng",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        viewModel = ViewModelProvider(this)[PostCreateViewModel::class.java]

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editContent = findViewById<EditText>(R.id.editContent)
        val editLocation = findViewById<EditText>(R.id.editLocation)
        val editCategory = findViewById<EditText>(R.id.editCategory)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSelectLocation = findViewById<Button>(R.id.btnSelectLocation)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val progress = findViewById<ProgressBar>(R.id.progressUpload)

        /** 이미지 선택 */
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        /** 지도 위치 선택 */
        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapSelectActivity::class.java)
            mapSelectLauncher.launch(intent)
        }

        /** 업로드 */
        btnUpload.setOnClickListener {

            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            val locationText = editLocation.text.toString()
            val categoryId = editCategory.text.toString().toLong()

            if (imageUri == null) {
                Toast.makeText(this, "이미지를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedLat == null || selectedLng == null) {
                Toast.makeText(this, "지도를 열어 위치를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Request DTO
            val requestDto = PostCreateRequest(
                title = title,
                content = content,
                foundLocation = locationText,
                categoryId = categoryId,
                lat = selectedLat!!,
                lng = selectedLng!!
            )

            val json = Gson().toJson(requestDto)
            val data = json.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart = imageUri?.let { uri ->
                val file = uriToFile(uri)
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }

            viewModel.createPost(imagePart, data)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uploadState.collectLatest { state ->
                when (state) {
                    PostCreateViewModel.UploadState.Loading -> {
                        progress.visibility = ProgressBar.VISIBLE
                    }

                    PostCreateViewModel.UploadState.Success -> {
                        progress.visibility = ProgressBar.GONE
                        Toast.makeText(this@PostCreateActivity, "등록 완료!", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    is PostCreateViewModel.UploadState.Error -> {
                        progress.visibility = ProgressBar.GONE
                        Toast.makeText(this@PostCreateActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }
}
