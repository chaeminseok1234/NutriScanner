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
import com.example.nutriscanner.R
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide


class MyLogAdapter(
    private val logList: MutableList<LogItem>,
    private val onItemClick: (LogItem) -> Unit
) : RecyclerView.Adapter<MyLogAdapter.LogViewHolder>() {

    // 로그 카드 구성요소
    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val imageView: ImageView = view.findViewById(R.id.foodImage)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val deleteLayout: View = view.findViewById(R.id.deleteConfirmLayout)
        val btnConfirmDelete: Button = view.findViewById(R.id.btnConfirmDelete)
    }

    //뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_card, parent, false)
        return LogViewHolder(view)
    }

    //데이터 바인딩 및 클릭 이벤트 처린
    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val item = logList[position]
        holder.dateText.text = item.dateTime

        Log.d("skrskr", "imageUri: ${item.imageUri}")
        Log.d("skrskr", "dateTime: ${item.dateTime}")

        //이미지 로딩
        Glide.with(holder.itemView.context)
            .load(item.imageUri)
            .placeholder(R.drawable.sample)
            .error(R.drawable.ic_close)
            .into(holder.imageView)

        //삭제 버튼 숨김 상태
        holder.deleteLayout.visibility = View.GONE

        //삭제 버튼 클릭시 창 뜨게함
        holder.btnDelete.setOnClickListener {
            holder.deleteLayout.visibility = View.VISIBLE
        }

        //삭제 버튼 클릭시 firstore에서 문서 삭제
        holder.btnConfirmDelete.setOnClickListener {
            val context = holder.itemView.context
            FirebaseFirestore.getInstance()
                .collection("logs")
                .document(item.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                    logList.removeAt(position)
                    notifyItemRemoved(position)
                }
        }

        //카드 클릭 시 상세보기 콜백 실행
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    //리스트 아이템 총 개수 반환
    override fun getItemCount() = logList.size
}