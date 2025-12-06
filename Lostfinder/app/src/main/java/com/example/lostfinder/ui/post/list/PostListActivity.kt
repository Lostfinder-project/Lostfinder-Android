package com.example.lostfinder.ui.post.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostfinder.databinding.ActivityPostListBinding
import com.example.lostfinder.ui.post.create.PostCreateActivity
import com.example.lostfinder.ui.post.detail.PostDetailActivity
import com.example.lostfinder.util.collectWhenStarted

class PostListActivity : ComponentActivity() {

    private lateinit var binding: ActivityPostListBinding
    private val viewModel: PostListViewModel by viewModels()

    companion object {
        private const val REQ_CREATE_POST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeData()

        // 상단 새로고침 버튼
        binding.btnRefresh.setOnClickListener {
            Log.d("POST_LIST", "새로고침 버튼 눌림")
            refreshList()
        }

        // 당겨서 새로고침
        binding.swipeRefresh.setOnRefreshListener {
            refreshList()
        }

        // 글쓰기 버튼
        binding.btnCreatePost.setOnClickListener {
            val intent = Intent(this, PostCreateActivity::class.java)
            startActivityForResult(intent, REQ_CREATE_POST)
        }

        // 최초 로딩
        viewModel.loadPosts()
    }

    private fun setupRecyclerView() {
        binding.recyclerPosts.layoutManager = LinearLayoutManager(this)
    }

    private fun observeData() {

        viewModel.posts.collectWhenStarted(this) { postList ->
            binding.recyclerPosts.adapter = PostListAdapter(postList) { postId ->
                val intent = Intent(this, PostDetailActivity::class.java)
                intent.putExtra("postId", postId)
                startActivity(intent)
            }
        }

        // 로딩 완료 이벤트
        viewModel.refreshDone.collectWhenStarted(this) {
            binding.swipeRefresh.isRefreshing = false
            // 다음 로딩을 위해 초기화
            viewModel.resetRefreshFlag()
        }
    }


    /** 리스트 새로고침 공통 로직 */
    private fun refreshList() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadPosts()
    }

    /** 글 작성 후 돌아왔을 때 자동 새로고침 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CREATE_POST && resultCode == Activity.RESULT_OK) {
            refreshList()
        }
    }
}
