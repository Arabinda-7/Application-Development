package com.example.allinone

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchitectTreeAdapter(
    private var flatNodes: List<FlatNode>,
    private val onNodeToggle: (FlatNode) -> Unit,
    private val onNodeAddChild: (FlatNode) -> Unit,
    private val onNodeEdit: (FlatNode) -> Unit
) : RecyclerView.Adapter<ArchitectTreeAdapter.TreeViewHolder>() {

    data class FlatNode(
        val feature: ProjectFeature,
        val depth: Int,
        val parentProject: Note,
        val path: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_architect_node, parent, false)
        return TreeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        val flatNode = flatNodes[position]
        val feature = flatNode.feature

        // Indentation
        val params = holder.contentContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = flatNode.depth * 24.dpToPx(holder.itemView.context)
        holder.contentContainer.layoutParams = params

        // Branch Line
        holder.branchLine.visibility = if (flatNode.depth > 0) View.VISIBLE else View.GONE

        // Content
        holder.tvName.text = feature.name ?: "New Node"
        holder.tvPath.text = flatNode.path ?: ""
        
        // Expand/Collapse Icon
        @Suppress("SENSELESS_COMPARISON")
        val children = feature.subFeatures
        val subCount = if (children != null) children.size else 0

        if (subCount > 0) {
            holder.ivExpand.visibility = View.VISIBLE
            holder.ivExpand.setImageResource(if (feature.isExpanded) android.R.drawable.arrow_down_float else android.R.drawable.arrow_up_float)
            holder.ivExpand.setOnClickListener { onNodeToggle(flatNode) }
        } else {
            holder.ivExpand.visibility = View.INVISIBLE
        }

        // Completion Style
        if (feature.isCompleted) {
            holder.tvName.paintFlags = holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvName.setTextColor(Color.GRAY)
        } else {
            holder.tvName.paintFlags = holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvName.setTextColor(Color.WHITE)
        }

        // Feature Tags (Resource/Path)
        @Suppress("SENSELESS_COMPARISON")
        val hasResource = (feature.resourceUrl != null && feature.resourceUrl.isNotEmpty()) || (feature.resourcePath != null && feature.resourcePath.isNotEmpty())
        holder.tagResource.visibility = if (hasResource) View.VISIBLE else View.GONE
        
        @Suppress("SENSELESS_COMPARISON")
        val isBlocked = feature.blockedByNodeId != null && feature.blockedByNodeId.isNotEmpty()
        holder.tagBlocked.visibility = if (isBlocked) View.VISIBLE else View.GONE

        // Actions
        holder.itemView.setOnClickListener { onNodeEdit(flatNode) }
        holder.btnAdd.setOnClickListener { onNodeAddChild(flatNode) }
    }

    override fun getItemCount(): Int = flatNodes.size

    fun updateNodes(newNodes: List<FlatNode>) {
        flatNodes = newNodes
        notifyDataSetChanged()
    }

    private fun Int.dpToPx(context: android.content.Context): Int = (this * context.resources.displayMetrics.density).toInt()

    class TreeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentContainer: LinearLayout = view.findViewById(R.id.node_content_container)
        val branchLine: View = view.findViewById(R.id.branch_line)
        val tvName: TextView = view.findViewById(R.id.tv_node_name)
        val tvPath: TextView = view.findViewById(R.id.tv_node_path)
        val ivExpand: ImageView = view.findViewById(R.id.iv_node_expand)
        val btnAdd: ImageView = view.findViewById(R.id.btn_add_sub_node)
        val tagResource: View = view.findViewById(R.id.tag_resource)
        val tagBlocked: View = view.findViewById(R.id.tag_blocked)
    }
}
