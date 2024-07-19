package org.joinmastodon.android.utils;

import android.net.Uri;
import android.util.Patterns;

import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.regex.Matcher;

// Inspired by https://github.com/GeopJr/Tuba/blob/91a036edff9ab1ffb38d5b54a33023e5db551051/src/Utils/Tracking.vala

public class Tracking{
	/* https://github.com/brave/brave-core/blob/face8d58ab81422480c8c05b9ba5d518e1a2d227/components/query_filter/utils.cc#L23-L119 */
	private static final String[] TRACKING_IDS = {
			// Strip any utm_ based ones
			"utm_",
			// https://github.com/brave/brave-browser/issues/4239
			"fbclid", "gclid", "msclkid", "mc_eid",
			// New Facebook one
			"mibexid",
			// https://github.com/brave/brave-browser/issues/9879
			"dclid",
			// https://github.com/brave/brave-browser/issues/13644
			"oly_anon_id", "oly_enc_id",
			// https://github.com/brave/brave-browser/issues/11579
			"_openstat",
			// https://github.com/brave/brave-browser/issues/11817
			"vero_conv", "vero_id",
			// https://github.com/brave/brave-browser/issues/13647
			"wickedid",
			// https://github.com/brave/brave-browser/issues/11578
			"yclid",
			// https://github.com/brave/brave-browser/issues/8975
			"__s",
			// https://github.com/brave/brave-browser/issues/17451
			"rb_clickid",
			// https://github.com/brave/brave-browser/issues/17452
			"s_cid",
			// https://github.com/brave/brave-browser/issues/17507
			"ml_subscriber", "ml_subscriber_hash",
			// https://github.com/brave/brave-browser/issues/18020
			"twclid",
			// https://github.com/brave/brave-browser/issues/18758
			"gbraid", "wbraid",
			// https://github.com/brave/brave-browser/issues/9019
			"_hsenc", "__hssc", "__hstc", "__hsfp", "hsCtaTracking",
			// https://github.com/brave/brave-browser/issues/22082
			"oft_id", "oft_k", "oft_lk", "oft_d", "oft_c", "oft_ck", "oft_ids", "oft_sk",
			// https://github.com/brave/brave-browser/issues/11580
			"igshid",
			// Instagram Threads
			"ad_id", "adset_id", "campaign_id", "ad_name", "adset_name", "campaign_name", "placement",
			// Reddit
			"share_id", "ref", "ref_share",
	};

	/**
	 * Tries to remove tracking parameters from a URL.
	 *
	 * @param url The original URL with tracking parameters
	 * @return The URL with the tracking parameters removed.
	 */
	@NonNull
	public static String removeTrackingParameters(@NonNull String url) {
		Uri uri = Uri.parse(url);
		Uri.Builder uriBuilder = uri.buildUpon().clearQuery();

		// Iterate over existing parameters and add them back if they are not tracking parameters
		for (String paramName : uri.getQueryParameterNames()) {
			if (!isTrackingParameter(paramName)) {
				for (String paramValue : uri.getQueryParameters(paramName)) {
					uriBuilder.appendQueryParameter(paramName, paramValue);
				}
			}
		}

		return uriBuilder.build().toString();
	}

	/**
	 * Cleans URLs within the provided text, removing the tracking parameters from them.
	 *
	 * @param text The text that may contain URLs.
	 * @return The given text with cleaned URLs.
	 */
	public static String cleanUrlsInText(String text) {
		Matcher matcher = Patterns.WEB_URL.matcher(text);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String url = matcher.group();
			matcher.appendReplacement(sb, removeTrackingParameters(url));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Returns true if the given parameter is used for tracking.
	 */
	private static boolean isTrackingParameter(String parameter) {
		return Arrays.stream(TRACKING_IDS).anyMatch(trackingId -> parameter.toLowerCase().contains(trackingId));
	}
}