package com.mridang.owninfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

import org.acra.ACRA;

/*
 * This class is the main class that provides the widget
 */
public class OwninfoWidget extends ImprovedExtension {

    /*
     * (non-Javadoc)
     * @see com.mridang.owninfo.ImprovedExtension#getIntents()
     */
    @Override
    protected IntentFilter getIntents() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.mridang.owninfo.ImprovedExtension#getTag()
     */
    @Override
    protected String getTag() {
        return getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * @see com.mridang.owninfo.ImprovedExtension#getUris()
     */
    @Override
    protected String[] getUris() {
        return null;
    }

    /*
     * @see
     * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
     * (int)
     */
    @Override
    protected void onUpdateData(int intReason) {

        Log.d(getTag(), "Fetching phone owner information");
        ExtensionData edtInformation = new ExtensionData();
        setUpdateWhenScreenOn(false);

        try {

            Log.d(getTag(), "Get the phone owner from the Me contact");
            Uri uriProfile = Uri.withAppendedPath(Profile.CONTENT_URI, Contacts.Data.CONTENT_DIRECTORY);
            Cursor curOwner = getContentResolver().query(uriProfile,
                    new String[]{Email.ADDRESS, Email.IS_PRIMARY, Profile.DISPLAY_NAME,},
                    ContactsContract.Contacts.Data.MIMETYPE + " = ?",
                    new String[]{Email.CONTENT_ITEM_TYPE},
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

            if (!getString("heading", "").isEmpty()) {
                edtInformation.expandedTitle(getString("heading", ""));
            }

            if (!getString("message", "").isEmpty()) {
                edtInformation.expandedBody(getString("message", ""));
            }

            while (curOwner.moveToNext()) {

                Integer intName = curOwner.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);
                if (!curOwner.getString(intName).isEmpty() && edtInformation.expandedTitle() == null) {
                    edtInformation.expandedTitle(curOwner.getString(intName));
                }

                Integer intAddress = curOwner.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                if (!curOwner.getString(intAddress).isEmpty() && edtInformation.expandedBody() == null) {
                    edtInformation.expandedBody(curOwner.getString(intAddress));
                }

            }

            edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Profile.CONTENT_URI));
            edtInformation.visible(true);
            curOwner.close();

        } catch (Exception e) {
            edtInformation.visible(false);
            Log.e(getTag(), "Encountered an error", e);
            ACRA.getErrorReporter().handleSilentException(e);
        }

        edtInformation.icon(R.drawable.ic_dashclock);
        doUpdate(edtInformation);

    }

    /*
     * (non-Javadoc)
     * @see com.mridang.owninfo.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
     */
    @Override
    protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
        onUpdateData(UPDATE_REASON_MANUAL);
    }

}