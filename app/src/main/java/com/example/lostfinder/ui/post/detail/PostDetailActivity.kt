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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PostDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val viewModel: PostDetailViewModel by viewModels()

    private var googleMap: GoogleMap? = null
    private var savedLat: Double? = null
    private var savedLng: Double? = null

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
        val foundLocation = findViewById<TextView>(R.id.textFoundLocation)
        val btnContact = findViewById<Button>(R.id.btnContact)

        /** 지도 Fragment 초기화 */
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.detailMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /** 상세 정보 로드 */
        viewModel.loadPost(postId)

        /** UI 갱신 처리 */
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
                    foundLocation.text = data.foundLocation ?: "정보 없음"

                    Glide.with(this)
                        .load(data.imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .fitCenter()
                        .into(img)

                    /** 저장된 좌표 보관 */
                    savedLat = data.lat
                    savedLng = data.lng

                    /** 지도 마커 업데이트 */
                    updateMapMarker()

                    btnContact.setOnClickListener {
                        viewModel.loadContact(postId)
                    }
                }
            }
        }

        /** 연락처 팝업 상태 처리 */
        viewModel.contactState.collectWhenStarted(this) { state ->
            when (state) {
                is PostDetailViewModel.ContactState.Success -> {
                    showContactDialog(state.data.writerName, state.data.writerPhone)
                }

                is PostDetailViewModel.ContactState.Error -> {
                    showErrorDialog(state.msg)
                }

                else -> Unit
            }
        }
    }

    /** 구글맵 준비 완료 */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        updateMapMarker()
    }

    /** 지도에 마커 표시 + 카메라 이동 */
    private fun updateMapMarker() {
        val lat = savedLat ?: return
        val lng = savedLng ?: return
        val map = googleMap ?: return

        val position = LatLng(lat, lng)

        map.clear()
        map.addMarker(MarkerOptions().position(position).title("습득 위치"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
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
