package com.example.lostfinder.ui.post.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostfinder.R
import com.example.lostfinder.ui.post.create.PostCreateActivity
import com.example.lostfinder.ui.post.detail.PostDetailActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest

class PostListActivity : ComponentActivity() {

    private lateinit var viewModel: PostListViewModel
    private lateinit var layoutPagination: LinearLayout

    /** 🔥 글쓰기 후 결과 받는 런처 */
    private val createPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 글쓰기 성공 → 첫 페이지 새로고침
                viewModel.loadPosts(0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        viewModel = ViewModelProvider(this)[PostListViewModel::class.java]

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerPosts)
        recycler.layoutManager = LinearLayoutManager(this)

        layoutPagination = findViewById(R.id.layoutPagination)

        val swipeRefresh = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(
            R.id.swipeRefresh
        )

        /** 첫 로딩 */
        viewModel.loadPosts(0)

        /** 🔥 게시글 목록 변경 시 UI 갱신 */
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collectLatest { list ->
                recycler.adapter = PostListAdapter(list) { id ->

                    // 🔥 상세 페이지 id 키 통일 (postId 사용!)
                    val intent = Intent(this@PostListActivity, PostDetailActivity::class.java)
                    intent.putExtra("postId", id)
                    startActivity(intent)
                }
            }
        }

        /** 페이지 수 변경 시 번호 갱신 */
        lifecycleScope.launchWhenStarted {
            viewModel.totalPages.collectLatest { total ->
                drawPagination(total, viewModel.currentPage.value)
            }
        }

        /** 현재 페이지 변경 시 번호 갱신 */
        lifecycleScope.launchWhenStarted {
            viewModel.currentPage.collectLatest { page ->
                drawPagination(viewModel.totalPages.value, page)
            }
        }



        /** 아래로 당겨 새로고침 */
        swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts(viewModel.currentPage.value)
            swipeRefresh.isRefreshing = false
        }

        /** 🔥 글쓰기 버튼 → 결과받기 방식으로 변경 */
        findViewById<FloatingActionButton>(R.id.btnCreatePost).setOnClickListener {
            val intent = Intent(this, PostCreateActivity::class.java)
            createPostLauncher.launch(intent)
        }
    }

    /** 페이지 번호 UI 생성 */
    private fun drawPagination(totalPages: Int, current: Int) {
        layoutPagination.removeAllViews()

        for (i in 0 until totalPages) {
            val tv = TextView(this).apply {
                text = (i + 1).toString()
                textSize = if (i == current) 20f else 16f
                setPadding(25, 10, 25, 10)
                setOnClickListener {
                    viewModel.loadPosts(i)
                }
            }
            layoutPagination.addView(tv)
        }
    }
}
