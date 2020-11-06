//package com.http.proxy.request
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.http.proxy.R
//import com.http.proxy.RequestLogPair
//import java.text.SimpleDateFormat
//import java.util.*
//import kotlin.collections.ArrayList
//
//class ProxyConfigListAdapter constructor(
//    private val context: Context
//) :
//    RecyclerView.Adapter<LogViewHolder>(), View.OnClickListener {
//
//    @SuppressLint("SimpleDateFormat")
//    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
//
//    private val dataList by lazy {
//        ArrayList<RequestLogPair>()
//    }
//
//    fun setData(list: List<RequestLogPair>) {
//        dataList.clear()
//        dataList.addAll(list)
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
//        val view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false)
//        return LogViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
//        val data = dataList[position]
//        holder.itemView.setOnClickListener(this)
//        holder.itemView.tag = data
//        holder.tvUrl.text = data.request.url
//        holder.tvTag.text = data.tag
//        var time = formatTime(data.request.time) + "-"
//        data.response?.let {
//            time += formatTime(it.time)
//        }
//        holder.tvTime.text = time
//    }
//
//    override fun onClick(v: View) {
//        val data: RequestLogPair = v.tag as RequestLogPair
//        RequestDetailActivity.start(context,data)
//    }
//
//    override fun getItemCount(): Int = dataList.size
//
//    private fun formatTime(time: Long): String {
//        val date = Date(time)
//        return format.format(date)
//    }
//}
//
//class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    val tvUrl: TextView = itemView.findViewById(R.id.tv_url)
//    val tvTime: TextView = itemView.findViewById(R.id.tv_time)
//
//    val tvTag: TextView = itemView.findViewById(R.id.tv_tag)
//}