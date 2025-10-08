package com.avetiso.goservice

import android.content.Intent
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

fun BottomNavigationView.setupWithNavController(
    navGraphIds: List<Int>,
    fragmentManager: FragmentManager,
    containerId: Int,
    intent: Intent,
): LiveData<NavController> {

    // Карта для хранения контроллеров для каждой вкладки
    val graphIdToTagMap = SparseArray<String>()
    // Результат, который будет возвращен
    val selectedNavController = MutableLiveData<NavController>()

    var firstFragmentGraphId = 0

    // Создаем NavHostFragment для каждой вкладки
    navGraphIds.forEachIndexed { index, navGraphId ->
        val fragmentTag = getFragmentTag(index)

        // Находим или создаем NavHostFragment
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            fragmentTag,
            navGraphId,
            containerId
        )

        // Получаем ID графа для первой вкладки
        val graphId = navHostFragment.navController.graph.id
        if (index == 0) {
            firstFragmentGraphId = graphId
        }

        graphIdToTagMap[graphId] = fragmentTag

        // Прикрепляем NavHostFragment к контейнеру
        if (this.selectedItemId == graphId) {
            // Восстанавливаем выбранный контроллер
            selectedNavController.value = navHostFragment.navController
            attachNavHostFragment(fragmentManager, navHostFragment, index == 0)
        } else {
            detachNavHostFragment(fragmentManager, navHostFragment)
        }
    }

    // Тег текущего выбранного фрагмента
    var selectedItemTag = graphIdToTagMap[this.selectedItemId]
    val firstFragmentTag = graphIdToTagMap[firstFragmentGraphId]
    var isFirstFragment = selectedItemTag == firstFragmentTag

    // Устанавливаем слушатель нажатий на элементы меню
    setOnItemSelectedListener { item ->
        // Не пересоздаем, если нажата та же вкладка
        if (fragmentManager.isStateSaved) {
            false
        } else {
            val newlySelectedItemTag = graphIdToTagMap[item.itemId]
            if (selectedItemTag != newlySelectedItemTag) {
                // Прячем старый фрагмент и показываем новый
                fragmentManager.popBackStack(
                    firstFragmentTag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                val selectedFragment = fragmentManager.findFragmentByTag(newlySelectedItemTag)
                        as NavHostFragment

                if (selectedItemTag != null) {
                    val MFragment = fragmentManager.findFragmentByTag(selectedItemTag)
                            as NavHostFragment
                    if (MFragment.isAdded) detachNavHostFragment(fragmentManager, MFragment)

                }
                attachNavHostFragment(fragmentManager, selectedFragment, newlySelectedItemTag == firstFragmentTag)

                selectedItemTag = newlySelectedItemTag
                isFirstFragment = selectedItemTag == firstFragmentTag
                selectedNavController.value = selectedFragment.navController
                true
            } else {
                false
            }
        }
    }

    // Обработка системной кнопки "назад"
    if (fragmentManager.findFragmentByTag(firstFragmentTag) == null) {
        // Если это первый запуск, добавляем первый фрагмент в back stack
        val navHostFragment = obtainNavHostFragment(
            fragmentManager,
            firstFragmentTag,
            navGraphIds[0],
            containerId
        )
        attachNavHostFragment(fragmentManager, navHostFragment, true)
    }

    // Выбираем правильный элемент, если back stack изменился
    fragmentManager.addOnBackStackChangedListener {
        if (!isFirstFragment && !fragmentManager.isOnBackStack(firstFragmentTag)) {
            this.selectedItemId = firstFragmentGraphId
        }

        // Обновляем контроллер при изменении back stack
        selectedNavController.value?.let { controller ->
            if (controller.currentDestination == null) {
                controller.navigate(controller.graph.id)
            }
        }
    }
    return selectedNavController
}

private fun detachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
) {
    fragmentManager.beginTransaction()
        .detach(navHostFragment)
        .commitNow()
}

private fun attachNavHostFragment(
    fragmentManager: FragmentManager,
    navHostFragment: NavHostFragment,
    isPrimaryNavFragment: Boolean,
) {
    fragmentManager.beginTransaction()
        .attach(navHostFragment)
        .apply {
            if (isPrimaryNavFragment) {
                setPrimaryNavigationFragment(navHostFragment)
            }
        }
        .commitNow()
}

private fun obtainNavHostFragment(
    fragmentManager: FragmentManager,
    fragmentTag: String,
    navGraphId: Int,
    containerId: Int,
): NavHostFragment {
    // Если фрагмент уже существует, возвращаем его
    val existingFragment = fragmentManager.findFragmentByTag(fragmentTag) as NavHostFragment?
    if (existingFragment != null) {
        return existingFragment
    }

    // Иначе, создаем новый
    val navHostFragment = NavHostFragment.create(navGraphId)
    fragmentManager.beginTransaction()
        .add(containerId, navHostFragment, fragmentTag)
        .commitNow()
    return navHostFragment
}

private fun FragmentManager.isOnBackStack(backStackName: String): Boolean {
    val backStackCount = backStackEntryCount
    for (index in 0 until backStackCount) {
        if (getBackStackEntryAt(index).name == backStackName) {
            return true
        }
    }
    return false
}

private fun getFragmentTag(index: Int) = "bottomNavigation#$index"