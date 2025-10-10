package com.avetiso.feature_clients.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avetiso.feature_clients.R
import com.avetiso.feature_clients.databinding.FragmentClientsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientsFragment : Fragment(R.layout.fragment_clients) {

    private var binding: FragmentClientsBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentClientsBinding.bind(view)

        binding?.btnAddClient?.setOnClickListener {
            // Переходим на экран добавления клиента без аргументов
            findNavController().navigate(R.id.action_clientsFragment_to_addEditClientFragment)
        }

        // Здесь будет логика отображения списка клиентов
        // TODO: Создать ViewModel для этого экрана, адаптер и RecyclerView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}