/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing
import com.telekom.citykey.view.main.MainActivity

abstract class MainFragment(
    layoutResId: Int,
    private val containsSensitiveInfo: Boolean = false
) : Fragment(layoutResId) {

    protected open fun handleWindowInsets() = Unit

    protected fun setupToolbar(toolbar: Toolbar) {
        (activity as? MainActivity)?.setupActionBar(toolbar)
        val titleTextView = toolbar.children.firstOrNull { view -> view is TextView }
        titleTextView?.setAccessibilityRole(AccessibilityRole.Heading)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleWindowInsets()
    }

    override fun onResume() {
        super.onResume()
        if (containsSensitiveInfo)
            setSecureFlag()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (containsSensitiveInfo)
            removeSecureFlag()
    }

    protected fun setSecureFlag() {
        if (shouldPreventContentSharing)
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
    }

    protected fun removeSecureFlag() {
        if (shouldPreventContentSharing)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val activity = requireActivity() as MainActivity

        if (activity.tabWasSelected) {
            return if (enter) {
                activity.tabWasSelected = false
                AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_pop_enter_anim)
            } else {
                AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_pop_exit_anim)
            }
        }

        return super.onCreateAnimation(transit, enter, nextAnim)
    }
}
