package com.yairkukielka.rssninja.network;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * A request for retrieving a {@link org.json.JSONObject} response body at a given URL,
 * allowing for an optional {@link org.json.JSONObject} to be passed in as part of the
 * request body. The custom part is to allow to receive an empty body without
 * error
 */
public class JsonCustomRequest extends JsonRequest<JSONObject> {

	/**
	 * Creates a new request.
	 *
	 * @param method
	 *            the HTTP method to use
	 * @param url
	 *            URL to fetch the JSON from
	 * @param jsonRequest
	 *            A {@link org.json.JSONObject} to post with the request. Null is allowed
	 *            and indicates no parameters will be posted along with request.
	 * @param listener
	 *            Listener to receive the JSON response
	 * @param errorListener
	 *            Error listener, or null to ignore errors.
	 */
	public JsonCustomRequest(int method, String url, JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener, errorListener);
	}

	/**
	 * Constructor which defaults to <code>GET</code> if
	 * <code>jsonRequest</code> is <code>null</code>, <code>POST</code>
	 * otherwise.
	 *
	 * @see #JsonObjectRequest(int, String, org.json.JSONObject, Listener, ErrorListener)
	 */
	public JsonCustomRequest(String url, JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, listener, errorListener);
	}

	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			if (TextUtils.isEmpty(jsonString)) {
				return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));
			} else {
				return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
			}
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}
}
