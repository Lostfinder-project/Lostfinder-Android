package com.example.lostfinder.ui.post.detail

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.lostfinder.R
import com.example.lostfinder.util.collectWhenStarted

class PostDetailActivity : AppCompatActivity() {

    private val viewModel: PostDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val postId = intent.getLongExtra("postId", -1L)
        if (postId == -1L) {
            finish()
            return
        }

        val img = findViewById<ImageView>(R.id.imgPost)
        val title = findViewById<TextView>(R.id.textTitle)
        val content = findViewById<TextView>(R.id.textContent)
        val btnContact = findViewById<Button>(R.id.btnContact)

        // 게시글 상세 데이터 로드
        viewModel.loadPost(postId)

        // 게시글 상세 UI 처리
        viewModel.state.collectWhenStarted(this) { state ->
            when (state) {

                is PostDetailViewModel.PostDetailState.Loading -> {
                    title.text = "불러오는 중..."
                }

                is PostDetailViewModel.PostDetailState.Error -> {
                    title.text = "오류: ${state.msg}"
                }

                is PostDetailViewModel.PostDetailState.Success -> {
                    val data = state.data

                    // 🔥 서버에서 오는 imageUrl이 뭔지 확인용 Log
                    Log.d("POST_DETAIL", "imageUrl = ${data.imageUrl}")

                    title.text = data.title
                    content.text = data.content

                    Glide.with(this)
                        .load(data.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(img)

                    // 연락처 조회 버튼
                    btnContact.setOnClickListener {
                        viewModel.loadContact(postId)
                    }
                }
            }
        }

        // 연락처 팝업 처리
        viewModel.contactState.collectWhenStarted(this) { state ->
            when (state) {
                is PostDetailViewModel.ContactState.Success -> {
                    val contact = state.data

                    // 📌 writerName, writerPhone 정확히 매칭
                    showContactDialog(contact.writerName, contact.writerPhone)
                }

                is PostDetailViewModel.ContactState.Error -> {
                    showErrorDialog(state.msg)
                }

                else -> Unit
            }
        }
    }

    /** 연락처 다이얼로그 */
    private fun showContactDialog(name: String, phone: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("작성자 연락처")
            .setMessage("👤 이름: $name\n📱 전화번호: $phone")
            .setPositiveButton("닫기") { _, _ ->
                viewModel.resetContactState()   // 닫기 버튼 누를 때 초기화
            }
            .create()

        dialog.setOnDismissListener {
            viewModel.resetContactState()       // 외부 터치로 닫혀도 초기화
        }

        dialog.show()
    }

    /** 에러 메시지 다이얼로그 */
    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(this)
            .setTitle("오류")
            .setMessage(msg)
            .setPositiveButton("확인", null)
            .show()
    }
}
