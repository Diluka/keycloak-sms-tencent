package com.github.qcloudsms.httpclient;


public class HTTPException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3690478133432741140L;
	
	private final int statusCode;
    private final String reason;

    public HTTPException(final int statusCode, final String reason) {
        super("HTTP statusCode: " + statusCode + ", reasons: " + reason);
        this.reason = reason;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }
}
