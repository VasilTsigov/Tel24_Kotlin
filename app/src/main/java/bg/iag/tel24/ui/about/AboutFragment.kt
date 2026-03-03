package bg.iag.tel24.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import bg.iag.tel24.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _b: FragmentAboutBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentAboutBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
