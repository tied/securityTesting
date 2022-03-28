package com.miniorange.sso.saml.bamboo;

import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.httpclient.RedirectException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.opensaml.saml2.core.*;
import org.xml.sax.InputSource;

import com.miniorange.sso.saml.MoSAMLException;
import com.miniorange.sso.saml.MoSAMLResponse;
import com.miniorange.sso.saml.bamboo.MoSAMLManager;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MoSAMLManager {

	private static Log LOGGER = LogFactory.getLog(MoSAMLManager.class);
	private String certificateexpected = "";
	private long timediff;
	private String replacement = "";
	private MoSAMLUserManager userManager;
	private CrowdService crowdService;
	private CrowdDirectoryService crowdDirectoryService;

	private MoSAMLSettings settings;

	public MoSAMLManager(MoSAMLSettings settings, MoSAMLUserManager userManager, CrowdService crowdService, CrowdDirectoryService crowdDirectoryService) {
		this.settings = settings;
		this.userManager = userManager;
		this.crowdService = crowdService;
		this.crowdDirectoryService = crowdDirectoryService;
	}

	public void createAuthnRequestAndRedirect(HttpServletRequest request, HttpServletResponse response,
											  String relayState, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating Signed AuthnRequest...");
			MoSAMLUtils.doBootstrap();
			AuthnRequest authnRequest = MoSAMLUtils.buildAuthnRequest(settings.getSpEntityId(),
					settings.getLoginServletUrl(), idpConfig.getSsoUrl(), idpConfig.getNameIdFormat(), idpConfig.getForceAuthentication(), idpConfig.getEnablePassiveSso());
			if (StringUtils.equals(idpConfig.getSsoBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SSO");
				authnRequest = (AuthnRequest) MoSAMLUtils.signHttpPostRequest(authnRequest,
						settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());
				String encodedAuthnRequest = MoSAMLUtils.base64EncodeRequest(authnRequest, true);
				String form = createHttpPostRequestForm(idpConfig.getSsoUrl(), encodedAuthnRequest, relayState);
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {

				LOGGER.debug("HTTP-Redirect Binding selected for SSO");
				String encodedAuthnRequest = MoSAMLUtils.base64EncodeRequest(authnRequest, false);
				String urlForSignature = createRequestQueryParamsForSignature(encodedAuthnRequest, relayState);
				String signature = MoSAMLUtils.signHttpRedirectRequest(urlForSignature,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, settings.getPublicSPCertificate(),
						settings.getPrivateSPCertificate());
				String redirectUrl = createRedirectURL(idpConfig.getSsoUrl(), encodedAuthnRequest, relayState,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, signature, false);
				LOGGER.info("redirect url" + redirectUrl);

				httpRedirect(response, redirectUrl);


			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the AuthnRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public String getSignedTestAuthnRequest(MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating AuthnRequest...");
			MoSAMLUtils.doBootstrap();
			AuthnRequest authnRequest = MoSAMLUtils.buildAuthnRequest(settings.getBaseUrl(),
					settings.getLoginServletUrl(), idpConfig.getSsoUrl(), idpConfig.getNameIdFormat(), idpConfig.getForceAuthentication(), idpConfig.getEnablePassiveSso());

			if (idpConfig.getSignedRequest() && StringUtils.equals(idpConfig.getSsoBindingType(), "HttpPost"))
				authnRequest = (AuthnRequest) MoSAMLUtils.signHttpPostRequest(authnRequest,
						settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());

			Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authnRequest);
			Element authDOM = marshaller.marshall(authnRequest);

			Document doc = authDOM.getOwnerDocument();

			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			String requestMessage = out.toString();
			return requestMessage;

		} catch (Throwable t) {
			LOGGER.error("An error occurred while creating the AuthnRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public String getUnSignedTestAuthnRequest(MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating AuthnRequest...");
			MoSAMLUtils.doBootstrap();
			AuthnRequest authnRequest = MoSAMLUtils.buildAuthnRequest(settings.getBaseUrl(),
					settings.getLoginServletUrl(), idpConfig.getSsoUrl(), idpConfig.getNameIdFormat(), idpConfig.getForceAuthentication(), idpConfig.getEnablePassiveSso());
			Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authnRequest);
			Element authDOM = marshaller.marshall(authnRequest);

			Document doc = authDOM.getOwnerDocument();

			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			String requestMessage = out.toString();
			return requestMessage;

		} catch (Throwable t) {
			LOGGER.error("An error occurred while creating the AuthnRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public void createUnSignedAuthnRequestAndRedirect(HttpServletRequest request, HttpServletResponse response,
													  String relayState, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating AuthnRequest...");
			MoSAMLUtils.doBootstrap();
			AuthnRequest authnRequest = MoSAMLUtils.buildAuthnRequest(settings.getSpEntityId(),
					settings.getLoginServletUrl(), idpConfig.getSsoUrl(), idpConfig.getNameIdFormat(), idpConfig.getForceAuthentication(), idpConfig.getEnablePassiveSso());
			if (StringUtils.equals(idpConfig.getSsoBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SSO");
				String encodedAuthnRequest = MoSAMLUtils.base64EncodeRequest(authnRequest, true);
				String form = createHttpPostRequestForm(idpConfig.getSsoUrl(), encodedAuthnRequest, relayState);
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {
				LOGGER.debug("HTTP-Redirect Binding selected for SSO");
				String encodedAuthnRequest = MoSAMLUtils.base64EncodeRequest(authnRequest, false);
				String redirectUrl = createUnSignedRedirectURL(idpConfig.getSsoUrl(), encodedAuthnRequest,
						relayState, false);
				httpRedirect(response, redirectUrl);
			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the AuthnRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public MoSAMLResponse readSAMLResponse(HttpServletRequest request, HttpServletResponse response, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Reading SAML Response");
			MoSAMLUtils.doBootstrap();
			String encodedSAMLResponse = request.getParameter(MoSAMLUtils.SAML_RESPONSE_PARAM);
			String relayState = request.getParameter(MoSAMLUtils.RELAY_STATE_PARAM);
			relayState = MoSAMLUtils.sanitizeText(relayState);

			Response samlResponse = MoSAMLUtils.decodeResponse(encodedSAMLResponse);
			if (!StringUtils.equals(samlResponse.getStatus().getStatusCode().getValue(), StatusCode.SUCCESS_URI)) {
				LOGGER.error("Invalid SAML response. SAML Status Code received: "
						+ samlResponse.getStatus().getStatusCode().getValue());
				String message = StringUtils.EMPTY;
				if (samlResponse.getStatus().getStatusMessage() != null) {
					LOGGER.error("Saml Status Message received: "
							+ samlResponse.getStatus().getStatusMessage().getMessage());
					message = samlResponse.getStatus().getStatusMessage().getMessage()
							+ ". Status Code received in SAML response: "
							+ samlResponse.getStatus().getStatusCode().getValue().split(":")[7];
				} else {
					message = "Invalid status code \""
							+ samlResponse.getStatus().getStatusCode().getValue().split(":")[7]
							+ "\" received in SAML response";
				}
				throw new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_SAML_STATUS);
			}
			Assertion assertion;
			if (samlResponse.getAssertions() != null && samlResponse.getAssertions().size() > 0) {
				assertion = samlResponse.getAssertions().get(0);
			} else {
				assertion = MoSAMLUtils.decryptAssertion(samlResponse.getEncryptedAssertions().get(0),
						settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());
			}

			String acsUrl = settings.getLoginServletUrl();
			String idpACSUrl = acsUrl + "?idp=" + idpConfig.getId();

			verifyConditions(assertion, settings.getSpEntityId());
			verifyIssuer(samlResponse, assertion, idpConfig.getIdpEntityId());
			verifyDestination(samlResponse, acsUrl, idpACSUrl);
			verifyRecipient(assertion, acsUrl, idpACSUrl);

			MoSAMLException t = null;
			Boolean verified = Boolean.FALSE;
			try {
				verified = verifyCertificate(samlResponse, assertion, idpConfig.getX509Certificate());
			} catch (MoSAMLException e) {
				t = e;
			}
			List<String> certificates = (List<String>) idpConfig.getCertificates();
			if (certificates != null && !verified) {
				for (int index = 1; index < certificates.size(); index++) {
					try {
						verified = verifyCertificate(samlResponse, assertion, certificates.get(index));
					} catch (MoSAMLException e) {
						t = e;
					}
					if (verified)
						break;
				}
			}
			if (!verified) {
				LOGGER.error(t.getMessage(), t);
				throw t;
			}

			Map<String, String[]> attributes = getAttributes(assertion);
			NameID nameId = assertion.getSubject().getNameID();
			String nameIdValue = StringUtils.EMPTY;
			String sessionIndex = assertion.getAuthnStatements().get(0).getSessionIndex();
			if (nameId != null) {
				nameIdValue = nameId.getValue();
			}
			attributes.put("NameID", new String[]{nameIdValue});
			MoSAMLResponse samlResponseObj = new MoSAMLResponse(attributes, nameIdValue, sessionIndex, relayState);
			return samlResponseObj;
		} catch (MoSAMLException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} catch (Throwable e) {
			LOGGER.error("An error occurred while verifying the SAML Response.", e);
			throw new MoSAMLException(e, MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	private void verifyIssuer(Response response, Assertion assertion, String idpEntityId) {
		LOGGER.debug("Verifying Issuer in Response and Assertion...");
		String issuerInResponse = response.getIssuer().getValue();
		String issuerInAssertion = assertion.getIssuer().getValue();
		if (!StringUtils.equals(issuerInResponse, idpEntityId)) {
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_ISSUER;
			MoSAMLException e = new MoSAMLException(errorCode.getMessage(),
					buildResolutionMessage(errorCode, idpEntityId, issuerInResponse), errorCode);
			LOGGER.debug(e.getMessage(), e);
			throw e;
		}
		if (!StringUtils.equals(issuerInAssertion, idpEntityId)) {
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_ISSUER;
			MoSAMLException e = new MoSAMLException(errorCode.getMessage(),
					buildResolutionMessage(errorCode, idpEntityId, issuerInAssertion), errorCode);
			LOGGER.debug(e.getMessage(), e);
			throw e;
		}
	}

	private void verifyDestination(Response response, String acsUrl, String idpAcsUrl) {
		// Destination is Optional field so verify only if exist.
		LOGGER.debug("Verifying Destination if present...");
		String destInResponse = response.getDestination();
		if (StringUtils.isBlank(destInResponse) || StringUtils.equals(destInResponse, acsUrl) || StringUtils.equals(destInResponse, idpAcsUrl)) {
			return;
		}
		MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_DESTINATION;
		MoSAMLException e = new MoSAMLException(errorCode.getMessage(),
				buildResolutionMessage(errorCode, acsUrl, destInResponse), errorCode);
		LOGGER.debug(e.getMessage(), e);
		throw e;
	}

	private void verifyRecipient(Assertion assertion, String acsUrl, String idpAcsUrl) {
		LOGGER.debug("Verifying Recipient if present...");
		String recipientInResponse = assertion.getSubject().getSubjectConfirmations().get(0)
				.getSubjectConfirmationData().getRecipient();
		if (StringUtils.isBlank(recipientInResponse) || StringUtils.equals(recipientInResponse, acsUrl) || StringUtils.equals(recipientInResponse, idpAcsUrl)) {
			return;
		}
		MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_RECIPIENT;
		MoSAMLException e = new MoSAMLException(errorCode.getMessage(),
				buildResolutionMessage(errorCode, acsUrl, recipientInResponse), errorCode);
		LOGGER.debug(e.getMessage(), e);
		throw e;
	}

	private void verifyConditions(Assertion assertion, String audienceExpected) {
		LOGGER.debug("Verifying Conditions...");
		Date now = new DateTime().toDate();
		Date notBefore = null;
		Date notOnOrAfter = null;
		long timeDifferenceInBefore = 0;
		long timeDifferenceInAfter = 0;
		if (assertion.getConditions().getNotBefore() != null) {
			notBefore = assertion.getConditions().getNotBefore().toDate();
			if (now.before(notBefore))
				timeDifferenceInBefore = Math.abs(notBefore.getTime() - now.getTime());
		}
		if (assertion.getConditions().getNotOnOrAfter() != null) {
			notOnOrAfter = assertion.getConditions().getNotOnOrAfter().toDate();
			if (now.after(notOnOrAfter))
				timeDifferenceInAfter = Math.abs(now.getTime() - notOnOrAfter.getTime());
		}
		long userAddeddelay = timeInMilliseconds();

		if (notBefore != null && now.before(notBefore) && userAddeddelay - timeDifferenceInBefore < 0) {
			timediff = -(userAddeddelay - timeDifferenceInBefore);
			replacement = "Forward";
		} else if (notOnOrAfter != null && (now.after(notOnOrAfter) || now.equals(notOnOrAfter)) && userAddeddelay - timeDifferenceInAfter < 0) {
			timediff = (timeDifferenceInAfter - userAddeddelay);
			replacement = "Back";
		}
		long valueinminutes = ((timediff / (60 * 1000)) % 60);
		long Exactvalueinminutes = Math.incrementExact(valueinminutes);

		LOGGER.debug("Time in milliseconds = " + timeInMilliseconds() + " Time diff before = "
				+ (userAddeddelay - timeDifferenceInBefore) + " Time diff after = "
				+ (userAddeddelay - timeDifferenceInAfter));

		if (notBefore != null && now.before(notBefore) && userAddeddelay - timeDifferenceInBefore < 0) {

			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_CONDITIONS;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					timeDiff(errorCode, Exactvalueinminutes), errorCode);

			LOGGER.error(samlexception.getMessage(), samlexception);
			throw samlexception;

		} else if (notOnOrAfter != null && (now.after(notOnOrAfter) || now.equals(notOnOrAfter)) && userAddeddelay - timeDifferenceInAfter < 0) {

			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_CONDITIONS;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					timeDiff(errorCode, Exactvalueinminutes), errorCode);

			LOGGER.error(samlexception.getMessage(), samlexception);
			throw samlexception;
		}
		List<Audience> audiencesInAssertion = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences();
		Iterator<Audience> it = audiencesInAssertion.iterator();
		while (it.hasNext()) {
			Audience audience = it.next();
			if (StringUtils.equalsIgnoreCase(audience.getAudienceURI(), audienceExpected)) {
				return;
			}
		}
		MoSAMLException e = new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_AUDIENCE);
		LOGGER.error(MoSAMLException.SAMLErrorCode.INVALID_AUDIENCE.getMessage(), e);
		throw e;
	}

	private String timeDiff(MoSAMLException.SAMLErrorCode error, long temp) {
		StringBuffer errorMsg = new StringBuffer(error.getResolution());
		//errorMsg.append(replacement);
		errorMsg.append(" Set your Server clock " + replacement + " by ");
		errorMsg.append(temp);
		errorMsg.append(" minutes  Or you can Increase ");
		errorMsg.append(temp);
		errorMsg.append(" minutes in  validate Saml Response in SSO setting tab.");
		return errorMsg.toString();
	}

	public long timeInMilliseconds() {
		String time = settings.getTimeDelay();
		LOGGER.debug("Time is: " + time);
		long timeDelay = Math.abs(Long.valueOf(time));
		timeDelay = timeDelay * 60 * 1000;
		return timeDelay;
	}


	private Boolean verifyCertificate(Response response, Assertion assertion, String x509Certificate) {
		LOGGER.debug("Verifying Certificate...");


		Boolean noError = true;
		try {
			if (!response.isSigned() && !assertion.isSigned()) {
				MoSAMLException e = new MoSAMLException(MoSAMLException.SAMLErrorCode.ASSERTION_NOT_SIGNED);
				LOGGER.error(MoSAMLException.SAMLErrorCode.ASSERTION_NOT_SIGNED.getMessage(), e);
				throw e;
			}
			if (response.isSigned()) {
				MoSAMLUtils.verifyCertificate(response, x509Certificate);
			}
			if (assertion.isSigned()) {
				MoSAMLUtils.verifyCertificate(assertion, x509Certificate);
			}
		} catch (CertificateException e) {
			//LOGGER.error(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE.getMessage(), e);
			noError = false;
			//throw new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE);
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					buildResolutionforcertificate(errorCode, assertion, response), errorCode);

			LOGGER.error(samlexception.getMessage(), e);
			throw samlexception;
		} catch (ValidationException e) {
			//LOGGER.error(MoSAMLException.SAMLErrorCode.INVALID_SIGNATURE.getMessage(), e);
			noError = false;
			//throw new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_SIGNATURE);
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_SIGNATURE;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					buildResolutionforcertificate(errorCode, assertion, response), errorCode);

			LOGGER.error(samlexception.getMessage(), e);
			throw samlexception;
		} catch (NoSuchAlgorithmException e) {
			//LOGGER.error(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE.getMessage(), e);
			noError = false;
			//throw new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE);
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					buildResolutionforcertificate(errorCode, assertion, response), errorCode);

			LOGGER.error(samlexception.getMessage(), e);
			throw samlexception;
		} catch (InvalidKeySpecException e) {
			//LOGGER.error(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE.getMessage(), e);
			noError = false;
			//throw new MoSAMLException(MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE);
			MoSAMLException.SAMLErrorCode errorCode = MoSAMLException.SAMLErrorCode.INVALID_CERTIFICATE;
			MoSAMLException samlexception = new MoSAMLException(errorCode.getMessage(),
					buildResolutionforcertificate(errorCode, assertion, response), errorCode);

			LOGGER.error(samlexception.getMessage(), e);
			throw samlexception;
		}
		return noError;
	}

	private String buildResolutionforcertificate(MoSAMLException.SAMLErrorCode error, Assertion assertion, Response response) {
		try {
			if (assertion.isSigned()) {
				List<X509Data> x509Datas = assertion.getSignature().getKeyInfo().getX509Datas();
				for (X509Data x509Data : x509Datas) {
					List<X509Certificate> certificates = x509Data.getX509Certificates();

					for (X509Certificate certificate : certificates) {
						certificateexpected = certificate.getValue();

					}
				}
			} else if (response.isSigned()) {
				List<X509Data> x509Datas = response.getSignature().getKeyInfo().getX509Datas();
				for (X509Data x509Data : x509Datas) {
					List<X509Certificate> certificates = x509Data.getX509Certificates();

					for (X509Certificate certificate : certificates) {
						certificateexpected = certificate.getValue();
					}
				}
			}
		} catch (Exception e) {
			error.getResolution();
		}

		StringBuffer errorMsg = new StringBuffer(error.getResolution());
		errorMsg.append(" Expected certificate : ");
		errorMsg.append(
				"<textarea rows='6' cols='100' word-wrap='break-word;' style='width:580px; margin:0px; " +
						"height:290px;' id ='errormsg' readonly>-----BEGIN CERTIFICATE-----" + certificateexpected + "-----END CERTIFICATE-----</textarea> ");

		errorMsg.append(
				"<div style=\"margin:3%;display:block;text-align:center;\"><input id =\"copy-button\" style=\"padding:1%;"
						+ "width:150px;background: #0091CD none repeat scroll 0% 0%;cursor: pointer;font-size:15px;"
						+ "border-width: 1px;border-style: solid;border-radius: 3px;white-space: nowrap;"
						+ "box-sizing:border-box;border-color: #0073AA;box-shadow:0px 1px 0px rgba(120,200,230,0.6) inset;"
						+ "color: #FFF;\" type=\"button\" value=\"Copy to Clipboard\"></div>");
		errorMsg.append("<script>" + "document.querySelector(\"#copy-button\").onclick = function() {"
				+ "document.querySelector(\"#errormsg\").select();" + "document.execCommand('copy');" + "};"
				+ "</script>");

		return errorMsg.toString();
	}

	private Map<String, String[]> getAttributes(Assertion assertion) {
		Map<String, String[]> attributes = new HashMap<String, String[]>();
		if (assertion.getAttributeStatements().size() > 0) {
			for (Attribute attr : assertion.getAttributeStatements().get(0).getAttributes()) {
				if (attr.getAttributeValues().size() > 0) {
					String[] values = new String[attr.getAttributeValues().size()];
					for (int i = 0; i < attr.getAttributeValues().size(); i++) {
						values[i] = attr.getAttributeValues().get(i).getDOM().getTextContent();
					}
					if (attributes.containsKey(attr.getName())) {
						String[] updatedValues = new String[values.length + attributes.get(attr.getName()).length];
						System.arraycopy(attributes.get(attr.getName()), 0, updatedValues, 0, attributes.get(attr.getName()).length);
						System.arraycopy(values, 0, updatedValues, attributes.get(attr.getName()).length, values.length);
						attributes.put(attr.getName(), updatedValues);
					} else {
						attributes.put(attr.getName(), values);
					}
				}
			}
		}
		return attributes;
	}

	public void createLogoutRequestAndRedirect(HttpServletRequest request, HttpServletResponse response, String nameId,
											   String sessionIndex, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating LogoutRequest...");
			MoSAMLUtils.doBootstrap();
			LogoutRequest logoutRequest = MoSAMLUtils.buildLogoutRequest(settings.getSpEntityId(),
					idpConfig.getSloUrl(), nameId, sessionIndex);

			if (StringUtils.equals(idpConfig.getSloBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SLO");
				logoutRequest = (LogoutRequest) MoSAMLUtils.signHttpPostRequest(logoutRequest,
						settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());
				String encodedLogoutRequest = MoSAMLUtils.base64EncodeRequest(logoutRequest, true);
				String form = createHttpPostRequestForm(idpConfig.getSloUrl(), encodedLogoutRequest,
						settings.getLoginPageUrl());
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {
				LOGGER.debug("HTTP-Redirect Binding selected for SLO");

				String encodedLogoutRequest = MoSAMLUtils.base64EncodeRequest(logoutRequest, false);
				String urlForSignature = createRequestQueryParamsForSignature(encodedLogoutRequest,
						settings.getLoginPageUrl());
				String signature = MoSAMLUtils.signHttpRedirectRequest(urlForSignature,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, settings.getPublicSPCertificate(),
						settings.getPrivateSPCertificate());
				String redirectUrl = createRedirectURL(idpConfig.getSloUrl(), encodedLogoutRequest,
						settings.getLoginPageUrl(), XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, signature, false);
				httpRedirect(response, redirectUrl);
			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the LogoutRequest.", t);
			t.printStackTrace();
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public void createUnSignedLogoutRequestAndRedirect(HttpServletRequest request, HttpServletResponse response,
													   String nameId, String sessionIndex, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating LogoutRequest...");
			MoSAMLUtils.doBootstrap();
			LogoutRequest logoutRequest = MoSAMLUtils.buildLogoutRequest(settings.getSpEntityId(),
					idpConfig.getSloUrl(), nameId, sessionIndex);

			if (StringUtils.equals(idpConfig.getSloBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SLO");
				String encodedLogoutRequest = MoSAMLUtils.base64EncodeRequest(logoutRequest, true);
				String form = createHttpPostRequestForm(idpConfig.getSloUrl(), encodedLogoutRequest,
						settings.getLoginPageUrl());
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {
				LOGGER.debug("HTTP-Redirect Binding selected for SLO");

				String encodedLogoutRequest = MoSAMLUtils.base64EncodeRequest(logoutRequest, false);
				String redirectUrl = createUnSignedRedirectURL(idpConfig.getSloUrl(), encodedLogoutRequest,
						settings.getLoginPageUrl(), false);
				httpRedirect(response, redirectUrl);
			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the LogoutRequest.", t);
			t.printStackTrace();
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public void createLogoutResponseAndRedirect(HttpServletRequest request, HttpServletResponse response,
												Boolean isPostRequest, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating LogoutResponse...");
			MoSAMLUtils.doBootstrap();
			String logoutRequestStr = request.getParameter("SAMLRequest");
			LogoutRequest logoutRequest = MoSAMLUtils.readLogoutRequest(logoutRequestStr, isPostRequest);
			LogoutResponse logoutResponse = MoSAMLUtils.buildLogoutResponse(settings.getSpEntityId(),
					idpConfig.getSsoUrl(), logoutRequest.getID(), StatusCode.SUCCESS_URI);
			if (StringUtils.equals(idpConfig.getSloBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SLO");
				logoutResponse = (LogoutResponse) MoSAMLUtils.signHttpPostRequest(logoutResponse,
						settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());
				String encodedLogoutResponse = MoSAMLUtils.base64EncodeRequest(logoutResponse, true);
				String form = createHttpPostResponseForm(idpConfig.getSloUrl(), encodedLogoutResponse,
						request.getParameter("RelayState"));
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {
				LOGGER.debug("HTTP-Redirect Binding selected for SLO");
				String encodedLogoutResponse = MoSAMLUtils.base64EncodeRequest(logoutResponse, false);
				String urlForSignature = createResponseQueryParamsForSignature(encodedLogoutResponse,
						request.getParameter("RelayState"));
				String signature = MoSAMLUtils.signHttpRedirectRequest(urlForSignature,
						XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, settings.getPublicSPCertificate(),
						settings.getPrivateSPCertificate());
				String redirectUrl = createRedirectURL(idpConfig.getSloUrl(), encodedLogoutResponse,
						request.getParameter("RelayState"), XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256, signature, true);
				httpRedirect(response, redirectUrl);
			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the LogoutRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	public void createUnSignedLogoutResponseAndRedirect(HttpServletRequest request, HttpServletResponse response,
														Boolean isPostRequest, MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating LogoutResponse...");
			MoSAMLUtils.doBootstrap();
			String logoutRequestStr = request.getParameter("SAMLRequest");
			LogoutRequest logoutRequest = MoSAMLUtils.readLogoutRequest(logoutRequestStr, isPostRequest);
			LogoutResponse logoutResponse = MoSAMLUtils.buildLogoutResponse(settings.getSpEntityId(),
					idpConfig.getSloUrl(), logoutRequest.getID(), StatusCode.SUCCESS_URI);
			if (StringUtils.equals(idpConfig.getSloBindingType(), "HttpPost")) {
				LOGGER.debug("HTTP-POST Binding selected for SLO");
				String encodedLogoutResponse = MoSAMLUtils.base64EncodeRequest(logoutResponse, true);
				String form = createHttpPostResponseForm(idpConfig.getSloUrl(), encodedLogoutResponse,
						request.getParameter("RelayState"));
//				response.setContentType("text/html");
				response.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
				response.getOutputStream().close();
				return;
			} else {
				LOGGER.debug("HTTP-Redirect Binding selected for SLO");
				String encodedLogoutResponse = MoSAMLUtils.base64EncodeRequest(logoutResponse, false);

				String redirectUrl = createUnSignedRedirectURL(idpConfig.getSloUrl(), encodedLogoutResponse,
						request.getParameter("RelayState"), true);
				httpRedirect(response, redirectUrl);
			}
		} catch (Throwable t) {
			LOGGER.error("An unknown error occurred while creating the LogoutRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}

	private String buildResolutionMessage(MoSAMLException.SAMLErrorCode error, String expected, String found) {
		StringBuffer errorMsg = new StringBuffer(error.getResolution());
		errorMsg.append(" Add-on was expecting ");
		errorMsg.append(found);
		errorMsg.append(" but found: ");
		errorMsg.append(expected);
		return errorMsg.toString();
	}

	private String createRedirectURL(String url, String samlRequestOrResponse, String relayState, String sigAlgo,
									 String signature, Boolean isResponse) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder(url);
		if (StringUtils.contains(url, "?") && !(StringUtils.endsWith(url, "?") || StringUtils.endsWith(url, "&"))) {
			builder.append("&");
		} else if (!StringUtils.contains(url, "?")) {
			builder.append("?");
		}
		if (isResponse) {
			builder.append(createResponseQueryParamsForSignature(samlRequestOrResponse, relayState));
		} else {
			builder.append(createRequestQueryParamsForSignature(samlRequestOrResponse, relayState));
		}
		builder.append("&").append(MoSAMLUtils.SIGNATURE_ALGO_PARAM).append("=")
				.append(URLEncoder.encode(sigAlgo, "UTF-8")).append("&").append(MoSAMLUtils.SIGNATURE_PARAM).append("=")
				.append(URLEncoder.encode(signature, "UTF-8"));
		return builder.toString();
	}

	private String createUnSignedRedirectURL(String url, String samlRequestOrResponse, String relayState,
											 Boolean isResponse) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder(url);
		if (StringUtils.contains(url, "?") && !(StringUtils.endsWith(url, "?") || StringUtils.endsWith(url, "&"))) {
			builder.append("&");
		} else if (!StringUtils.contains(url, "?")) {
			builder.append("?");
		}
		if (isResponse) {
			builder.append(createResponseQueryParamsForSignature(samlRequestOrResponse, relayState));
		} else {
			builder.append(createRequestQueryParamsForSignature(samlRequestOrResponse, relayState));
		}
		return builder.toString();
	}

	private String createRequestQueryParamsForSignature(String httpRedirectRequest, String relayState)
			throws UnsupportedEncodingException {
		StringBuffer urlForSignature = new StringBuffer();
		urlForSignature.append(MoSAMLUtils.SAML_REQUEST_PARAM).append("=")
				.append(URLEncoder.encode(httpRedirectRequest, StandardCharsets.UTF_8.toString()));
		urlForSignature.append("&").append(MoSAMLUtils.RELAY_STATE_PARAM).append("=");
		if (StringUtils.isNotBlank(relayState)) {
			urlForSignature.append(URLEncoder.encode(relayState, StandardCharsets.UTF_8.toString()));
		} else {
			urlForSignature.append(URLEncoder.encode("/", StandardCharsets.UTF_8.toString()));
		}
		return urlForSignature.toString();
	}

	private String createResponseQueryParamsForSignature(String httpRedirectResponse, String relayState)
			throws UnsupportedEncodingException {
		StringBuffer urlForSignature = new StringBuffer();
		urlForSignature.append(MoSAMLUtils.SAML_RESPONSE_PARAM).append("=")
				.append(URLEncoder.encode(httpRedirectResponse, StandardCharsets.UTF_8.toString()));
		urlForSignature.append("&").append(MoSAMLUtils.RELAY_STATE_PARAM).append("=");
		if (StringUtils.isNotBlank(relayState)) {
			urlForSignature.append(URLEncoder.encode(relayState, StandardCharsets.UTF_8.toString()));
		} else {
			urlForSignature.append(URLEncoder.encode("/", StandardCharsets.UTF_8.toString()));
		}
		return urlForSignature.toString();
	}

	private String createHttpPostRequestForm(String ssoUrl, String encodedRequest, String relayState) {
		String form = ("<html><head><script type=\"text/javascript\">window.onload = function() { document"
				+ ".forms['saml-request-form'].submit(); }</script></head><body><form action=\"") + ssoUrl
				+ "\" method=\"post\" id=\"saml-request-form\"><input type=\"hidden\" name=\"SAMLRequest\" "
				+ "value=\"" + MoSAMLUtils.htmlEncode(encodedRequest) + "\" /><input type=\"hidden\" "
				+ "name=\"RelayState\" value=\"" + MoSAMLUtils.htmlEncode(relayState) + "\" /></form></body></html>";
		return form;
	}

	public String processRedirectionRulesAndReturnIDP(String username) {
		JSONArray rules = settings.getRedirectionRules();
		LOGGER.debug("Redirection Rules " + rules.toString());
		JSONObject rule;
		String idp = null;
		ArrayList<String> ssoEnabledIdpList = settings.getSsoEnabledForIdPList();

		try {
			if (rules == null)
				return null;
			//Get user object, null if user doesn't exist
			BambooUser user = userManager.getUserByUsernameOrEmail(username);

			//For the length of JsonArray
			for (int i = 0; i < rules.length(); i++) {
				//Get a rule JsonObject
				rule = rules.optJSONObject(i);
				JSONObject condition = rule.getJSONObject("condition");
				String decisionFactor = condition.getString("decisionFactor");

				// Check decisionfactor
				switch (decisionFactor) {
					case "domain":
						idp = matchUserDomain(user, username, rule);
						break;
					case "group":
						if (user != null) {
							idp = matchUserGroup(user, rule);
						}
						break;
					case "directory":
						if (user != null) {
							idp = matchUserDirectory(user, rule);
						}
						break;
					default:
						LOGGER.error("Malformed Rule. Skipping this one");
				}
				LOGGER.debug("IDP is "+ idp);
				if (StringUtils.isNotBlank(idp)) {
					if (StringUtils.equalsIgnoreCase(idp, "loginPage") || StringUtils.equalsIgnoreCase(idp, "redirectUrl")){
						return idp;}
					else if (ssoEnabledIdpList.contains(idp)){
						return idp;}
				}
			}
			LOGGER.debug("This is the defaule one " + settings.getDefaultBambooIDP());
			return settings.getDefaultBambooIDP();
		} catch (JSONException e) {
			LOGGER.error("An error occurred while trying to process redirection rules ", e);
			return null;
		}
	}

	public String getTestAuthnRequest(MoIDPConfig idpConfig) {
		try {
			LOGGER.debug("Creating Authentication Request.");
			MoSAMLUtils.doBootstrap();
			AuthnRequest authnRequest = MoSAMLUtils.buildAuthnRequest(settings.getBaseUrl(),
					settings.getLoginServletUrl(), idpConfig.getSsoUrl(), idpConfig.getNameIdFormat(), idpConfig.getForceAuthentication(), idpConfig.getEnablePassiveSso());
			if (idpConfig.getSignedRequest()) {
				if (StringUtils.equals(idpConfig.getSsoBindingType(), "HttpPost")) {
					authnRequest = (AuthnRequest) MoSAMLUtils.signHttpPostRequest(authnRequest,
							settings.getPublicSPCertificate(), settings.getPrivateSPCertificate());
				}
			}

			Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(authnRequest);
			Element authDOM = marshaller.marshall(authnRequest);
			Document doc = authDOM.getOwnerDocument();
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			String requestMessage = out.toString();
			return requestMessage;

		} catch (Throwable t) {
			LOGGER.error("An error occurred while creating the AuthnRequest.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}
	}


	/**
	 * This function matches user's email domain to the domain of the rule. If it matches, the IDP of this rule will be used
	 * for redirection. If not found, the function returns null
	 *
	 * @param user     User object is used primarily to get the email address and match it
	 * @param username username is used if the user was not found, so in case of new users
	 * @param rule
	 * @return
	 */
	private String matchUserDomain(BambooUser user, String username, JSONObject rule) throws JSONException {
		if (user != null) {
			//Assigning email address of existing user so we can compare the domain
			username = user.getEmail();
		}
		//Get the domain from email
		String domain = username.substring(username.indexOf("@") + 1);
		//switch on conditionOperation
		JSONObject condition = rule.getJSONObject("condition");
		JSONObject domainObject = condition.getJSONObject("domain");
		String conditionOperation = domainObject.getString("conditionOperation");
		LOGGER.debug("conditionOperation " + conditionOperation);
		String ruleDomain = domainObject.getString(conditionOperation);
		LOGGER.debug("ruleDomain " + ruleDomain);
		//Using the switch case here so we can add contains, regex, etc.
		switch (conditionOperation) {
			//case equal
			case "equals":
				//if rule domain is equal to user's email domain
				if (StringUtils.equals(ruleDomain, domain)) {
					// Return IDP of the rule
					return rule.getString("idp");
				}
				break;
			case "regex":
				LOGGER.debug("Domain regex =" + ruleDomain);
				if (StringUtils.containsIgnoreCase(domain, ruleDomain)) {
					LOGGER.debug("Username matched configured regex");
					return rule.getString("idp");
				}
				break;
		}

		return null;
	}

	/**
	 * This function matches the
	 *
	 * @param user
	 * @param rule
	 * @return idp id if the group matches
	 */
	private String matchUserGroup(BambooUser user, JSONObject rule) throws JSONException {
		JSONObject condition = rule.getJSONObject("condition");
		JSONObject groupObect = condition.getJSONObject("group");
		String conditionOperation = groupObect.getString("conditionOperation");
		LOGGER.debug("conditionOperation " + conditionOperation);
		String ruleGroup = groupObect.getString(conditionOperation);
		LOGGER.debug("ruleGroup " + ruleGroup);

		//Using the switch case here so we can add contains, regex, startswith etc.
		switch (conditionOperation) {
			//case equal
			case "equals":
				if (userManager.isUserPresentInGroups(user.getName(), Collections.singletonList(ruleGroup)))
					return rule.getString("idp");
				break;
			case "regex":

				List<String> userGroups = (List<String>) userManager.getUserGroupName(user.getName());
				for (String userGroup : userGroups) {
					if (StringUtils.containsIgnoreCase(userGroup, ruleGroup)) {
						return rule.getString("idp");
					}
				}
				break;

		}
		return null;
	}

	private String matchUserDirectory(BambooUser bambooUser, JSONObject rule) throws JSONException {
		JSONObject condition = rule.getJSONObject("condition");
		LOGGER.debug("condition "+ condition);
		JSONObject groupObect = condition.getJSONObject("directory");
		LOGGER.debug("groupObject "+ groupObect);
		String conditionOperation = groupObect.getString("conditionOperation");
		LOGGER.debug("conditionOperation "+conditionOperation);
		String ruleDirectory = StringUtils.EMPTY;
		if(StringUtils.equals(conditionOperation,"regex")) {
			LOGGER.debug("Under regex condition");
			 ruleDirectory = groupObect.getString("regex");
		}else{
			LOGGER.debug("Under equals condition");
			ruleDirectory = groupObect.getString("equals");
		}
		LOGGER.debug("Rule Directory : "+ ruleDirectory);
		User user = crowdService.getUser(bambooUser.getName());
		LOGGER.debug("Directory name : " + crowdDirectoryService.findDirectoryById(user.getDirectoryId()).getName());
		switch (conditionOperation) {
			case "equals":
				if (StringUtils.equals(ruleDirectory, crowdDirectoryService.findDirectoryById(user.getDirectoryId()).getName())) {
					return rule.getString("idp");
				}
				break;
			case "regex":
				if (StringUtils.containsIgnoreCase(crowdDirectoryService.findDirectoryById(user.getDirectoryId()).getName(), ruleDirectory)) {
					return rule.getString("idp");
				}
				break;
		}
		return null;

	}


	private String createHttpPostResponseForm(String ssoUrl, String encodedResponse, String relayState) {
		String form = ("<html><head><script type=\"text/javascript\">window.onload = function() { document"
				+ ".forms['saml-request-form'].submit(); }</script></head><body>Please wait...<form action=\"") + ssoUrl
				+ "\" method=\"post\" id=\"saml-request-form\"><input type=\"hidden\" name=\"SAMLResponse\" "
				+ "value=\"" + MoSAMLUtils.htmlEncode(encodedResponse) + "\" /><input type=\"hidden\" "
				+ "name=\"RelayState\" value=\"" + MoSAMLUtils.htmlEncode(relayState) + "\" /></form></body></html>";
		return form;
	}


	public static void httpRedirect(HttpServletResponse response, String redirectUrl) throws IOException {
		LOGGER.debug("Redirecting user to " + redirectUrl);
		response.sendRedirect(redirectUrl);
	}

	public String formatXML(String xml) {
		String responseMessage = xml;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xml)));
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			responseMessage = out.toString();
		} catch (Throwable t) {
			LOGGER.error("An error occurred while parsing the response.", t);
			throw new MoSAMLException(MoSAMLException.SAMLErrorCode.UNKNOWN);
		}

		return responseMessage;
	}

	public static String getIssuerFromResponse(HttpServletRequest request) {
		MoSAMLUtils.doBootstrap();
		String encodedSAMLResponse = request.getParameter(MoSAMLUtils.SAML_RESPONSE_PARAM);
		try {
			Response samlResponse = MoSAMLUtils.decodeResponse(encodedSAMLResponse);
			return samlResponse.getIssuer().getValue();
		} catch (Exception e) {
			LOGGER.error("An error occurred while extracting issuer from SAML Response", e);
			return null;
		}
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}

	public MoSAMLUserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(MoSAMLUserManager userManager) {
		this.userManager = userManager;
	}

	public CrowdService getCrowdService() {
		return crowdService;
	}

	public void setCrowdService(CrowdService crowdService) {
		this.crowdService = crowdService;
	}

}