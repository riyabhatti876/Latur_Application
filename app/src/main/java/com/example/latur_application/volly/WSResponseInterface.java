package com.example.latur_application.volly;

import com.android.volley.VolleyError;
import com.example.latur_application.volly.URL_Utility.ResponseCode;


public interface WSResponseInterface {

	void onSuccessResponse(ResponseCode responseCode, String response);
	void onErrorResponse(ResponseCode responseCode, VolleyError error);

}
