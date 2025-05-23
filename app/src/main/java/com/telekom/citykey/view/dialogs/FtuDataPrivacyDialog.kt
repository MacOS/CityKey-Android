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

package com.telekom.citykey.view.dialogs

import android.content.DialogInterface
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.ViewCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacyFtuDialogBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.LinkAccessibilityHelper
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class FtuDataPrivacyDialog : FullScreenBottomSheetDialogFragment(R.layout.data_privacy_ftu_dialog) {

    private val adjustManager: AdjustManager by inject()
    private val preferencesHelper: PreferencesHelper by inject()
    private val binding: DataPrivacyFtuDialogBinding by viewBinding(DataPrivacyFtuDialogBinding::bind)

    init {
        isCancelable = false
    }

    override fun setupUI() {

        setAccessibilityRoleForToolbarTitle(binding.toolbar)

        binding.btnChangeSettings.setupOutlineStyle()
        binding.btnChangeSettings.setOnClickListener {
            DataPrivacySettingsDialog(
                acceptedListener = {
                    preferencesHelper.saveConfirmedTrackingTerms()
                    dismiss()
                }
            )
                .showDialog(parentFragmentManager, "DataPrivacySettingsDialog")
        }

        binding.btnAcceptAll.setOnClickListener {
            adjustManager.updateTrackingPermissions(true)
            preferencesHelper.saveConfirmedTrackingTerms()
            dismiss()
        }

        setupDescription()
    }

    private fun setupDescription() {
        val dpnLink = getString(R.string.dialog_dpn_ftu_dpn_link)
        val continueLink = getString(R.string.dialog_dpn_ftu_continue_link)
        val format = getString(
            R.string.dialog_dpn_ftu_text_format,
            dpnLink,
            continueLink
        )
        val dpnLinkIndex = format.indexOf(dpnLink)
        val continueLinkIndex = format.indexOf(continueLink)
        val textSpan = SpannableString(format)

        textSpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    DataPrivacyNoticeDialog()
                        .showDialog(parentFragmentManager, "DataPrivacyNoticeDialog")
                }
            },
            dpnLinkIndex, dpnLinkIndex + dpnLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textSpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    adjustManager.updateTrackingPermissions(false)
                    dismiss()
                }
            },
            continueLinkIndex, continueLinkIndex + continueLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textBody.apply {
            text = textSpan
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(getColor(R.color.oscaColor))
            ViewCompat.setAccessibilityDelegate(
                this,
                LinkAccessibilityHelper(
                    linkTextView = this,
                    linkAccessibilityId = "linkId"
                )
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        adjustManager.trackAppLaunchedEvent()
    }
}
