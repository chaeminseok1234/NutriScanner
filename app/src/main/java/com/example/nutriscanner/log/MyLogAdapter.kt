package com.example.nutriscanner.log

import android.net.Uri
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

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val imageView: ImageView = view.findViewById(R.id.foodImage)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
        val deleteLayout: View = view.findViewById(R.id.deleteConfirmLayout)
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

        Log.d("skrskr", "imageUri: ${item.imageUri}")
        Log.d("skrskr", "dateTime: ${item.dateTime}")

        Glide.with(holder.itemView.context)
            .load(item.imageUri)
            .placeholder(R.drawable.sample)
            .error(R.drawable.ic_close)
            .into(holder.imageView)

        holder.deleteLayout.visibility = View.GONE

        holder.btnDelete.setOnClickListener {
            holder.deleteLayout.visibility = View.VISIBLE
        }

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

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = logList.size
}