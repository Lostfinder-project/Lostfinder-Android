package com.example.lostfinder.ui.post.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import com.example.lostfinder.R
import com.example.lostfinder.data.model.post.PostCreateRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostCreateActivity : ComponentActivity() {

    private lateinit var viewModel: PostCreateViewModel

    private var imageUri: Uri? = null
    private val PICK_IMAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        viewModel = ViewModelProvider(this)[PostCreateViewModel::class.java]

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editContent = findViewById<EditText>(R.id.editContent)
        val editLocation = findViewById<EditText>(R.id.editLocation)
        val editCategory = findViewById<EditText>(R.id.editCategory)
        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val progress = findViewById<ProgressBar>(R.id.progressUpload)

        /** 이미지 선택 **/
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }

        /** 업로드 **/
        btnUpload.setOnClickListener {

            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            val location = editLocation.text.toString()
            val categoryId = editCategory.text.toString().toLong()

            // JSON RequestBody 생성
            val requestDto = PostCreateRequest(title, content, location, categoryId)
            val gson = Gson()
            val json = gson.toJson(requestDto)

            val data = json.toRequestBody("application/json".toMediaTypeOrNull())

            // 이미지 Part 생성
            val imagePart = imageUri?.let { uri ->
                val file = uriToFile(uri)
                val body = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", file.name, body)
            }

            viewModel.createPost(imagePart, data)
        }

        /** ViewModel 상태 수집 **/
        lifecycleScope.launchWhenStarted {
            viewModel.uploadState.collectLatest { state ->
                when (state) {
                    PostCreateViewModel.UploadState.Loading -> {
                        progress.visibility = ProgressBar.VISIBLE
                    }
                    PostCreateViewModel.UploadState.Success -> {
                        progress.visibility = ProgressBar.GONE
                        Toast.makeText(this@PostCreateActivity, "등록 완료!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
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

    /** 갤러리 선택 결과 처리 **/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            val preview = findViewById<ImageView>(R.id.imgPreview)
            preview.setImageURI(imageUri)
        }
    }

    /** Uri → File 변환 **/
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }
}
