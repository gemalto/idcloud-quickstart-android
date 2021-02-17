/*
 * MIT License
 *
 * Copyright (c) 2020 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * IMPORTANT: This source code is intended to serve training information purposes only.
 *            Please make sure to review our IdCloud documentation, including security guidelines.
 */

package com.gemalto.mobileprotector.gettingstarted.otp;

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.idp.mobile.ui.UiModule;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputBuilder;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputService;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputUi;
import com.gemalto.idp.mobile.ui.secureinput.SecureKeypadListener;
import com.gemalto.mobileprotector.sdk.ProtectorConfig;

/**
 * Mobile Protector SDK logic for OTP generation.
 */
class OtpLogic {

    private static DialogFragment sDialogFragment;

    /**
     * Generates an OTP.
     *
     * @param token Token to be used for OTP generation.
     * @param pin   PIN.
     * @return Generated OTP and its lifespan wrapped in OtpResult class.
     * @throws IdpException If error during OTP generation occures.
     */
    static OtpResult generateOtp(@NonNull SoftOathToken token, @NonNull AuthInput pin)
            throws IdpException {

        try {

            OathFactory oathFactory = OathService.create(OtpModule.create()).getFactory();
            SoftOathSettings softOathSettings = oathFactory.createSoftOathSettings();
            softOathSettings.setOcraSuite(ProtectorConfig.Otp.getOcraSuite());

            OathDevice oathDevice = oathFactory.createSoftOathDevice(token, softOathSettings);

            SecureString totp = oathDevice.getTotp(pin);
            int otpLifespan = oathDevice.getLastOtpLifespan();

            OtpResult otpResult = new OtpResult(totp.toString(), otpLifespan);
            totp.wipe();

            return otpResult;

        } finally {
            pin.wipe();
        }
    }

    /**
     * Retrieves the PIN.
     *
     * @param activity Activity on which to show the {@code DialogFragment}.
     * @param callback Callback to receive the PIN.
     */
    static void getUserPin(
            @NonNull AppCompatActivity activity,
            @NonNull final OtpPinCallback callback
    ) {
        final SecureInputBuilder builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();
        SecureInputUi secureInputUi = builder.buildKeypad(false, false, false, new SecureKeypadListener() {
            @Override
            public void onKeyPressedCountChanged(int count, int inputField) {
                // Nothing to be done here, but it must be implemented and it could be used to customize the default behavior later
            }

            @Override
            public void onInputFieldSelected(int inputField) {
                // Nothing to be done here, but it must be implemented and it could be used to customize the default behavior later
            }

            @Override
            public void onOkButtonPressed() {
                // Nothing to be done here, but it must be implemented and it could be used to customize the default behavior later
            }

            @Override
            public void onDeleteButtonPressed() {
                // Nothing to be done here, but it must be implemented and it could be used to customize the default behavior later
            }

            @Override
            public void onFinish(PinAuthInput pinAuthInput, PinAuthInput pinAuthInput1) {
                callback.onPinSuccess(pinAuthInput);
                sDialogFragment.dismiss();
                builder.wipe();
            }

            @Override
            public void onError(final String errorMessage) {
                callback.onPinError(errorMessage);
                sDialogFragment.dismiss();
                builder.wipe();
            }
        });

        sDialogFragment = secureInputUi.getDialogFragment();
        sDialogFragment.show(activity.getSupportFragmentManager(), "SECURE PIN");
    }
}
