package net.osmand.plus.views.mapwidgets.widgets.routeinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.AndroidUiHelper;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.settings.backend.preferences.CommonPreference;
import net.osmand.plus.settings.backend.preferences.OsmandPreference;
import net.osmand.plus.settings.enums.WidgetSize;
import net.osmand.plus.utils.AndroidUtils;
import net.osmand.plus.utils.ColorUtilities;
import net.osmand.plus.utils.OsmAndFormatter;
import net.osmand.plus.utils.OsmAndFormatterParams;
import net.osmand.plus.utils.UiUtilities;
import net.osmand.plus.views.layers.MapInfoLayer;
import net.osmand.plus.views.layers.MapInfoLayer.TextState;
import net.osmand.plus.views.layers.base.OsmandMapLayer.DrawSettings;
import net.osmand.plus.views.mapwidgets.WidgetType;
import net.osmand.plus.views.mapwidgets.WidgetsContextMenu;
import net.osmand.plus.views.mapwidgets.WidgetsPanel;
import net.osmand.plus.views.mapwidgets.widgetinterfaces.IComplexWidget;
import net.osmand.plus.views.mapwidgets.widgetinterfaces.ISupportMultiRow;
import net.osmand.plus.views.mapwidgets.widgetinterfaces.ISupportVerticalPanel;
import net.osmand.plus.views.mapwidgets.widgetinterfaces.ISupportWidgetResizing;
import net.osmand.plus.views.mapwidgets.widgets.MapWidget;
import net.osmand.plus.views.mapwidgets.widgetstates.ResizableWidgetState;
import net.osmand.util.Algorithms;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RouteInfoWidget extends MapWidget implements IComplexWidget, ISupportVerticalPanel, ISupportWidgetResizing, ISupportMultiRow {

	private static final String DISPLAY_MODE_PREF_ID = "route_info_widget_display_mode";

	private static final String ARRIVAL_TIME_FORMAT = "HH:mm";

	private final ResizableWidgetState widgetState;
	private final CommonPreference<RouteInfoDisplayMode> displayModePref;

	@Nullable
	protected String customId;
	private boolean isFullRow;
	private MapInfoLayer.TextState textState;

	// views
	private View buttonTappableArea;
	private View buttonBody;
	private View primaryBlock;
	private TextView tvPrimaryValue1;
	private TextView tvSecondaryValue1;
	private TextView tvTertiaryValue1;
	private View secondaryBlock;
	private TextView tvPrimaryValue2;
	private TextView tvSecondaryValue2;
	private TextView tvTertiaryValue2;
	private View blocksDivider;

	public RouteInfoWidget(@NonNull MapActivity mapActivity, @Nullable String customId, @Nullable WidgetsPanel panel) {
		super(mapActivity, WidgetType.ROUTE_INFO);
		this.customId = customId;
		widgetState = new ResizableWidgetState(app, customId, widgetType);
		displayModePref = registerDisplayModePreference(customId);

		setupViews();
//		updateVisibility(false);
		updateVisibility(true); // todo false
	}

	@Override
	protected int getLayoutId() {
		return R.layout.simple_widget_vertical_content_container;
	}

	@LayoutRes
	protected int getContentLayoutId() {
		WidgetSize selectedSize = widgetState.getWidgetSizePref().get();
		return switch (selectedSize) {
			case SMALL -> isFullRow
					? hasIntermediatePoints()
							? R.layout.widget_route_information_small_duo
							: R.layout.widget_route_information_small
					: R.layout.widget_route_information_small_half;
			case MEDIUM -> isFullRow
					? R.layout.widget_route_information_medium
					: R.layout.widget_route_information_medium_half;
			case LARGE -> isFullRow
					? R.layout.widget_route_information_large
					: R.layout.widget_route_information_large_half;
		};
	}

	private void setupViews() {
		LinearLayout container = (LinearLayout) view;
		container.removeAllViews();
		LayoutInflater inflater = UiUtilities.getInflater(mapActivity, nightMode);
		inflater.inflate(getContentLayoutId(), container);
		collectViews();
		updateWidgetView();
		view.setOnLongClickListener(v -> {
			WidgetsContextMenu.showMenu(view, mapActivity, widgetType, customId, null, true, nightMode);
			return true;
		});
	}

	private void collectViews() {
		buttonTappableArea = view.findViewById(R.id.button_tappable_area);
		buttonBody = view.findViewById(R.id.button_body);
		blocksDivider = view.findViewById(R.id.blocks_divider);

		// Initialization of primary block elements
		primaryBlock = view.findViewById(R.id.primary_block);
		tvPrimaryValue1 = view.findViewById(R.id.primary_value_1);
		tvSecondaryValue1 = view.findViewById(R.id.secondary_value_1);
		tvTertiaryValue1 = view.findViewById(R.id.tertiary_value_1);

		// Initialization of secondary block elements
		secondaryBlock = view.findViewById(R.id.secondary_block);
		tvPrimaryValue2 = view.findViewById(R.id.primary_value_2);
		tvSecondaryValue2 = view.findViewById(R.id.secondary_value_2);
		tvTertiaryValue2 = view.findViewById(R.id.tertiary_value_2);
	}

	@Override
	public void updateValueAlign(boolean fullRow) {
	}

	@Override
	public void updateFullRowState(boolean fullRow) {
		if (isFullRow != fullRow) {
			isFullRow = fullRow;
			recreateView();
			if (textState != null) {
				updateColors(textState);
			}
			updateInfo(null);
		}
	}

	private void updateNavigationButtonBg() {
		int color = ColorUtilities.getSecondaryActiveColor(app, nightMode);
		Drawable normal = UiUtilities.createTintedDrawable(app, R.drawable.rectangle_rounded_small, color);

		int rippleDrawableId = nightMode ? R.drawable.ripple_solid_dark_3dp : R.drawable.ripple_solid_light_3dp;
		Drawable selected = AppCompatResources.getDrawable(app, rippleDrawableId);

		Drawable drawable = UiUtilities.getLayeredIcon(normal, selected);
		AndroidUtils.setBackground(view.findViewById(R.id.button_body), drawable);
	}

	@Override
	public void updateInfo(@Nullable DrawSettings drawSettings) {
		updateNavigationInfo();
	}

	private void updateNavigationInfo() {
		// todo calculate and use real data
		tvPrimaryValue1.setText("20:30");
		tvSecondaryValue1.setText("2 h 27 m");
		tvTertiaryValue1.setText("331km");

		if (secondaryBlock != null) {
			boolean secondaryDataPresent = hasIntermediatePoints();
			AndroidUiHelper.setVisibility(secondaryDataPresent, blocksDivider, secondaryBlock);
			if (secondaryDataPresent) {
				tvPrimaryValue2.setText("01:27");
				tvSecondaryValue2.setText("9 h 23 m");
				tvTertiaryValue2.setText("629km");
			}
		}
	}

	@Override
	public void updateColors(@NonNull TextState textState) {
		this.textState = textState;
		this.nightMode = textState.night;
		recreateView();
		view.setBackgroundResource(textState.widgetBackgroundId);
		updateNavigationButtonBg();
		// todo implement

		// todo for tests only
		view.findViewById(R.id.button_tappable_area).setOnClickListener(v -> app.showShortToastMessage("Button clicked"));
	}

	@Override
	public boolean updateVisibility(boolean visible) {
		if (super.updateVisibility(visible)) {
			updateWidgetView();
			return true;
		}
		return false;
	}

	public void updateWidgetView() {
		app.getOsmandMap().getMapLayers().getMapInfoLayer().updateRow(this);
	}

	@Override
	public boolean allowResize() {
		return true;
	}

	@NonNull
	@Override
	public OsmandPreference<WidgetSize> getWidgetSizePref() {
		return widgetState.getWidgetSizePref();
	}

	@Override
	public void recreateViewIfNeeded(@NonNull WidgetsPanel panel) {
	}

	@Override
	public void recreateView() {
		setupViews();
		updateNavigationInfo();
	}

	private boolean hasIntermediatePoints() {
		return false; // todo consider real calculations count
	}

	@NonNull
	public RouteInfoDisplayMode getDisplayMode(@NonNull ApplicationMode appMode) {
		return displayModePref.getModeValue(appMode);
	}

	public void setDisplayMode(@NonNull ApplicationMode appMode, @NonNull RouteInfoDisplayMode displayMode) {
		displayModePref.setModeValue(appMode, displayMode);
	}

	@NonNull
	private CommonPreference<RouteInfoDisplayMode> registerDisplayModePreference(@Nullable String customId) {
		String prefId = Algorithms.isEmpty(customId)
				? DISPLAY_MODE_PREF_ID
				: DISPLAY_MODE_PREF_ID + customId;
		return settings.registerEnumStringPreference(prefId, RouteInfoDisplayMode.ARRIVAL_TIME,
						RouteInfoDisplayMode.values(), RouteInfoDisplayMode.class)
				.makeProfile()
				.cache();
	}

	@NonNull
	public static String formatArrivalTime(long arrivalTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(ARRIVAL_TIME_FORMAT, Locale.getDefault());
		return dateFormat.format(arrivalTime);
	}

	@NonNull
	public static String formatDuration(@NonNull Context ctx, long timeLeft) {
		long diffInMinutes = TimeUnit.MINUTES.convert(timeLeft, TimeUnit.MILLISECONDS);
		String hour = ctx.getString(R.string.int_hour);
		String formattedDuration = Algorithms.formatMinutesDuration((int) diffInMinutes, true);
		return ctx.getString(R.string.ltr_or_rtl_combine_via_space, formattedDuration, hour);
	}

	@NonNull
	public static String formatDistance(@NonNull OsmandApplication ctx, float meters) {
		return OsmAndFormatter.getFormattedDistance(meters, ctx, OsmAndFormatterParams.USE_LOWER_BOUNDS);
	}
}
