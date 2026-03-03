package bg.iag.tel24.ui.employee

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import bg.iag.tel24.R
import coil.load

class PhotoViewerDialog : DialogFragment() {

    companion object {
        fun show(fm: FragmentManager, url: String) {
            PhotoViewerDialog().apply {
                arguments = Bundle().also { it.putString("url", url) }
            }.show(fm, "photo_viewer")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setOnClickListener { dismiss() }
            load(arguments?.getString("url")) {
                placeholder(R.drawable.ic_person_placeholder)
                error(R.drawable.ic_person_placeholder)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.black)
    }
}
