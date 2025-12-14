package com.example.lostfinder.ui.post.create

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lostfinder.R
import com.example.lostfinder.data.model.category.Category
import com.example.lostfinder.data.model.post.PostCreateRequest
import com.example.lostfinder.ui.map.MapSelectActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class PostCreateActivity : ComponentActivity() {

    private lateinit var viewModel: PostCreateViewModel

    // 이미지
    private var imageUri: Uri? = null

    // 지도 좌표
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    // 카테고리
    private lateinit var categories: List<Category>
    private var selectedCategoryId: Long = 0L

    /** 이미지 선택 */
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                findViewById<ImageView>(R.id.imgPreview).setImageURI(it)
            }
        }

    /** 지도 선택 */
    private val mapSelectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedLat = result.data?.getDoubleExtra("lat", 0.0)
                selectedLng = result.data?.getDoubleExtra("lng", 0.0)
                Toast.makeText(this, "위치 선택 완료", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_create)

        viewModel = ViewModelProvider(this)[PostCreateViewModel::class.java]

        val editTitle = findViewById<EditText>(R.id.editTitle)
        val editContent = findViewById<EditText>(R.id.editContent)
        val editLocation = findViewById<EditText>(R.id.editLocation)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)

        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSelectLocation = findViewById<Button>(R.id.btnSelectLocation)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val progress = findViewById<ProgressBar>(R.id.progressUpload)

        /** 카테고리 로딩 */
        loadCategories(spinnerCategory)

        /** 이미지 선택 */
        btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        /** 지도 선택 */
        btnSelectLocation.setOnClickListener {
            mapSelectLauncher.launch(Intent(this, MapSelectActivity::class.java))
        }

        /** 업로드 */
        btnUpload.setOnClickListener {

            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()
            val locationText = editLocation.text.toString().trim()

            if (title.isEmpty() || content.isEmpty() || locationText.isEmpty()) {
                Toast.makeText(this, "제목, 내용, 습득 장소를 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedCategoryId == 0L) {
                Toast.makeText(this, "카테고리를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri == null) {
                Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedLat == null || selectedLng == null) {
                Toast.makeText(this, "지도를 열어 위치를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestDto = PostCreateRequest(
                title = title,
                content = content,
                foundLocation = locationText,
                categoryId = selectedCategoryId,
                lat = selectedLat!!,
                lng = selectedLng!!
            )

            val json = Gson().toJson(requestDto)
            val data = json.toRequestBody("application/json".toMediaTypeOrNull())

            val imagePart = try {
                val file = uriToFile(imageUri!!)
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
            } catch (e: IOException) {
                Toast.makeText(this, "이미지 처리 오류", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createPost(imagePart, data)
        }

        /** 업로드 상태 */
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
                        Log.e("PostCreate", state.msg)
                    }
                    else -> Unit
                }
            }
        }
    }
    //카테고리 불러오기
    private fun loadCategories(spinner: Spinner) {
        lifecycleScope.launch {
            try {
                val response = viewModel.getCategories()

                val categoryList = response.data
                if (categoryList.isNullOrEmpty()) {
                    Toast.makeText(
                        this@PostCreateActivity,
                        "카테고리를 불러올 수 없습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                categories = categoryList

                val adapter = ArrayAdapter(
                    this@PostCreateActivity,
                    android.R.layout.simple_spinner_item,
                    categories.map { it.name }
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                spinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedCategoryId = categories[position].id
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }

            } catch (e: Exception) {
                Toast.makeText(
                    this@PostCreateActivity,
                    "카테고리 로딩 중 오류 발생",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /** Uri → File */
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("InputStream is null")
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
