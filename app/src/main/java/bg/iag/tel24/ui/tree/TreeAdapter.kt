package bg.iag.tel24.ui.tree

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import bg.iag.tel24.data.model.TreeNode
import bg.iag.tel24.databinding.ItemDepartmentBinding
import bg.iag.tel24.databinding.ItemEmployeeBinding

class TreeAdapter(
    private val onEmployeeClick: (TreeNode) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    data class Item(
        val node: TreeNode,
        val level: Int,
        val isDept: Boolean,
        var expanded: Boolean = true
    )

    private var source:  List<Item> = emptyList()
    private var visible: List<Item> = emptyList()

    fun submitNodes(roots: List<TreeNode>) {
        source = flatten(roots, 0)
        rebuild()
    }

    private fun flatten(nodes: List<TreeNode>, level: Int): List<Item> {
        val list = mutableListOf<Item>()
        for (n in nodes) {
            val isDept = !n.leaf && !n.children.isNullOrEmpty()
            list.add(Item(n, level, isDept, expanded = level == 0))
            if (isDept) list.addAll(flatten(n.children!!, level + 1))
        }
        return list
    }

    private fun rebuild() {
        val result = mutableListOf<Item>()
        var collapsedDepth = -1
        for (item in source) {
            if (collapsedDepth >= 0) {
                if (item.level > collapsedDepth) continue else collapsedDepth = -1
            }
            result.add(item)
            if (item.isDept && !item.expanded) collapsedDepth = item.level
        }
        visible = result
        notifyDataSetChanged()
    }

    override fun getItemCount() = visible.size
    override fun getItemViewType(position: Int) = if (visible[position].isDept) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == 0)
            DeptHolder(ItemDepartmentBinding.inflate(inf, parent, false))
        else
            EmpHolder(ItemEmployeeBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = visible[position]
        when (holder) {
            is DeptHolder -> holder.bind(item)
            is EmpHolder  -> holder.bind(item)
        }
    }

    inner class DeptHolder(private val b: ItemDepartmentBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: Item) {
            b.tvName.text = item.node.text
            b.root.setPadding(item.level.dp(b.root.context), b.root.paddingTop,
                b.root.paddingRight, b.root.paddingBottom)
            b.ivChevron.animate()
                .rotation(if (item.expanded) 90f else 0f)
                .setDuration(150).start()
            b.root.setOnClickListener {
                item.expanded = !item.expanded
                rebuild()
            }
        }
    }

    inner class EmpHolder(private val b: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: Item) {
            b.tvName.text  = item.node.text
            b.tvTitle.text = item.node.dlag
            val base = (52 * b.root.context.resources.displayMetrics.density).toInt()
            b.root.setPadding(base + item.level.dp(b.root.context), b.root.paddingTop,
                b.root.paddingRight, b.root.paddingBottom)
            b.root.setOnClickListener { onEmployeeClick(item.node) }
        }
    }

    private fun Int.dp(ctx: Context) = (this * 16 * ctx.resources.displayMetrics.density).toInt()
}
