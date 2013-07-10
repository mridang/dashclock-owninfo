package com.mridang.owninfo;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class OwninfoWidget extends DashClockExtension {

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("OwninfoWidget", "Created");
		BugSenseHandler.initAndStartSession(this, "8550857e");

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int arg0) {

		Log.d("OwninfoWidget", "Fetching phone owner information");
		ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(false);

		try {

			Log.d("OwninfoWidget", "Get the phone owner from the Me contact");

			Cursor curOwner = getContentResolver()
					.query(Uri.withAppendedPath(
							ContactsContract.Profile.CONTENT_URI,
							ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
							new String[] { 
									ContactsContract.CommonDataKinds.Email.ADDRESS,
									ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
									ContactsContract.Profile.DISPLAY_NAME, },
							ContactsContract.Contacts.Data.MIMETYPE + " = ?",
							new String[] { 
								ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },
							ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");

			if (curOwner.moveToFirst()) {

				edtInformation.status(curOwner.getString(curOwner.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)));
				edtInformation.expandedBody(curOwner.getString(curOwner.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
				edtInformation.visible(true);

			}

			curOwner.close();

		} catch (Exception e) {
			Log.e("OwninfoWidget", "Encountered an error", e);
			BugSenseHandler.sendException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("OwninfoWidget", "Done");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();
		Log.d("OwninfoWidget", "Destroyed");
		BugSenseHandler.closeSession(this);

	}

}