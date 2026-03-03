package bg.iag.tel24.ui.employee

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import bg.iag.tel24.R
import bg.iag.tel24.data.model.TreeNode
import bg.iag.tel24.databinding.BottomSheetEmployeeBinding

class EmployeeDetailSheet : BottomSheetDialogFragment() {

    private var _b: BottomSheetEmployeeBinding? = null
    private val b get() = _b!!

    companion object {
        fun newInstance(node: TreeNode) = EmployeeDetailSheet().apply {
            arguments = Bundle().also { it.putString("node", Gson().toJson(node)) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = BottomSheetEmployeeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val node = Gson().fromJson(arguments?.getString("node"), TreeNode::class.java)

        b.tvName.text  = node.text  ?: ""
        b.tvTitle.text = node.dlag  ?: ""
        b.tvDept.text  = node.pod   ?: ""
        b.tvPhone.text = node.gsm   ?: ""
        b.tvEmail.text = node.email ?: ""

        b.ivPhoto.load(node.imageUrl) {
            placeholder(R.drawable.ic_person_placeholder)
            error(R.drawable.ic_person_placeholder)
            transformations(CircleCropTransformation())
        }

        b.btnCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${node.gsm}")))
        }
        b.btnSms.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:${node.gsm}")))
        }
        b.btnEmail.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${node.email}")))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
