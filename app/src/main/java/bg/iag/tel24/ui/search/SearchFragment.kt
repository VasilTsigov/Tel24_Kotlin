package bg.iag.tel24.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import bg.iag.tel24.databinding.FragmentSearchBinding
import bg.iag.tel24.ui.employee.EmployeeDetailSheet
import bg.iag.tel24.ui.tree.TreeAdapter
import com.google.android.material.snackbar.Snackbar

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val vm: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
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

        vm.results.observe(viewLifecycleOwner) { adapter.submitNodes(it) }
        vm.loading.observe(viewLifecycleOwner) { binding.progressBar.isVisible = it }
        vm.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { vm.search(s?.toString() ?: "") }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
