package bg.iag.tel24.ui.tree

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bg.iag.tel24.databinding.FragmentTreeBinding
import bg.iag.tel24.ui.employee.EmployeeDetailSheet
import com.google.android.material.snackbar.Snackbar

class TreeFragment : Fragment() {

    private var _binding: FragmentTreeBinding? = null
    private val binding get() = _binding!!
    private val vm: TreeViewModel by viewModels()

    companion object {
        private const val ARG = "source"
        fun newIag() = make(DataSource.IAG)
        fun newRdg() = make(DataSource.RDG)
        fun newDp()  = make(DataSource.DP)
        private fun make(src: DataSource) = TreeFragment().apply {
            arguments = bundleOf(ARG to src.name)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TreeAdapter { employee ->
            EmployeeDetailSheet.newInstance(employee)
                .show(childFragmentManager, "emp")
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter  = adapter
        }

        vm.items.observe(viewLifecycleOwner)   { adapter.submitNodes(it) }
        vm.loading.observe(viewLifecycleOwner) { binding.progressBar.isVisible = it }
        vm.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        val source = DataSource.valueOf(arguments?.getString(ARG) ?: DataSource.IAG.name)
        if (vm.items.value.isNullOrEmpty()) vm.load(source)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
