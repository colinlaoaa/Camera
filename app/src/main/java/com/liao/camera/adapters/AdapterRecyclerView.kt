package com.liao.camera.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.liao.camera.R
import com.liao.camera.model.PhotoInfo
import kotlinx.android.synthetic.main.new_row.view.*

class AdapterRecyclerView(var mContext: Context, var mList: ArrayList<PhotoInfo>) :
    RecyclerView.Adapter<AdapterRecyclerView.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        var view = LayoutInflater.from(mContext).inflate(R.layout.new_row, parent, false)
        var viewHolder = MyViewHolder(view)
        return viewHolder
    }

    fun setList(list: ArrayList<PhotoInfo>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var photo = mList[position]
        holder.Bind(photo,position)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun Bind(photo: PhotoInfo, position: Int) {
            itemView.image_view.setImageBitmap(photo.photo)
            itemView.text_view_1.text = photo.name
            itemView.button_delete.visibility = View.GONE
            itemView.image_view.setOnClickListener {
                if (itemView.button_delete.visibility == View.VISIBLE) {
                    itemView.button_delete.visibility = View.GONE
                } else {
                    itemView.button_delete.visibility = View.VISIBLE
                    itemView.button_delete.setOnClickListener {
                        notifyItemRemoved(position)
                        mList.remove(mList[position])
                        notifyItemRangeChanged(position,itemCount)
                        notifyDataSetChanged()

                    }
                }
            }
        }
    }
}