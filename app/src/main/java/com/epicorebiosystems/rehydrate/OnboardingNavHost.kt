package com.epicorebiosystems.rehydrate

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.epicorebiosystems.rehydrate.modelData.EBSDeviceMonitor
import com.epicorebiosystems.rehydrate.modelData.ModelData
import com.epicorebiosystems.rehydrate.modelData.isValidDeviceSerialNumber
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountCheckEmailView
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountChooseCurrentEnterprise
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountConfirmEnterprise
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountEnterCodeView
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountEnterEmailAddress
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountGetStartedView
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountMainView
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountUserExistsScreen
import com.epicorebiosystems.rehydrate.onboarding.CreateAccountVerificationFailedView
import com.epicorebiosystems.rehydrate.onboarding.InitialSetupView
import com.epicorebiosystems.rehydrate.onboarding.LogInAccountCreatedView
import com.epicorebiosystems.rehydrate.onboarding.LogInCheckEmailView
import com.epicorebiosystems.rehydrate.onboarding.LogInEnterCodeView
import com.epicorebiosystems.rehydrate.onboarding.LogInEnterEmailAddressScreen
import com.epicorebiosystems.rehydrate.onboarding.LogInMainView
import com.epicorebiosystems.rehydrate.onboarding.LogInNavToEmailView
import com.epicorebiosystems.rehydrate.onboarding.LogInTroubleshootingEmailView
import com.epicorebiosystems.rehydrate.onboarding.LogInUserExistsScreen
import com.epicorebiosystems.rehydrate.onboarding.LogInVerificationFailedView
import com.epicorebiosystems.rehydrate.onboarding.StartOnboardingView
import com.epicorebiosystems.rehydrate.onboarding.step2_personalize.Step2PersonalizeMainView
import com.epicorebiosystems.rehydrate.onboarding.step2_personalize.Step2SharingMainView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleIdentify
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleMainView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleManuallyView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleScanView
import com.epicorebiosystems.rehydrate.onboarding.step3_pairmodule.Step3PairModuleUnresponsive
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.Step4AttachModule
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.armbandapplication.Step4ModuleApplicationStrapTighten
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchApplicationApplySleeveView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchApplicationApplyView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchApplicationCleanSkinView
import com.epicorebiosystems.rehydrate.onboarding.step4_attachmodule.patchapplication.Step4PatchApplicationMainView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5ModuleButtonTrackIntakeView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewEndOfShiftView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewMainView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewNotificationsView
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewSetupComplete
import com.epicorebiosystems.rehydrate.onboarding.step5_overview.Step5OverviewTrackIntakeView
import com.epicorebiosystems.rehydrate.sharedViews.EditEnterpriseScreen
import com.epicorebiosystems.rehydrate.sharedViews.EpicoreLegalScreens
import com.epicorebiosystems.rehydrate.sharedViews.EpicoreRequiredView
import com.epicorebiosystems.rehydrate.sharedViews.QrScannerScreen
import com.epicorebiosystems.rehydrate.sharedViews.VerifyPhysiologyInfoView

interface OnboardingScreenDestination {
    val route: String
}

sealed class OnboardingScreens() {
    // Main Onboarding screens
    object StartOnboardingView : OnboardingScreenDestination { override val route = "start_onboarding" }

    object InitialSetupView : OnboardingScreenDestination { override val route = "initial_setup" }

    // Log In Flow
    object LogInEnterEmailAddressScreen : OnboardingScreenDestination { override val route = "login_enter_email_address" }
    object LogInUserExistsScreen : OnboardingScreenDestination { override val route = "login_user_exists" }
    object LogInCheckEmailView : OnboardingScreenDestination { override val route = "login_check_email" }
    object LogInMainView : OnboardingScreenDestination { override val route = "login_main_view" }
    object LogInNavToEmailView : OnboardingScreenDestination { override val route = "login_nav_to_email" }
    object LogInEnterCodeView : OnboardingScreenDestination { override val route = "login_enter_code" }
    object LogInTroubleshootingEmailView : OnboardingScreenDestination { override val route = "login_troubleshooting_email" }
    object LogInVerificationFailedView : OnboardingScreenDestination { override val route = "login_verification_failed" }
    object LogInAccountCreatedView : OnboardingScreenDestination { override val route = "login_account_created" }
    object LogInPairModuleView : OnboardingScreenDestination { override val route = "login_pair_new_module_main" }
    object LogInModuleIdentifyView : OnboardingScreenDestination { override val route = "login_module_identify" }
    object LogInModuleScanView : OnboardingScreenDestination { override val route = "login_module_scan" }
    object LogInModuleManuallyView : OnboardingScreenDestination { override val route = "login_module_manually" }
    object LogInVerifyPhysiologyInfoView : OnboardingScreenDestination { override val route = "login_module_physiology" }

    // Create Account Flow
    object CreateAccountGetStartedView : OnboardingScreenDestination { override val route = "create_account_get_started" }
    object CreateAccountMainView : OnboardingScreenDestination { override val route = "create_account_main" }
    object CreateAccountConfirmEnterprise : OnboardingScreenDestination { override val route = "create_account_confirm_enterprise" }
    object CreateAccountEnterEmailAddress : OnboardingScreenDestination { override val route = "create_account_enter_email" }
    object CreateAccountUserExistsScreen : OnboardingScreenDestination { override val route = "create_account_user_exists" }
    object CreateAccountCheckEmailView : OnboardingScreenDestination { override val route = "create_account_check_email" }
    object CreateAccountEnterCodeView : OnboardingScreenDestination { override val route = "create_account_enter_code" }
    object CreateAccountVerificationFailedView : OnboardingScreenDestination { override val route = "create_account_verification_fail" }
    object CreateAccountChooseCurrentEnterprise : OnboardingScreenDestination { override val route = "create_account_choose_enterprise" }

    // Step 2
    object Step2PersonalizeMainView: OnboardingScreenDestination { override val route = "step2_personalize_main_view" }
    object Step2SharingMainView: OnboardingScreenDestination { override val route = "step2_sharing_main_view" }

    // Step 3
    object Step3PairModuleMainView: OnboardingScreenDestination { override val route = "step3_pair_module_main_view" }
    object Step3PairModuleScanView: OnboardingScreenDestination { override val route = "step3_pair_module_scan_view" }
    object Step3PairModuleManuallyView: OnboardingScreenDestination { override val route = "step3_pair_module_manually_view" }
    object Step3PairModuleUnresponsive: OnboardingScreenDestination { override val route = "step3_pair_module_unresponsive_view" }
    object Step3PairModuleIdentify: OnboardingScreenDestination { override val route = "step3_pair_module_identify_view" }

    // Step 4
    object Step4AttachModule: OnboardingScreenDestination { override val route = "step4_attach_module_start" }
    object Step4PatchApplicationMainView: OnboardingScreenDestination { override val route = "step4_patch_app_main_view" }
    object Step4PatchApplicationCleanSkinView: OnboardingScreenDestination { override val route = "step4_patch_app_clean_skin_view" }
    object Step4PatchApplicationApplyView: OnboardingScreenDestination { override val route = "step4_patch_app_apply_view" }
    object Step4PatchApplicationApplySleeveView: OnboardingScreenDestination { override val route = "step4_patch_app_sleeve_view" }
    object Step4ModuleApplicationStrapTighten: OnboardingScreenDestination { override val route = "step4_patch_app_strap" }

    // Step 5
    object Step5OverviewMainView: OnboardingScreenDestination { override val route = "step5_overview_view" }
    object Step5OverviewNotificationsView: OnboardingScreenDestination { override val route = "step5_notifications_view" }
    object Step5OverviewTrackIntakeView: OnboardingScreenDestination { override val route = "step5_track_intake_view" }
    object Step5OverviewSetupComplete: OnboardingScreenDestination { override val route = "step5_track_complete_view" }
    object Step5ModuleButtonTrackIntakeView: OnboardingScreenDestination { override val route = "step5_track_intake_button" }
    object Step5OverviewEndOfShiftView: OnboardingScreenDestination { override val route = "step5_end_of_shift" }

}

@Composable
fun OnboardingNavHost(chViewModel: ModelData, ebsMonitor: EBSDeviceMonitor, modifier: Modifier = Modifier) {
    val navController = rememberNavController().apply {
        NavigationViewTrackingEffect(navController = this)
    }

    chViewModel.isCHDeviceConnected = false
    chViewModel._isSensorConnected.value = false

    NavHost(navController, startDestination = OnboardingScreens.StartOnboardingView.route, modifier = modifier) {

        composable(OnboardingScreens.StartOnboardingView.route) {
            StartOnboardingView(chViewModel = chViewModel, navController = navController)
        }

        // terms and privacy routes
        composable(SettingsSubScreens.PrivacyPolicy.route!!) {
            EpicoreRequiredView(chViewModel = chViewModel, navController = navController, EpicoreLegalScreens.PRIVACY)
        }

        composable(SettingsSubScreens.TermsConditions.route!!) {
            EpicoreRequiredView(chViewModel = chViewModel, navController = navController, EpicoreLegalScreens.TERMS)
        }

        composable(OnboardingScreens.LogInEnterEmailAddressScreen.route) {
            LogInEnterEmailAddressScreen(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInUserExistsScreen.route) {
            LogInUserExistsScreen(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInEnterCodeView.route) {
            LogInEnterCodeView(chViewModel = chViewModel, navController = navController, emailCode = null)
        }

        composable(OnboardingScreens.LogInTroubleshootingEmailView.route) {
            LogInTroubleshootingEmailView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInMainView.route) {
            LogInMainView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInVerificationFailedView.route) {
            LogInVerificationFailedView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInAccountCreatedView.route) {
            LogInAccountCreatedView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInPairModuleView.route) {
            Step3PairModuleMainView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, isNewPair = true, navController = navController, isOnboarding = true, updateHideBottomBar = {})
        }

        composable(OnboardingScreens.LogInNavToEmailView.route) {
            LogInNavToEmailView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInCheckEmailView.route) {
            LogInCheckEmailView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.LogInModuleScanView.route) {
            Step3PairModuleScanView(chViewModel = chViewModel, ebsMonitor, isNewPair = false, isOnboarding = true, navController = navController)
        }

        composable(OnboardingScreens.LogInModuleManuallyView.route) {
            Step3PairModuleManuallyView(chViewModel = chViewModel, ebsMonitor, isNewPair = false, isOnboarding = true, navController = navController)
        }

        composable(OnboardingScreens.LogInModuleIdentifyView.route) {
            Step3PairModuleIdentify(chViewModel = chViewModel, ebsMonitor = ebsMonitor, isNewPair = true, isOnboarding = true, navController = navController, updateHideBottomBar = {})
        }

        composable(OnboardingScreens.LogInVerifyPhysiologyInfoView.route) {
            VerifyPhysiologyInfoView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, navController = navController, isOnboarding = true, updateHideBottomBar = {})
        }

        composable(OnboardingScreens.CreateAccountGetStartedView.route) {
            CreateAccountGetStartedView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountMainView.route) {
            CreateAccountMainView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountConfirmEnterprise.route) {
            CreateAccountConfirmEnterprise(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountEnterEmailAddress.route) {
            CreateAccountEnterEmailAddress(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountUserExistsScreen.route) {
            CreateAccountUserExistsScreen(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountCheckEmailView.route) {
            CreateAccountCheckEmailView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountEnterCodeView.route) {
            CreateAccountEnterCodeView(chViewModel = chViewModel, navController = navController, emailCode = null)
        }

        composable(OnboardingScreens.CreateAccountVerificationFailedView.route) {
            CreateAccountVerificationFailedView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.CreateAccountChooseCurrentEnterprise.route) {
            CreateAccountChooseCurrentEnterprise(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.InitialSetupView.route) {
            InitialSetupView(chViewModel = chViewModel, navController = navController)
        }

        // Deep link support for email codes
        composable(
            "code",
            deepLinks = listOf(
                navDeepLink { uriPattern = "rehydrate://epicore.dev/code/{emailCode}" },
                navDeepLink { uriPattern = "rehydrate://ch.epicorebiosystems.com/code/{emailCode}"}
            ),
        ) { backStackEntry ->
            val emailCode = backStackEntry.arguments?.getString("emailCode")
            Log.d("DEEPLINK", "emailCode = $emailCode")

            chViewModel.isCreateAccountFlow.value = chViewModel.getIsCreateAccountFlow()

            if (chViewModel.isCreateAccountFlow.value) {
                CreateAccountEnterCodeView(
                    chViewModel = chViewModel,
                    navController = navController,
                    emailCode = emailCode
                )
            }
            else {
                LogInEnterCodeView(
                    chViewModel = chViewModel,
                    navController = navController,
                    emailCode = emailCode
                )
            }
        }

        // Used by Step1SignUpAccountCreatedView to edit enterprise info
        composable(SharedScreens.EditEnterprise.route!!) {
            EditEnterpriseScreen(navController = navController, ebsDeviceMonitor = ebsMonitor, chViewModel = chViewModel, updateHideBottomBar = {})
        }

        composable(SharedScreens.ScanEnterpriseQRCode.route!!) {
            QrScannerScreen(navController, Modifier.fillMaxSize(), onQrCodeScanned = { qrCode ->
                // TODO: Might need to validate enterprise site ID before navigating back
                chViewModel.onboardingEnterpriseId.value = qrCode
                navController.navigateUp()
            })
        }

        composable(SharedScreens.ScanDeviceQRCode.route!!) {
            QrScannerScreen(navController, Modifier.fillMaxSize(), onQrCodeScanned = { qrCode ->
                chViewModel._qrPairingState.value = chViewModel._qrPairingState.value.copy(showLoading = true)
                if (isValidDeviceSerialNumber(qrCode)) {
                    chViewModel.deviceSN.value = qrCode
                    navController.navigateUp()
                }
            })
        }

        // Step 2
        composable(OnboardingScreens.Step2PersonalizeMainView.route) {
            Step2PersonalizeMainView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, navController = navController)
        }

        composable(OnboardingScreens.Step2SharingMainView.route) {
            Step2SharingMainView(chViewModel = chViewModel, navController = navController)
        }

        // Step 3
        composable(OnboardingScreens.Step3PairModuleMainView.route) {
            Step3PairModuleMainView(chViewModel = chViewModel, ebsDeviceMonitor = ebsMonitor, isNewPair = false, navController = navController, isOnboarding = true, updateHideBottomBar = {})
        }

        composable(OnboardingScreens.Step3PairModuleScanView.route) {
            Step3PairModuleScanView(chViewModel = chViewModel, ebsMonitor, isNewPair = false, isOnboarding = false, navController = navController)
        }

        composable(OnboardingScreens.Step3PairModuleManuallyView.route) {
            Step3PairModuleManuallyView(chViewModel = chViewModel, ebsMonitor, isNewPair = false, isOnboarding = false, navController = navController)
        }

        composable(OnboardingScreens.Step3PairModuleUnresponsive.route) {
            Step3PairModuleUnresponsive(chViewModel, navController = navController, isNewPair = false, isOnboarding = true, updateHideBottomBar = {})
        }

        composable(OnboardingScreens.Step3PairModuleIdentify.route) {
            Step3PairModuleIdentify(chViewModel = chViewModel, ebsMonitor = ebsMonitor, isNewPair = false, isOnboarding = false, navController = navController, updateHideBottomBar = {})
        }

        // Step 4
        composable(OnboardingScreens.Step4AttachModule.route) {
            Step4AttachModule(ebsMonitor, navController = navController)
        }

        composable(OnboardingScreens.Step4PatchApplicationMainView.route) {
            Step4PatchApplicationMainView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step4PatchApplicationCleanSkinView.route) {
            Step4PatchApplicationCleanSkinView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step4PatchApplicationApplyView.route) {
            Step4PatchApplicationApplyView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step4PatchApplicationApplySleeveView.route) {
            Step4PatchApplicationApplySleeveView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step4ModuleApplicationStrapTighten.route) {
            Step4ModuleApplicationStrapTighten(chViewModel = chViewModel, navController = navController)
        }

        // Step 5
        composable(OnboardingScreens.Step5OverviewMainView.route) {
            Step5OverviewMainView(chViewModel, ebsMonitor, navController = navController)
        }

        composable(OnboardingScreens.Step5OverviewNotificationsView.route) {
            Step5OverviewNotificationsView(chViewModel, ebsMonitor, navController = navController)
        }

        composable(OnboardingScreens.Step5OverviewTrackIntakeView.route) {
            Step5OverviewTrackIntakeView(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step5OverviewSetupComplete.route) {
            Step5OverviewSetupComplete(chViewModel = chViewModel, navController = navController)
        }

        composable(OnboardingScreens.Step5ModuleButtonTrackIntakeView.route) {
            Step5ModuleButtonTrackIntakeView(chViewModel = chViewModel, ebsMonitor, navController = navController)
        }

        composable(OnboardingScreens.Step5OverviewEndOfShiftView.route) {
            Step5OverviewEndOfShiftView(ebsMonitor, navController = navController)
        }
    }
}