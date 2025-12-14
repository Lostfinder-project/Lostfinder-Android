package com.example.lostfinder.ui.post.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostfinder.R
import com.example.lostfinder.ui.post.create.PostCreateActivity
import com.example.lostfinder.ui.post.detail.PostDetailActivity
import kotlinx.coroutines.flow.collectLatest

class PostListActivity : ComponentActivity() {

    private lateinit var viewModel: PostListViewModel
    private lateinit var layoutPagination: LinearLayout

    private val createPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.loadPosts(0)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        viewModel = ViewModelProvider(this)[PostListViewModel::class.java]

        val recycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerPosts)
        recycler.layoutManager = LinearLayoutManager(this)

        val spinner = findViewById<Spinner>(R.id.spinnerFilterCategory)
        val swipeRefresh =
            findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefresh)

        layoutPagination = findViewById(R.id.layoutPagination)

        /** 카테고리 observe */
        lifecycleScope.launchWhenStarted {
            viewModel.categories.collectLatest { categories ->
                val names = mutableListOf("전체")
                names.addAll(categories.map { it.name })

                val adapter = ArrayAdapter(
                    this@PostListActivity,
                    android.R.layout.simple_spinner_item,
                    names
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
                            val categoryId =
                                if (position == 0) null
                                else categories[position - 1].id

                            viewModel.setCategory(categoryId)
                            viewModel.loadPosts(0)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
            }
        }

        /** 게시글 목록 observe */
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collectLatest { list ->
                recycler.adapter = PostListAdapter(list) { postId ->
                    val intent =
                        Intent(this@PostListActivity, PostDetailActivity::class.java)
                    intent.putExtra("postId", postId)
                    startActivity(intent)
                }
            }
        }

        /** 페이지네이션 observe */
        lifecycleScope.launchWhenStarted {
            viewModel.totalPages.collectLatest { total ->
                drawPagination(total, viewModel.currentPage.value)
            }
        }

        /** SwipeRefresh 로딩 상태 연동 */
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collectLatest { loading ->
                swipeRefresh.isRefreshing = loading
            }
        }

        /** 새로고침 트리거 */
        swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts(0)
        }

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
            R.id.btnCreatePost
        ).setOnClickListener {
            createPostLauncher.launch(
                Intent(this, PostCreateActivity::class.java)
            )
        }
    }

    private fun drawPagination(totalPages: Int, current: Int) {
        layoutPagination.removeAllViews()

        for (i in 0 until totalPages) {
            val tv = TextView(this).apply {
                text = (i + 1).toString()
                setPadding(25, 10, 25, 10)
                textSize = if (i == current) 20f else 16f
                setOnClickListener { viewModel.loadPosts(i) }
            }
            layoutPagination.addView(tv)
        }
    }
}
