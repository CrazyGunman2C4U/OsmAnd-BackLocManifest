package net.osmand.plus.backup.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import net.osmand.plus.R;
import net.osmand.plus.utils.UiUtilities;
import net.osmand.plus.widgets.dialogbutton.DialogButtonType;
import net.osmand.plus.backup.ui.DeleteAllDataConfirmationBottomSheet.OnConfirmDeletionListener;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.BottomSheetItemWithDescription;

public class DeleteAllDataBottomSheet extends MenuBottomSheetDialogFragment implements OnConfirmDeletionListener {

	public static final String TAG = DeleteAllDataBottomSheet.class.getSimpleName();

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		LayoutInflater themedInflater = UiUtilities.getInflater(requireContext(), nightMode);
		View titleView = themedInflater.inflate(R.layout.backup_delete_data, null);
		BaseBottomSheetItem item = new BottomSheetItemWithDescription.Builder()
				.setDescription(getString(R.string.backup_delete_all_data_warning))
				.setTitle(getString(R.string.backup_delete_all_data))
				.setIcon(getIcon(R.drawable.ic_action_alert, R.color.color_osm_edit_delete))
				.setCustomView(titleView)
				.create();
		items.add(item);
	}

	@Override
	protected void onRightBottomButtonClick() {
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager != null) {
			DeleteAllDataConfirmationBottomSheet.showInstance(fragmentManager, this);
		}
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.shared_string_cancel;
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.backup_delete_all_data;
	}

	@Override
	protected DialogButtonType getRightBottomButtonType() {
		return DialogButtonType.PRIMARY_HARMFUL;
	}

	@Override
	public void onDeletionConfirmed() {
		Fragment fragment = getTargetFragment();
		if (fragment instanceof OnConfirmDeletionListener) {
			((OnConfirmDeletionListener) fragment).onDeletionConfirmed();
		}
		dismiss();
	}

	@Override
	protected boolean useVerticalButtons() {
		return true;
	}

	@Override
	public int getFirstDividerHeight() {
		return getResources().getDimensionPixelSize(R.dimen.dialog_content_margin);
	}

	public static void showInstance(@NonNull FragmentManager fragmentManager, Fragment target) {
		if (!fragmentManager.isStateSaved()) {
			DeleteAllDataBottomSheet fragment = new DeleteAllDataBottomSheet();
			fragment.setTargetFragment(target, 0);
			fragment.show(fragmentManager, TAG);
		}
	}
}