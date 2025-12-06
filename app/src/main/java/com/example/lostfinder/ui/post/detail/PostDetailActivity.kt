package com.example.lostfinder.ui.post.detail

import android.app.AlertDialog
import android.os.Bundle
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
        val foundLocation = findViewById<TextView>(R.id.textFoundLocation)   // ★ 습득 장소 추가
        val btnContact = findViewById<Button>(R.id.btnContact)

        // 게시글 상세 로드
        viewModel.loadPost(postId)

        // UI 처리
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

                    title.text = data.title
                    content.text = data.content
                    foundLocation.text = data.foundLocation ?: "정보 없음"   // ★ 표시

                    Glide.with(this)
                        .load(data.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(img)

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
                viewModel.resetContactState()
            }
            .create()

        dialog.setOnDismissListener {
            viewModel.resetContactState()
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
