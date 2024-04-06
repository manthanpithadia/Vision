package com.example.vision.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.vision.R

class FeatureListAdapter(private val listner: OnItemClickListner) : RecyclerView.Adapter<FeatureListAdapter.ViewHolder>(){

    var array = arrayOf<String>("Read the Document","Detect Object", "Open Map")
    var desc = arrayOf<String>("Read and Analyze your Document",
    "Detect the object in real time", "Navigate through Map")
    var img = arrayOf(R.drawable.ic_scan,R.drawable.ic_obj_detection,R.drawable.ic_map)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FeatureListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_card_view,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureListAdapter.ViewHolder, position: Int) {
        holder.icCardView.setImageResource(img[position])
        holder.title.text = array[position]
        holder.desc.text = desc[position]
    }

    override fun getItemCount(): Int {
        return array.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        var icCardView: ImageView
        var title: TextView
        var desc: TextView
        init{
            icCardView = itemView.findViewById(R.id.ic_cardView)
            title = itemView.findViewById(R.id.txtTitle)
            desc = itemView.findViewById(R.id.txtDesc)

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listner.onItemClick(position)
            }
        }
    }

    interface OnItemClickListner{
        fun onItemClick(position: Int)
    }

}