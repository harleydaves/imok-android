package com.hububnet.util;

import com.google.android.c2dm.C2DMessaging;
import com.hububnet.DroidHubub;
import com.hububnet.Hubub;
import com.hububnet.Invoker;
import com.hububnet.InvokerListener;
import com.hububnet.docs.HububCookies;
import com.hububnet.docs.HububService;
import com.hububnet.docs.HububServices;
import com.hububnet.reg.HububRegDialog;
import com.hububnet.reg.HububRegProcess;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.*;

public class HububPhone extends PhoneStateListener implements HububRegDialog.Listener, InvokerListener{//implements PhoneListener{	// Wraps BB Phone class..
	private static HububPhone _instance;
	boolean _isActive = false;
	private HububNVPair _phoneNum;
	private String _prevPhoneNum;

	private HububPhone(){
		//Phone.addPhoneListener(this);
		TelephonyManager tm = (TelephonyManager) DroidHubub.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(this, LISTEN_CALL_STATE);
	}

	public static HububPhone getInstance(){
		if(_instance == null)
			_instance = new HububPhone();
		return _instance;
	}

	public static String getGlobalPhoneNumber(){
		return HububCookies.getCookie("CountryCode") +HububCookies.getCookie("PhoneNum");
	}

	public void initiateCall(String number){
		try{
			_isActive = true;
			String cleanNumber = "+" +Hubub.cleanPhoneNumber(number);
			Hubub.Debug("2", "number: " +number +", cleanNumber: " +cleanNumber);
			Uri uri = Uri.fromParts("tel", cleanNumber, null);
			Intent callIntent = new Intent(Intent.ACTION_CALL, uri);
			//callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//DroidHubub.getInstance().startActivity(callIntent);
			DroidHubub.getInstance().startActivityForResult(callIntent, Hubub.PhoneIntent);
		}catch(Exception e){
			Hubub.Logger(Hubub.getStackTrace(e));
		}
	}

	public void setActive(){
		_isActive = true;
	}

	public void reset(){
		_isActive = false;
	}

	protected void finalize(){
		Hubub.Logger("HububPhone: finalize...");
		//Phone.removePhoneListener(this);		
	}

	public boolean isActive(){
		return _isActive;
	}

	/* PhoneListener Protocol */
	public void callAdded(int arg0) {
	}

	public void callAnswered(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callConferenceCallEstablished(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callConnected(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callDirectConnectConnected(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callDirectConnectDisconnected(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callDisconnected(int arg0) {
		Hubub.Debug("2", "...");
		_isActive = false;
	}

	public void callEndedByUser(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callFailed(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public void callHeld(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callIncoming(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callInitiated(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callRemoved(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callResumed(int arg0) {
		// TODO Auto-generated method stub

	}

	public void callWaiting(int arg0) {
		// TODO Auto-generated method stub

	}

	public void conferenceCallDisconnected(int arg0) {
		// TODO Auto-generated method stub

	}

	public static String getCountryCode(){
		String retval = null;
		int mcc = DroidHubub.getInstance().getResources().getConfiguration().mcc;
		retval = getCountryCode("" +mcc);
		Hubub.Debug("2", "mcc: " +mcc +", cc: " +retval);
		return retval;
	}

	public static String getCountryCode(String mcc){
		String retval = null;
		for(int i=0; i<MMCToDCCMap.length; i+=2){
			if(mcc.equals(MMCToDCCMap[i])){
				retval = MMCToDCCMap[i+1];
				break;
			}
		}
		return retval;
	}

	private void updatePhoneNum(String phoneNum){
		if(!phoneNum.equals(_prevPhoneNum)){
			HububCookies.setCookie("PhoneNum", phoneNum);
			HububServices services = new HububServices();
			HububService service = services.addServiceCall("SetGetEntity");
			service.getInputs();
			service.setParm("MobilePhone", phoneNum);
			Invoker invoker = new Invoker();
			HububWorking.getInstance().working();
			invoker.send(services, this);
		}
		HububCookies.getInstance().sync();

	}

	public boolean isDerPhoneNum(){	// Is phone number derived...
		String derPhoneNum = HububCookies.getCookie("DerPhoneNum");
		return (derPhoneNum.length() > 0);
	}

	public String determinePhoneNumber(){
		Hubub.Debug("2", "...");
		HububRegProcess regprocess = HububRegProcess.getInstance();
		regprocess.justDoPhone();
		return "";
	}
	/*
		String retval = "";
		HububAlert alert = HububAlert.getInstance();
		String countryCode = getCountryCode();
		if(countryCode == null || countryCode.length() == 0){
			alert.removeButton();
			alert.alert("Unable to determine the country code for this phone and cannot proceed...MCC: " 
					+DroidHubub.getInstance().getResources().getConfiguration().mcc);
			return retval;
		}
		String prevCountryCode = HububCookies.getCookie("CountryCode");
		String phoneNum = HububCookies.getCookie("PhoneNum");
		_prevPhoneNum = phoneNum;
		String derPhoneNum = "";
		try{
			derPhoneNum = ((TelephonyManager) DroidHubub.getInstance().
					getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		}catch(Exception e){
			derPhoneNum = "";
			Hubub.Debug("1", "TelephoneManager Error: " +e.getMessage());
			Hubub.Logger(Hubub.getStackTrace(e));

		}

		// Testing area...
		//derPhoneNum = "";
		//countryCode = "44";

		String prevDerPhoneNum = HububCookies.getCookie("DerPhoneNum");

		HububCookies.setCookie("CountryCode", countryCode);
		HububCookies.setCookie("DerPhoneNum", derPhoneNum);
		if(derPhoneNum.length() > 0){
			//if(!derPhoneNum.equals(_prevPhoneNum))
			this.updatePhoneNum(derPhoneNum);
		}
		else{
			if(!countryCode.equals(prevCountryCode)){// ask for number...
				retval = "New country code since last time IMOK was activated...";
			}
			if(retval.length() == 0 && prevDerPhoneNum.length() > 0){ // couldn't derive PN and prev derived PN exists
				retval = "Can't derive phone number...";
			}
			if(retval.length() == 0 && phoneNum.length() == 0){ // couldn't derive PN and prev derived PN exists
				retval = "Can't derive phone number...";
			}			
		}
		if(retval.length() > 0){
			HububRegDialog dialog = new HububRegDialog();
			dialog.setCancelable(false);
			dialog.setTag("PhoneNum");
			dialog.setListener(this);
			dialog.setLButton("", "");
			dialog.setRButton("Enter", "enter");
			dialog.show();
			dialog.setTopText("Please provide your <i>Mobile Phone Number</i>, <b>without the Country Code</b>. " +
					"Leading zeros you enter will be deleted.<br><br>" +
			"This number will <b>ONLY</b> be used during an alert or emergency.<br>");
			HububWidgett widget = dialog.getWidgets();
			_phoneNum = new HububNVPair("Phone Number");
			_phoneNum.setDropKeyboardOnUnFocus(true);
			//_phoneNum.setValue(HububCookies.getCookie("PhoneNum"));
			widget.addVertWidget(_phoneNum);
			widget.align();
			widget.sizeToFit();
			widget.edit();
		}

		return retval;
	}
	*/

	public void determinePushToken(){
		/* Register device for push notifications... */
		Hubub.Debug("2", "...");
		//String prevPushToken = HububCookies.getCookie("PushToken");
		String registrationId = C2DMessaging.getRegistrationId(DroidHubub.getInstance());

		//registrationId = "";	// FOR TESTING ONLY

		if(registrationId == null || registrationId.length() == 0){
			Hubub.Debug("2", "No existing registrationId. Registering..");
			HububWorking.getInstance().working();
			C2DMessaging.register(DroidHubub.getInstance(), "hububnet@gmail.com");
		}
		else{
			this.updatePushToken(registrationId);
		}
	}

	public void updatePushToken(String pushToken){
		// Now update the server
		String prevPushToken = HububCookies.getCookie("PushToken");
		if(prevPushToken.equals(pushToken)){
			Hubub.Debug("2", "No change to pushToken, don't send...");
			return;
		}
		Hubub.Debug("2", "...");
		HububWorking.getInstance().working();
		HububServices services = new HububServices();
		HububService service = services.addServiceCall("UpdateDevice");
		service.getInputs();
		service.setParm("DeviceID", HububCookies.getCookie("DeviceID"));
		service.setParm("PushToken", pushToken);
		Invoker invoker = new Invoker();
		invoker.sendAsEntityID("0");
		invoker.send(services, this);

	}

	/* PhoneStateListener Protocol */
	public void onCallStateChanged (int state, String incomingNumber){
		_isActive = !(state == TelephonyManager.CALL_STATE_IDLE);
		Hubub.Debug("2", " state: " +state +", incomingNumber: " +incomingNumber +", isActive: " +_isActive);
	}

	/** Returns the country code associated with the current mobile subscriber's wireless carrier. */

	private static String[] MMCToDCCMap = {	// Maps MCC to international country code
		"412", "93"	//	AF	Afghanistan
		,"276", "355"	//	AL	Albania
		,"603", "213"	//	DZ	Algeria
		,"544", "1684"	//	AS	American Samoa (US)
		,"213", "376"	//	AD	Andorra
		,"631", "244"	//	AO	Angola
		,"365", "1264"	//	AI	Anguilla
		,"344", "1268"	//	AG	Antigua and Barbuda
		,"722", "54"	//	AR	Argentine Republic
		,"283", "374"	//	AM	Armenia
		,"363", "297"	//	AW	Aruba (Netherlands)
		,"505", "61"	//	AU	Australia
		,"232", "43"	//	AT	Austria
		,"400", "994"	//	AZ	Azerbaijani Republic
		,"364", "1242"	//	BS	Bahamas
		,"426", "973"	//	BH	Bahrain
		,"470", "880"	//	BD	Bangladesh
		,"342", "1243"	//	BB	Barbados
		,"257", "375"	//	BY	Belarus
		,"206", "32"	//	BE	Belgium
		,"702", "501"	//	BZ	Belize
		,"616", "229"	//	BJ	Benin
		,"350", "1441"	//	BM	Bermuda (UK)
		,"402", "975"	//	BT	Bhutan
		,"736", "591"	//	BO	Bolivia
		,"218", "387"	//	BA	Bosnia and Herzegovina
		,"652", "267"	//	BW	Botswana
		,"724", "55"	//	BR	Brazil
		,"348", "1284"	//	VG	British Virgin Islands (UK)
		,"528", "673"	//	BN	Brunei Darussalam
		,"284", "359"	//	BG	Bulgaria
		,"613", "226"	//	BF	Burkina Faso
		,"642", "257"	//	BI	Burundi
		,"456", "855"	//	KH	Cambodia
		,"624", "237"	//	CM	Cameroon
		,"302", "1"	//	CA	Canada
		,"625", "238"	//	CV	Cape Verde
		,"346", "1345"	//	KY	Cayman Islands (UK)
		,"623", "236"	//	CF	Central African Republic
		,"622", "235"	//	TD	Chad
		,"730", "56"	//	CL	Chile
		,"460", "86"	//	CN	China
		,"461", "86"	//	CN	China
		,"732", "57"	//	CO	Colombia
		,"654", "269"	//	KM	Comoros
		,"629", "242"	//	CG	Republic of the Congo
		,"548", "682"	//	CK	Cook Islands (NZ)
		,"712", "506"	//	CR	Costa Rica
		,"612", "225"	//	CI	Côte d'Ivoire
		,"219", "385"	//	HR	Croatia
		,"368", "53"	//	CU	Cuba
		,"280", "357"	//	CY	Cyprus
		,"230", "420"	//	CZ	Czech Republic
		,"630", "243"	//	CD	Democratic Republic of the Congo
		,"238", "43"	//	DK	Denmark
		,"638", "253"	//	DJ	Djibouti
		,"366", "1767"	//	DM	Dominica
		,"370", "1809"	//	DO	Dominican Republic
		,"514", "670"	//	TL	East Timor
		,"740", "593"	//	EC	Ecuador
		,"602", "20"	//	EG	Egypt
		,"706", "503"	//	SV	El Salvador
		,"627", "240"	//	GQ	Equatorial Guinea
		,"657", "291"	//	ER	Eritrea
		,"248", "372"	//	EE	Estonia
		,"636", "251"	//	ET	Ethiopia
		,"750", "500"	//	FK	Falkland Islands (Malvinas)
		,"288", "298"	//	FO	Faroe Islands (Denmark)
		,"542", "679"	//	FJ	Fiji
		,"244", "358"	//	FI	Finland
		,"208", "33"	//	FR	France
		,"742", ""	//	GF	French Guiana (France)
		,"547", "689"	//	PF	French Polynesia (France)
		,"628", "241"	//	GA	Gabonese Republic
		,"607", "220"	//	GM	Gambia
		,"282", "995"	//	GE	Georgia
		,"262", "49"	//	DE	Germany
		,"620", "233"	//	GH	Ghana
		,"266", "350"	//	GI	Gibraltar (UK)
		,"202", "30"	//	GR	Greece
		,"290", "299"	//	GL	Greenland (Denmark)
		,"352", "1473"	//	GD	Grenada
		,"340", ""	//	GP	Guadeloupe (France)
		,"535", "1671"	//	GU	Guam (US)
		,"704", "502"	//	GT	Guatemala
		,"611", "224"	//	GN	Guinea
		,"632", "245"	//	GW	Guinea-Bissau
		,"738", "592"	//	GY	Guyana
		,"372", "509"	//	HT	Haiti
		,"708", "504"	//	HN	Honduras
		,"454", "852"	//	HK	Hong Kong (PRC)
		,"216", "36"	//	HU	Hungary
		,"274", "354"	//	IS	Iceland
		,"404", "91"	//	IN	India
		,"405", "91"	//	IN	India
		,"510", "62"	//	ID	Indonesia
		,"432", "98"	//	IR	Iran
		,"418", "964"	//	IQ	Iraq
		,"272", "353"	//	IE	Ireland
		,"425", "972"	//	IL	Israel
		,"222", "39"	//	IT	Italy
		,"338", "1876"	//	JM	Jamaica
		,"441", "81"	//	JP	Japan
		,"440", "81"	//	JP	Japan
		,"416", "962"	//	JO	Jordan
		,"401", "7"	//	KZ	Kazakhstan
		,"639", "254"	//	KE	Kenya
		,"545", "686"	//	KI	Kiribati
		,"467", "850"	//	KP	Korea, North
		,"450", "82"	//	KR	Korea, South
		,"419", "965"	//	KW	Kuwait
		,"437", "996"	//	KG	Kyrgyz Republic
		,"457", "856"	//	LA	Laos
		,"247", "371"	//	LV	Latvia
		,"415", "961"	//	LB	Lebanon
		,"651", "266"	//	LS	Lesotho
		,"618", "231"	//	LR	Liberia
		,"606", "218"	//	LY	Libya
		,"295", "423"	//	LI	Liechtenstein
		,"246", "370"	//	LT	Lithuania
		,"270", "352"	//	LU	Luxembourg
		,"455", "853"	//	MO	Macau (PRC)
		,"294", "389"	//	MK	Republic of Macedonia
		,"646", "261"	//	MG	Madagascar
		,"650", "265"	//	MW	Malawi
		,"502", "60"	//	MY	Malaysia
		,"472", "960"	//	MV	Maldives
		,"610", "223"	//	ML	Mali
		,"278", "356"	//	MT	Malta
		,"551", "692"	//	MH	Marshall Islands
		,"340", ""	//	MQ	Martinique (France)
		,"609", "222"	//	MR	Mauritania
		,"617", "230"	//	MU	Mauritius
		,"334", "52"	//	MX	Mexico
		,"550", "691"	//	FM	Federated States of Micronesia
		,"259", "373"	//	MD	Moldova
		,"212", "377"	//	MC	Monaco
		,"428", "976"	//	MN	Mongolia
		,"297", "382"	//	ME	Montenegro (Republic of)
		,"354", "1664"	//	MS	Montserrat (UK)
		,"604", "212"	//	MA	Morocco
		,"643", "258"	//	MZ	Mozambique
		,"414", "95"	//	MM	Myanmar
		,"649", "264"	//	NA	Namibia
		,"536", "674"	//	NR	Nauru
		,"429", "977"	//	NP	Nepal
		,"204", "31"	//	NL	Netherlands
		,"362", "599"	//	AN	Netherlands Antilles (Netherlands)
		,"546", "687"	//	NC	New Caledonia (France)
		,"530", "64"	//	NZ	New Zealand
		,"710", "505"	//	NI	Nicaragua
		,"614", "227"	//	NE	Niger
		,"621", "234"	//	NG	Nigeria
		,"534", "1670"	//	MP	Northern Mariana Islands (US)
		,"242", "47"	//	NO	Norway
		,"422", "968"	//	OM	Oman
		,"410", "92"	//	PK	Pakistan
		,"552", "680"	//	PW	Palau
		,"423", ""	//	PS	Palestine
		,"714", "507"	//	PA	Panama
		,"537", "675"	//	PG	Papua New Guinea
		,"744", "595"	//	PY	Paraguay
		,"716", "51"	//	PE	Perú
		,"515", "63"	//	PH	Philippines
		,"260", "48"	//	PL	Poland
		,"268", "351"	//	PT	Portugal
		,"330", "1"	//	PR	Puerto Rico (US)
		,"427", "974"	//	QA	Qatar
		,"647", ""	//	RE	Réunion (France)
		,"226", "40"	//	RO	Romania
		,"250", "7"	//	RU	Russian Federation
		,"635", "250"	//	RW	Rwandese Republic
		,"356", "1869"	//	KN	Saint Kitts and Nevis
		,"358", "1758"	//	LC	Saint Lucia
		,"308", "508"	//	PM	Saint Pierre and Miquelon (France)
		,"360", "1784"	//	VC	Saint Vincent and the Grenadines
		,"549", "685"	//	WS	Samoa
		,"292", "378"	//	SM	San Marino
		,"626", "239"	//	ST	São Tomé and Príncipe
		,"420", "966"	//	SA	Saudi Arabia
		,"608", "221"	//	SN	Senegal
		,"220", "381"	//	RS	Serbia (Republic of)
		,"633", "248"	//	SC	Seychelles
		,"619", "232"	//	SL	Sierra Leone
		,"525", "65"	//	SG	Singapore
		,"231", "421"	//	SK	Slovakia
		,"293", "386"	//	SI	Slovenia
		,"540", "677"	//	SB	Solomon Islands
		,"637", "252"	//	SO	Somalia
		,"655", "27"	//	ZA	South Africa
		,"214", "34"	//	ES	Spain
		,"413", "94"	//	LK	Sri Lanka
		,"634", "249"	//	SD	Sudan
		,"746", "597"	//	SR	Suriname
		,"653", "268"	//	SZ	Swaziland
		,"240", "46"	//	SE	Sweden
		,"228", "41"	//	CH	Switzerland
		,"417", "963"	//	SY	Syria
		,"466", "886"	//	TW	Taiwan
		,"436", "992"	//	TJ	Tajikistan
		,"640", "255"	//	TZ	Tanzania
		,"520", "66"	//	TH	Thailand
		,"615", "228"	//	TG	Togolese Republic
		,"539", "676"	//	TO	Tonga
		,"374", "1868"	//	TT	Trinidad and Tobago
		,"605", "216"	//	TN	Tunisia
		,"286", "90"	//	TR	Turkey
		,"438", "993"	//	TM	Turkmenistan
		,"376", "1649"	//	TC	Turks and Caicos Islands (UK)
		,"641", "256"	//	UG	Uganda
		,"255", "380"	//	UA	Ukraine
		,"424", "971"	//	AE	United Arab Emirates
		,"430", "971"	//	AE	United Arab Emirates (Abu Dhabi)
		,"431", "971"	//	AE	United Arab Emirates (Dubai)
		,"235", "44"	//	GB	United Kingdom
		,"234", "44"	//	GB	United Kingdom
		,"310", "1"	//	US	United States of America
		,"311", "1"	//	US	United States of America
		,"312", "1"	//	US	United States of America
		,"313", "1"	//	US	United States of America
		,"314", "1"	//	US	United States of America
		,"315", "1"	//	US	United States of America
		,"316", "1"	//	US	United States of America
		,"332", "1340"	//	VI	United States Virgin Islands (US)
		,"748", "598"	//	UY	Uruguay
		,"434", "998"	//	UZ	Uzbekistan
		,"541", "678"	//	VU	Vanuatu
		,"225", "39"	//	VA	Vatican City State
		,"734", "58"	//	VE	Venezuela
		,"452", "84"	//	VN	Viet Nam
		,"543", "681"	//	WF	Wallis and Futuna (France)
		,"421", "967"	//	YE	Yemen
		,"645", "260"	//	ZM	Zambia
		,"648", "263"	//	ZW	Zimbabwe
	};

	/* HububRegDialog.Listener Protocol */
	public void dialogAction(HububRegDialog regDialog, Object selectedObject) {
		if(regDialog.getTag().equals("PhoneNum")){
			String phoneNum = Hubub.cleanPhoneNumber(_phoneNum.getValue());
			Hubub.Debug("2", "phoneNum: " +phoneNum);
			if(phoneNum.length() < 7){
				HububRegDialog dialog = new HububRegDialog();
				dialog.setCancelable(false);
				dialog.setTag("Error");
				dialog.setListener(this);
				dialog.setLButton("", "");
				dialog.setRButton("Continue", "continue");
				dialog.showButtons();
				regDialog.dismiss();
				dialog.show();
				dialog.setTopText("<br><b>Sorry, the phone number length must be greater than 6 digits...</b>");
				return;
			}
			else{
				regDialog.dismiss();
				this.updatePhoneNum(phoneNum);
				//HububCookies.setCookie("PhoneNum", phoneNum);
				//HububCookies.getInstance().sync();
			}
		}
		else if(regDialog.getTag().equals("Error")){
			HububRegDialog dialog = new HububRegDialog();
			dialog.setCancelable(false);
			dialog.setTag("PhoneNum");
			dialog.setListener(this);
			dialog.setLButton("", "");
			dialog.setRButton("Enter", "enter");
			regDialog.dismiss();
			dialog.show();
			dialog.setTopText("Please provide your <i>Mobile Phone Number</i>, <b>without the Country Code</b>. " +
					"Leading zeros you enter will be deleted.<br><br>" +
			"This number will <b>ONLY</b> be used during an alert or emergency.<br>");
			HububWidgett widget = dialog.getWidgets();
			_phoneNum = new HububNVPair("Phone Number");
			_phoneNum.setDropKeyboardOnUnFocus(true);
			//_phoneNum.setValue(HububCookies.getCookie("PhoneNum"));
			widget.addVertWidget(_phoneNum);
			widget.align();
			widget.sizeToFit();
			widget.edit();

		}

	}

	/* InvokerListener Protocol */
	public void onResponseReceived(HububServices services) {
		Hubub.Debug("2", "service: " +services);
		HububWorking.getInstance().doneWorking();
		services.getServices();
		HububService hubServ = services.nextService();
		if(hubServ.getName().equals("UpdateDevice")){
			hubServ.getInputs();
			HububCookies.setCookie("PushToken", hubServ.getParm("PushToken"));
		}

	}

	/*
	public static String getCountryCode() {
		String retval = "";
		String mcc = getMCCFromIMSI();
		for(int i=0; i<MMCToDCCMap.length; i+=2){
			if(MMCToDCCMap[i].equals(mcc)){
				retval = MMCToDCCMap[i+1];
				break;
			}
		}
		Hubub.Logger("HububPhone: getCountryCode: mcc: " +mcc +", cc: " +retval);
		return retval;
	}
	 */
	/*

	private static String getMCCFromIMSI() {
		String retval = "";
		byte[] imsi = getIMSI();
		if( imsi == null || imsi.length < 3 ) {
			return retval;
		}

		// the first three bytes of the IMSI string represents the country code
		int mcc = 0;
		for( int i = 0; i < 3; ++i ) {
			mcc *= 10;
			mcc += imsi[i];
		}

		retval = "" + mcc;
		//int mccAsDecimal = Integer.parseInt(mccString, 16);

		return retval;
	}
	private static byte [] getIMSI() {
		try {

			if (RadioInfo.getNetworkType() == RadioInfo.NETWORK_CDMA) {

				return CDMAInfo.getIMSI();

			} else {

				return SIMCardInfo.getIMSI();
			}

		} catch (Exception e) {
		}
		return null;
	}
	 */


}
