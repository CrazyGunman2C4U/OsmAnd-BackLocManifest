package net.osmand.plus.activities.search;

import java.util.List;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.search.SearchActivity.SearchActivityChild;
import net.osmand.plus.dialogs.DirectionsDialogs;
import net.osmand.plus.helpers.SearchHistoryHelper;
import net.osmand.plus.helpers.SearchHistoryHelper.HistoryEntry;
import net.osmand.util.MapUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;


public class SearchHistoryFragment extends ListFragment implements SearchActivityChild {
	private LatLon location;
	private SearchHistoryHelper helper;
	private Button clearButton;
	public static final String SEARCH_LAT = SearchActivity.SEARCH_LAT;
	public static final String SEARCH_LON = SearchActivity.SEARCH_LON;
	private HistoryAdapter historyAdapter;
	private RecentIcons recentIcons;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.search_history, container, false);
		clearButton = (Button) view.findViewById(R.id.clearAll);
		clearButton.setText(R.string.clear_all);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				helper.removeAll();
				historyAdapter.clear();
				clearButton.setVisibility(View.GONE);
			}
		});
		recentIcons = new RecentIcons(getActivity());
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		helper = SearchHistoryHelper.getInstance((OsmandApplication) getActivity().getApplicationContext());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		historyAdapter = new HistoryAdapter(helper.getHistoryEntries());
		setListAdapter(historyAdapter);
		setHasOptionsMenu(true);
	}


	@Override
	public void onResume() {
		super.onResume();

		//Hardy: onResume() code is needed so that search origin is properly reflected in tab contents when origin has been changed on one tab, then tab is changed to another one.
		location = null;
		FragmentActivity activity = getActivity();
		Intent intent = activity.getIntent();
		if (intent != null) {
			double lat = intent.getDoubleExtra(SEARCH_LAT, 0);
			double lon = intent.getDoubleExtra(SEARCH_LON, 0);
			if (lat != 0 || lon != 0) {
				historyAdapter.location = new LatLon(lat, lon);
			}
		}
		if (location == null && activity instanceof SearchActivity) {
			location = ((SearchActivity) activity).getSearchPoint();
		}
		if (location == null) {
			location = ((OsmandApplication) activity.getApplication()).getSettings().getLastKnownMapLocation();
		}
		historyAdapter.clear();
		for (HistoryEntry entry : helper.getHistoryEntries()) {
			historyAdapter.add(entry);
		}
		locationUpdate(location);
		clearButton.setVisibility(historyAdapter.isEmpty() ? View.GONE : View.VISIBLE);

	}

	@Override
	public void locationUpdate(LatLon l) {
		//location = l;
		if (historyAdapter != null) {
			historyAdapter.updateLocation(l);
		}
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		HistoryEntry model = ((HistoryAdapter) getListAdapter()).getItem(position);
		selectModel(model, v);
	}

	private void selectModel(final HistoryEntry model, View v) {
		PointDescription name = model.getName();
		boolean light = ((OsmandApplication) getActivity().getApplication()).getSettings().isLightContent();
		final PopupMenu optionsMenu = new PopupMenu(getActivity(), v);
		OsmandSettings settings = ((OsmandApplication) getActivity().getApplication()).getSettings();
		DirectionsDialogs.createDirectionsActionsPopUpMenu(optionsMenu, new LatLon(model.getLat(), model.getLon()),
				model, name, settings.getLastKnownMapZoom(), getActivity(), true);
		MenuItem item = optionsMenu.getMenu().add(
				R.string.edit_filter_delete_menu_item).setIcon(light ?
				R.drawable.ic_action_delete_light : R.drawable.ic_action_delete_dark);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				helper.remove(model);
				historyAdapter.remove(model);
				return true;
			}
		});
		optionsMenu.show();
	}

	class HistoryAdapter extends ArrayAdapter<HistoryEntry> {
		private LatLon location;
		

		public void updateLocation(LatLon l) {
			location = l;
			notifyDataSetChanged();
		}

		public HistoryAdapter(List<HistoryEntry> list) {
			super(getActivity(), R.layout.search_history_list_item, list);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.search_history_list_item, parent, false);
			}
			final HistoryEntry historyEntry = getItem(position);
			udpateHistoryItem(historyEntry, row, location, getActivity(), recentIcons);
			ImageButton options = (ImageButton) row.findViewById(R.id.options);
			options.setVisibility(View.VISIBLE);
			options.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					selectModel(historyEntry, v);
				}
			});
			return row;
		}
	}
	
	public static class RecentIcons {
		
		private Drawable arrowImage;
		private Drawable addressIcon;
		private Drawable favoriteIcon;
		private Drawable locationIcon;
		private Drawable poiIcon;
		private Drawable wptIcon;
		private Drawable audioNoteIcon;
		private Drawable videoNoteIcon;
		private Drawable photoNoteIcon;
		
		public RecentIcons(Activity a) {
			loadIcons(a, (OsmandApplication) a.getApplication());
		}
		
		private void loadIcons(Context ctx, OsmandApplication app) {
			arrowImage = ctx.getResources().getDrawable(R.drawable.ic_destination_arrow_white);
			arrowImage = arrowImage.mutate();
			arrowImage.setColorFilter(ctx.getResources().getColor(R.color.color_distance), PorterDuff.Mode.MULTIPLY);
			addressIcon = ctx.getResources().getDrawable(R.drawable.ic_type_address);
			favoriteIcon = ctx.getResources().getDrawable(R.drawable.ic_type_favorites);
			locationIcon = ctx.getResources().getDrawable(R.drawable.ic_type_coordinates);
			poiIcon = ctx.getResources().getDrawable(R.drawable.ic_type_info);
			wptIcon = ctx.getResources().getDrawable(R.drawable.ic_type_waypoint);
			audioNoteIcon = ctx.getResources().getDrawable(R.drawable.ic_type_audio);
			videoNoteIcon = ctx.getResources().getDrawable(R.drawable.ic_type_video);
			photoNoteIcon = ctx.getResources().getDrawable(R.drawable.ic_type_img);
			if (app.getSettings().isLightContent()) {
				addressIcon = addressIcon.mutate();
				addressIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				favoriteIcon = favoriteIcon.mutate();
				favoriteIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				locationIcon = locationIcon.mutate();
				locationIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				poiIcon = poiIcon.mutate();
				poiIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				wptIcon = wptIcon.mutate();
				wptIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				audioNoteIcon = audioNoteIcon.mutate();
				audioNoteIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				videoNoteIcon = videoNoteIcon.mutate();
				videoNoteIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
				photoNoteIcon = photoNoteIcon.mutate();
				photoNoteIcon.setColorFilter(ctx.getResources().getColor(R.color.icon_color_light), PorterDuff.Mode.MULTIPLY);
			}
		}

	}
			
	public static void udpateHistoryItem(final HistoryEntry historyEntry, View row,
			LatLon location, Activity activity, RecentIcons ri) {
		TextView nameText = (TextView) row.findViewById(R.id.name);
		TextView distanceText = (TextView) row.findViewById(R.id.distance);
		ImageView direction = (ImageView) row.findViewById(R.id.direction);
		direction.setImageDrawable(ri.arrowImage);
		String distance = "";
		if (location != null) {
			int dist = (int) (MapUtils.getDistance(location, historyEntry.getLat(), historyEntry.getLon()));
			distance = OsmAndFormatter.getFormattedDistance(dist, (OsmandApplication) activity.getApplication()) + "  ";
		}
		distanceText.setText(distance);
		PointDescription pd = historyEntry.getName();
		nameText.setText(pd.getSimpleName(activity, historyEntry.getLat(), historyEntry.getLon()), BufferType.SPANNABLE);
		ImageView icon = ((ImageView) row.findViewById(R.id.icon));

		if (historyEntry.getName().isAddress()) {
			icon.setImageDrawable(ri.addressIcon);
		} else if (historyEntry.getName().isFavorite()) {
			icon.setImageDrawable(ri.favoriteIcon);
		} else if (historyEntry.getName().isLocation()) {
			icon.setImageDrawable(ri.locationIcon);
		} else if (historyEntry.getName().isPoi()) {
			icon.setImageDrawable(ri.poiIcon);
		} else if (historyEntry.getName().isWpt()) {
			icon.setImageDrawable(ri.wptIcon);
		} else if (historyEntry.getName().isAudioNote()) {
			icon.setImageDrawable(ri.audioNoteIcon);
		} else if (historyEntry.getName().isVideoNote()) {
			icon.setImageDrawable(ri.videoNoteIcon);
		}else if (historyEntry.getName().isPhotoNote()) {
			icon.setImageDrawable(ri.photoNoteIcon);
		}  else {
			icon.setImageDrawable(ri.addressIcon);
		}

		String typeName = historyEntry.getName().getTypeName();
		if (typeName != null && !typeName.isEmpty()) {
			row.findViewById(R.id.type_name_icon).setVisibility(View.VISIBLE);
			((TextView) row.findViewById(R.id.type_name)).setText(typeName);
		} else {
			row.findViewById(R.id.type_name_icon).setVisibility(View.GONE);
			((TextView) row.findViewById(R.id.type_name)).setText("");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu onCreate, MenuInflater inflater) {
		if (getActivity() instanceof SearchActivity) {
			((SearchActivity) getActivity()).getClearToolbar(false);
		}
	}

	public OsmandApplication getMyApplication() {
		return (OsmandApplication) getActivity().getApplication();
	}
}
