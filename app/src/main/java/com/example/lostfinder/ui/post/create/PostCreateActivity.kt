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
import com.example.lostfinder.data.model.post.PostCreateRequest
import com.example.lostfinder.ui.map.MapSelectActivity
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class PostCreateActivity : ComponentActivity() {

    private lateinit var viewModel: PostCreateViewModel

    // 선택된 이미지 URI
    private var imageUri: Uri? = null

    // 지도에서 받은 좌표
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    // 이미지 선택 런처
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri
                findViewById<ImageView>(R.id.imgPreview).setImageURI(uri)
            } else {
                Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // 지도 위치 선택 런처
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
        val editLocation = findViewById<EditText>(R.id.editLocation) // 사용자가 직접 쓰는 설명
        val editCategory = findViewById<EditText>(R.id.editCategory)

        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSelectLocation = findViewById<Button>(R.id.btnSelectLocation)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val progress = findViewById<ProgressBar>(R.id.progressUpload)

        /** 이미지 선택 버튼 **/
        btnSelectImage.setOnClickListener {
            // SAF 사용: 갤러리에서 이미지 선택
            pickImageLauncher.launch("image/*")
        }

        /** 지도 위치 선택 버튼 **/
        btnSelectLocation.setOnClickListener {
            val intent = Intent(this, MapSelectActivity::class.java)
            mapSelectLauncher.launch(intent)
        }

        /** 업로드 버튼 **/
        btnUpload.setOnClickListener {

            val title = editTitle.text.toString().trim()
            val content = editContent.text.toString().trim()
            val locationText = editLocation.text.toString().trim()
            val categoryText = editCategory.text.toString().trim()

            // 간단 유효성 검사
            if (title.isEmpty() || content.isEmpty() || locationText.isEmpty() || categoryText.isEmpty()) {
                Toast.makeText(this, "제목, 내용, 습득 장소, 카테고리를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = try {
                categoryText.toLong()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "카테고리 ID는 숫자여야 합니다.", Toast.LENGTH_SHORT).show()
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

            // DTO 생성 (백엔드 CreatePost DTO에 맞춰서 수정해 둔 상태)
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

            // 이미지 Part 생성
            val imagePart: MultipartBody.Part? = try {
                val file = uriToFile(imageUri!!)
                MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "이미지 파일 처리 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 업로드 호출
            viewModel.createPost(imagePart, data)
        }

        /** 업로드 상태 관찰 **/
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
                        Log.e("PostCreate", "업로드 실패: ${state.msg}")
                    }

                    else -> Unit
                }
            }
        }
    }

    /**
     * SAF Uri → Temp File 변환
     */
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("InputStream is null for uri: $uri")
        val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
