package com.dtj.email;

public class TTMail 
{
	String sSubject;
	String sBody;
	String sFrom;
	String sTo;
	String sDateReceived;	
	
	String sMailHost;
	int nPort;
	String sUsername;
	String sPassword;
	
	public TTMail()
	{
		sSubject = "";
		sBody = "";
		sFrom = "";
		sTo = "";
		sDateReceived = "";
		
		sMailHost = "";
		nPort = 537;
		sUsername = "";
		sPassword = "";
	}

	public String getsSubject() {
		return sSubject;
	}

	public void setsSubject(String sSubject) {
		this.sSubject = sSubject;
	}

	public String getsBody() {
		return sBody;
	}

	public void setsBody(String sBody) {
		this.sBody = sBody;
	}

	public String getsFrom() {
		return sFrom;
	}

	public void setsFrom(String sFrom) {
		this.sFrom = sFrom;
	}

	public String getsDateReceived() {
		return sDateReceived;
	}

	public void setsDateReceived(String sDateReceived) {
		this.sDateReceived = sDateReceived;
	}

	public String getsTo() {
		return sTo;
	}

	public void setsTo(String sTo) {
		this.sTo = sTo;
	}

	public String getsMailHost() {
		return sMailHost;
	}

	public void setsMailHost(String sMailHost) {
		this.sMailHost = sMailHost;
	}

	public int getnPort() {
		return nPort;
	}

	public void setnPort(int nPort) {
		this.nPort = nPort;
	}

	public String getsUsername() {
		return sUsername;
	}

	public void setsUsername(String sUsername) {
		this.sUsername = sUsername;
	}

	public String getsPassword() {
		return sPassword;
	}

	public void setsPassword(String sPassword) {
		this.sPassword = sPassword;
	}
	
	public void Copy(TTMail mail)
	{
		this.sSubject = mail.getsSubject();
		this.sBody = mail.getsBody();
		this.sFrom = mail.getsFrom();
		this.sTo = mail.getsTo();
		this.sDateReceived = mail.getsDateReceived();
		
		this.sMailHost = mail.getsMailHost();
		this.nPort = mail.getnPort();
		this.sUsername = mail.getsUsername();
		this.sPassword = mail.getsPassword();
	}	
}
