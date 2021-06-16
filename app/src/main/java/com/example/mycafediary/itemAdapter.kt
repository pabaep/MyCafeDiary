package com.example.mycafediary

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_view.view.*
import java.text.SimpleDateFormat

class itemAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var firestore : FirebaseFirestore? = null
    private var auth : FirebaseAuth
    var title : String? = null
    var time : Long? = null
    var detail : String? = null

    var itemList: ArrayList<item> = arrayListOf()

    init {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore?.collection("Records")?.whereEqualTo("uid", auth.currentUser?.uid)//
            ?.get()?.addOnSuccessListener { documents ->
                itemList.clear()
                for (doc in documents) {
                    title = doc?.data?.get("cafe_name").toString()
                    time = (doc?.data?.get("visit_date").toString()).toLong()
                    detail = doc?.data?.get("diary_detail").toString()
                    var item = item(title, time, detail)
                    itemList.add(item!!)
                }
                notifyDataSetChanged()//새로고침
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.list_view, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var viewHolder = (holder as ViewHolder).itemView
        viewHolder.textView.text = itemList[position].title
        viewHolder.textView2.text = SimpleDateFormat("yyyy.MM.dd").format(itemList[position].time)
        viewHolder.textView3.text = itemList[position].detail
        holder.bind(itemList[position])

    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(Item : item){
            itemView.setOnClickListener {
                Intent(context,DiaryViewActivity::class.java).apply {
                    putExtra("date", Item.time.toString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }
        }
    }
    //TODO 리사이클러 뷰 클릭 이벤트 구현하기 - 이것만 성공하면 끝남

}