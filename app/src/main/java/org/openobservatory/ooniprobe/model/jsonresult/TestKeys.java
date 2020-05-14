package org.openobservatory.ooniprobe.model.jsonresult;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.openobservatory.ooniprobe.R;

import java.util.ArrayList;
import java.util.Locale;

public class TestKeys {
	public static final String BLOCKED = "blocked";
	@SerializedName("blocking")
	public String blocking;
	@SerializedName("accessible")
	public String accessible;
	@SerializedName("sent")
	public ArrayList<String> sent;
	@SerializedName("received")
	public ArrayList<String> received;
	@SerializedName("failure")
	public String failure;
	@SerializedName("whatsapp_endpoints_status")
	public String whatsapp_endpoints_status;
	@SerializedName("whatsapp_web_status")
	public String whatsapp_web_status;
	@SerializedName("registration_server_status")
	public String registration_server_status;
	@SerializedName("facebook_tcp_blocking")
	public Boolean facebook_tcp_blocking;
	@SerializedName("facebook_dns_blocking")
	public Boolean facebook_dns_blocking;
	@SerializedName("telegram_http_blocking")
	public Boolean telegram_http_blocking;
	@SerializedName("telegram_tcp_blocking")
	public Boolean telegram_tcp_blocking;
	@SerializedName("telegram_web_status")
	public String telegram_web_status;
	@SerializedName("protocol")
	public Integer protocol;
	@SerializedName("simple")
	public Simple simple;
	@SerializedName("advanced")
	@Deprecated public Advanced advanced;
	@SerializedName("summary")
	public Summary summary;
	@SerializedName("server")
	public Server server;
	@SerializedName("server_address")
	@Deprecated public String server_address;
	//We calculate server_name and server_country at runtime and save the single values here.
	public String server_name;
	public String server_country;
	@SerializedName("tampering")
	public Tampering tampering;

	private static String setFractionalDigits(double value) {
		return String.format(Locale.getDefault(), value < 10 ? "%.2f" : "%.1f", value);
	}

	public int getWebsiteBlocking() {
		if (blocking != null) {
			switch (blocking) {
				case "dns":
					return R.string.TestResults_Details_Websites_LikelyBlocked_BlockingReason_DNS;
				case "tcp_ip":
					return R.string.TestResults_Details_Websites_LikelyBlocked_BlockingReason_TCPIP;
				case "http-diff":
					return R.string.TestResults_Details_Websites_LikelyBlocked_BlockingReason_HTTPDiff;
				case "http-failure":
					return R.string.TestResults_Details_Websites_LikelyBlocked_BlockingReason_HTTPFailure;
				default:
					return R.string.TestResults_NotAvailable;
			}
		} else
			return R.string.TestResults_NotAvailable;
	}

	public int getWhatsappEndpointStatus() {
		if (whatsapp_endpoints_status != null) {
			if (whatsapp_endpoints_status.equals(BLOCKED))
				return R.string.TestResults_Details_InstantMessaging_WhatsApp_Application_Label_Failed;
			return R.string.TestResults_Details_InstantMessaging_WhatsApp_Application_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getWhatsappWebStatus() {
		if (whatsapp_web_status != null) {
			if (whatsapp_web_status.equals(BLOCKED))
				return R.string.TestResults_Details_InstantMessaging_WhatsApp_WebApp_Label_Failed;
			return R.string.TestResults_Details_InstantMessaging_WhatsApp_WebApp_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getWhatsappRegistrationStatus() {
		if (registration_server_status != null) {
			if (registration_server_status.equals(BLOCKED))
				return R.string.TestResults_Details_InstantMessaging_WhatsApp_Registrations_Label_Failed;
			return R.string.TestResults_Details_InstantMessaging_WhatsApp_Registrations_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getTelegramEndpointStatus() {
		if (telegram_http_blocking != null && telegram_tcp_blocking != null) {
			if (telegram_http_blocking || telegram_tcp_blocking)
				return R.string.TestResults_Details_InstantMessaging_Telegram_Application_Label_Failed;
			else
				return R.string.TestResults_Details_InstantMessaging_Telegram_Application_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getTelegramWebStatus() {
		if (telegram_web_status != null) {
			if (telegram_web_status.equals(BLOCKED))
				return R.string.TestResults_Details_InstantMessaging_Telegram_WebApp_Label_Failed;
			return R.string.TestResults_Details_InstantMessaging_Telegram_WebApp_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getFacebookMessengerDns() {
		if (facebook_dns_blocking != null) {
			if (facebook_dns_blocking)
				return R.string.TestResults_Details_InstantMessaging_FacebookMessenger_DNS_Label_Failed;
			else
				return R.string.TestResults_Details_InstantMessaging_FacebookMessenger_DNS_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public int getFacebookMessengerTcp() {
		if (facebook_tcp_blocking != null) {
			if (facebook_tcp_blocking)
				return R.string.TestResults_Details_InstantMessaging_FacebookMessenger_TCP_Label_Failed;
			else
				return R.string.TestResults_Details_InstantMessaging_FacebookMessenger_TCP_Label_Okay;
		}
		return R.string.TestResults_NotAvailable;
	}

	public Boolean isNdt7() {
		return protocol != null && protocol == 7;
	}
	
	public String getUpload(Context ctx) {
		if (isNdt7() && summary != null && summary.upload != null)
			return setFractionalDigits(getScaledValue(summary.upload));
		if (simple != null && simple.upload != null)
			return setFractionalDigits(getScaledValue(simple.upload));
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public int getUploadUnit() {
		if (isNdt7() && summary != null && summary.upload != null)
			return getUnit(summary.upload);
		if (simple != null && simple.upload != null)
			return getUnit(simple.upload);
		return R.string.TestResults_NotAvailable;
	}

	public String getDownload(Context ctx) {
		if (isNdt7() && summary != null && summary.download != null)
			return setFractionalDigits(getScaledValue(summary.download));
		if (simple != null && simple.download != null)
			return setFractionalDigits(getScaledValue(simple.download));
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public int getDownloadUnit() {
		if (isNdt7() && summary != null && summary.download != null)
			return getUnit(summary.download);
		if (simple != null && simple.download != null)
			return getUnit(simple.download);
		return R.string.TestResults_NotAvailable;
	}

	private double getScaledValue(double value) {
		if (value < 1000)
			return value;
		else if (value < 1000 * 1000)
			return value / 1000;
		else
			return value / 1000 * 1000;
	}

	private int getUnit(double value) {
		//We assume there is no Tbit/s (for now!)
		if (value < 1000)
			return R.string.TestResults_Kbps;
		else if (value < 1000 * 1000)
			return R.string.TestResults_Mbps;
		else
			return R.string.TestResults_Gbps;
	}

	public String getPing(Context ctx) {
		if (isNdt7() && summary != null && summary.ping != null)
			return String.format(Locale.getDefault(), "%.1f", summary.ping);
		if (simple != null && simple.ping != null)
			return String.format(Locale.getDefault(), "%.1f", simple.ping);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getServer(Context ctx) {
		if (server_name != null && server_country != null)
			return server_name + " - " + server_country;
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getPacketLoss(Context ctx) {
		if (isNdt7() && summary != null && summary.retransmit_rate != null)
			return String.format(Locale.getDefault(), "%.3f", summary.retransmit_rate * 100);
		if (advanced != null && advanced.packet_loss != null)
			return String.format(Locale.getDefault(), "%.3f", advanced.packet_loss * 100);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getAveragePing(Context ctx) {
		if (isNdt7() && summary != null && summary.avg_rtt != null)
			return String.format(Locale.getDefault(), "%.1f", summary.avg_rtt);
		if (advanced != null && advanced.avg_rtt != null)
			return String.format(Locale.getDefault(), "%.1f", advanced.avg_rtt);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getMaxPing(Context ctx) {
		if (isNdt7() && summary != null && summary.max_rtt != null)
			return String.format(Locale.getDefault(), "%.1f", summary.max_rtt);
		if (advanced != null && advanced.max_rtt != null)
			return String.format(Locale.getDefault(), "%.1f", advanced.max_rtt);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getMSS(Context ctx) {
		if (isNdt7() && summary != null && summary.mss != null)
			return String.format(Locale.getDefault(), "%.0f", summary.mss);
		if (advanced != null && advanced.mss != null)
			return String.format(Locale.getDefault(), "%.0f", advanced.mss);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public String getMedianBitrate(Context ctx) {
		if (simple != null && simple.median_bitrate != null)
			return setFractionalDigits(getScaledValue(simple.median_bitrate));
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	public int getMedianBitrateUnit() {
		if (simple != null && simple.median_bitrate != null)
			return getUnit(simple.median_bitrate);
		return R.string.TestResults_NotAvailable;
	}

	public int getVideoQuality(Boolean extended) {
		if (simple != null && simple.median_bitrate != null)
			return minimumBitrateForVideo(simple.median_bitrate, extended);
		return R.string.TestResults_NotAvailable;
	}

	private int minimumBitrateForVideo(double videoQuality, Boolean extended) {
		if (videoQuality < 600)
			return R.string.r240p;
		else if (videoQuality < 1000)
			return R.string.r360p;
		else if (videoQuality < 2500)
			return R.string.r480p;
		else if (videoQuality < 5000)
			return extended ? R.string.r720p_ext : R.string.r720p;
		else if (videoQuality < 8000)
			return extended ? R.string.r1080p_ext : R.string.r1080p;
		else if (videoQuality < 16000)
			return extended ? R.string.r1440p_ext : R.string.r1440p;
		else
			return extended ? R.string.r2160p_ext : R.string.r2160p;
	}

	public String getPlayoutDelay(Context ctx) {
		if (simple != null && simple.min_playout_delay != null)
			return String.format(Locale.getDefault(), "%.2f", simple.min_playout_delay);
		return ctx.getString(R.string.TestResults_NotAvailable);
	}

	//NDTSummary
	public static class Summary {
		@SerializedName("upload")
		public Double upload;
		@SerializedName("download")
		public Double download;
		@SerializedName("ping")
		public Double ping;
		@SerializedName("max_rtt")
		public Double max_rtt;
		@SerializedName("avg_rtt")
		public Double avg_rtt;
		@SerializedName("min_rtt")
		public Double min_rtt;
		@SerializedName("mss")
		public Double mss;
		@SerializedName("retransmit_rate")
		public Double retransmit_rate;
	}

	public class Server {
		@SerializedName("hostname")
		public String hostname;
		@SerializedName("site")
		public String site;
	}

	//DASHSummary and NDT5Summary
	//Deprecate values
	public static class Simple {
		@SerializedName("upload")
		@Deprecated public Double upload;
		@SerializedName("download")
		@Deprecated public Double download;
		@SerializedName("ping")
		@Deprecated public Double ping;
		@SerializedName("median_bitrate")
		public Double median_bitrate;
		@SerializedName("min_playout_delay")
		public Double min_playout_delay;
	}

	@Deprecated public static class Advanced {
		@SerializedName("packet_loss")
		public Double packet_loss;
		@SerializedName("avg_rtt")
		public Double avg_rtt;
		@SerializedName("max_rtt")
		public Double max_rtt;
		@SerializedName("mss")
		public Double mss;
	}

	public static class Tampering {
		public final boolean value;

		public Tampering(boolean value) {
			this.value = value;
		}

		public class TamperingObj {
			@SerializedName("header_field_name") boolean header_field_name;
			@SerializedName("header_field_number") boolean header_field_number;
			@SerializedName("header_field_value") boolean header_field_value;
			@SerializedName("header_name_capitalization") boolean header_name_capitalization;
			@SerializedName("request_line_capitalization") boolean request_line_capitalization;
			@SerializedName("total") boolean total;

			public boolean isAnomaly() {
				return header_field_name || header_field_number || header_field_value || header_name_capitalization || request_line_capitalization || total;
			}
		}
	}
}
