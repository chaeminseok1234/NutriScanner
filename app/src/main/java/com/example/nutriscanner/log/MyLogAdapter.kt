package com.example.nutriscanner.log

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutriscanner.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyLogAdapter(
    private val logList: MutableList<LogItem>,
    private val onItemClick: (LogItem) -> Unit
) : RecyclerView.Adapter<MyLogAdapter.LogViewHolder>() {

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView    = view.findViewById(R.id.dateText)
        val imageView: ImageView  = view.findViewById(R.id.foodImage)
        val btnDelete: ImageView  = view.findViewById(R.id.btnDelete)
        val deleteLayout: View    = view.findViewById(R.id.deleteConfirmLayout)
        val btnConfirmDelete: Button = view.findViewById(R.id.btnConfirmDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_card, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val item = logList[position]

        holder.dateText.text = item.dateTime
        Log.d("MyLogAdapter", "Loading imageUri=${item.imageUri}")

        // Glide 로 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(item.imageUri)
            .placeholder(R.drawable.sample)
            .error(R.drawable.ic_close)
            .into(holder.imageView)

        // 삭제 레이아웃 숨김
        holder.deleteLayout.visibility = View.GONE

        // “삭제” 아이콘 클릭 → 확인 버튼 보이기
        holder.btnDelete.setOnClickListener {
            holder.deleteLayout.visibility = View.VISIBLE
        }

        // “확인” 클릭 → Firestore 에서 문서 삭제
        holder.btnConfirmDelete.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid.isNullOrEmpty()) {
                Toast.makeText(holder.itemView.context, "유저 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("logs")
                .document(item.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    logList.removeAt(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 카드 전체 클릭 → 상세보기 콜백
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = logList.size
}
