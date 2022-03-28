package com.miniorange.sso.saml.utils;

import com.atlassian.json.jsonorg.JSONArray;
import com.atlassian.json.jsonorg.JSONException;
import com.atlassian.json.jsonorg.JSONObject;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.*;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.*;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;

import com.miniorange.sso.saml.MoSAMLException;
import com.miniorange.sso.saml.bamboo.action.MoAddIDPConfigurationAction;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.*;
import java.net.URLEncoder;
import java.security.*;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class MoSAMLUtils {

	private static Log LOGGER = LogFactory.getLog(MoSAMLUtils.class);

	public static final String SAML_REQUEST_PARAM = "SAMLRequest";
	public static final String RELAY_STATE_PARAM = "RelayState";
	public static final String SIGNATURE_ALGO_PARAM = "SigAlg";
	public static final String SIGNATURE_PARAM = "Signature";
	public static final String SAML_RESPONSE_PARAM = "SAMLResponse";

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	
	public static List<String> certificates = new ArrayList<>();

	private static boolean bootstrap = false;
	
	public static void doBootstrap() {
		if (!bootstrap) {
			try {
				bootstrap = true;
				DefaultBootstrap.bootstrap();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
	}

	public static AuthnRequest buildAuthnRequest(String issuer, String acsUrl, String destination,String nameIdFormat,Boolean forceAuthentication,Boolean enablePassiveSso) {
		AuthnRequest authnRequest = new AuthnRequestBuilder()
				.buildObject(SAMLConstants.SAML20P_NS, AuthnRequest.DEFAULT_ELEMENT_LOCAL_NAME, "samlp");
		DateTime issueInstant = new DateTime();
		authnRequest.setID(generateRandomString());
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssueInstant(issueInstant);
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		authnRequest.setIssuer(buildIssuer(issuer));
		authnRequest.setAssertionConsumerServiceURL(acsUrl);
		authnRequest.setDestination(destination);
		authnRequest.setForceAuthn(forceAuthentication);
		NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
		NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
		nameIdPolicy.setFormat(nameIdFormat);
		nameIdPolicy.setAllowCreate(true);
		authnRequest.setNameIDPolicy(nameIdPolicy);
		LOGGER.info("Value of Passive SSO" + enablePassiveSso);
		authnRequest.setIsPassive(enablePassiveSso);
		return authnRequest;
	}
	
	public static LogoutRequest buildLogoutRequest(String issuer, String destination, String nameId, String
			sessionIndex) {
		LogoutRequest logoutRequest = new LogoutRequestBuilder()
				.buildObject(SAMLConstants.SAML20P_NS, LogoutRequest.DEFAULT_ELEMENT_LOCAL_NAME, "samlp");
		DateTime issueInstant = new DateTime();
		logoutRequest.setIssueInstant(issueInstant);
		logoutRequest.setID(generateRandomString());
		logoutRequest.setVersion(SAMLVersion.VERSION_20);
		logoutRequest.setIssuer(buildIssuer(issuer));
		logoutRequest.setDestination(destination);

		NameID nameIdObj = new NameIDBuilder()
				.buildObject(SAMLConstants.SAML20_NS, NameID.DEFAULT_ELEMENT_LOCAL_NAME, "saml");
		nameIdObj.setValue(nameId);
		logoutRequest.setNameID(nameIdObj);

		SessionIndex sessionIndexObj = new SessionIndexBuilder()
				.buildObject(SAMLConstants.SAML20P_NS, SessionIndex.DEFAULT_ELEMENT_LOCAL_NAME, "samlp");
		sessionIndexObj.setSessionIndex(sessionIndex);
		logoutRequest.getSessionIndexes().add(sessionIndexObj);

		return logoutRequest;
	}

	public static LogoutResponse buildLogoutResponse(String issuer, String destination, String inResponseTo, String
			status) {
		LogoutResponse logoutResponse = new LogoutResponseBuilder()
				.buildObject(SAMLConstants.SAML20P_NS, LogoutResponse.DEFAULT_ELEMENT_LOCAL_NAME, "samlp");
		DateTime issueInstant = new DateTime();
		logoutResponse.setIssueInstant(issueInstant);
		logoutResponse.setID(generateRandomString());
		logoutResponse.setVersion(SAMLVersion.VERSION_20);
		logoutResponse.setIssuer(buildIssuer(issuer));
		logoutResponse.setDestination(destination);
		logoutResponse.setInResponseTo(inResponseTo);
		logoutResponse.setStatus(buildStatus(status));
		return logoutResponse;
	}

	public static LogoutRequest readLogoutRequest(String logoutRequestStr, Boolean isPostBinding) throws
			ParserConfigurationException, IOException, SAXException, UnmarshallingException, DataFormatException {
		byte[] base64Decoded = org.opensaml.xml.util.Base64.decode(logoutRequestStr);
		String requestXml = new String(base64Decoded, "UTF-8");
		if (!isPostBinding) {
			Inflater inflater = new Inflater(true);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(byteArrayOutputStream, inflater);
			inflaterOutputStream.write(base64Decoded);
			inflaterOutputStream.close();
			byteArrayOutputStream.close();
			requestXml = byteArrayOutputStream.toString();
		}
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		disableExternalEntityParsing(documentBuilderFactory);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(requestXml.getBytes());
		Document document = docBuilder.parse(is);
		Element element = document.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
		XMLObject xmlObj = unmarshaller.unmarshall(element);
		LogoutRequest logoutRequest = (LogoutRequest) xmlObj;
		return logoutRequest;
	}

	private static Issuer buildIssuer(String issuerValue) {
		Issuer issuer = new IssuerBuilder()
				.buildObject(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, "saml");
		issuer.setValue(issuerValue);
		return issuer;
	}
	
	private static Status buildStatus(String statusCodeValue) {
		StatusCode statusCode = new StatusCodeBuilder().buildObject(SAMLConstants.SAML20P_NS,
				StatusCode.DEFAULT_ELEMENT_LOCAL_NAME, "samlp");
		statusCode.setValue(statusCodeValue);
		Status status = new StatusBuilder().buildObject(SAMLConstants.SAML20P_NS, Status.DEFAULT_ELEMENT_LOCAL_NAME,
				"samlp");
		status.setStatusCode(statusCode);
		return status;
	}

	public static String base64EncodeRequest(XMLObject request, Boolean isHttpPostBinding) throws Exception {
		// converting to a DOM
		Marshaller marshaller = Configuration.getMarshallerFactory().getMarshaller(request);
		Element authDOM = marshaller.marshall(request);

		// DOM to string
		StringWriter requestWriter = new StringWriter();
		XMLHelper.writeNode(authDOM, requestWriter);
		String requestMessage = requestWriter.toString();

		if (isHttpPostBinding) {
			String authnRequestStr = Base64.encodeBytes(requestMessage.getBytes(), Base64.DONT_BREAK_LINES);
			return authnRequestStr;
		}
		// compressing
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
		deflaterOutputStream.write(requestMessage.getBytes());
		deflaterOutputStream.close();
		byteArrayOutputStream.close();
		String encodedRequestMessage = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
		return encodedRequestMessage;
	}

	public static Response decodeResponse(String encodedResponse) throws Exception {
		String xml = new String(Base64.decode(encodedResponse), "UTF-8");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setIgnoringComments(true);
		disableExternalEntityParsing(documentBuilderFactory);
		DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		Document document = docBuilder.parse(is);
		Element element = document.getDocumentElement();
		UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
		Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
		XMLObject xmlObj = unmarshaller.unmarshall(element);
		Response response = (Response) xmlObj;
		return response;
	}

	public static Boolean verifyCertificate(SignableXMLObject response, String certificate) throws ValidationException,
			CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
		if (response.isSigned()) {
			SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
			profileValidator.validate(response.getSignature());
			Credential verificationCredential = getCredential(certificate, "");
			SignatureValidator sigValidator = new SignatureValidator(verificationCredential);
			sigValidator.validate(response.getSignature());
			return Boolean.TRUE;
		} else {
			if (response instanceof Response) {
				throw new MoSAMLException(MoSAMLException.SAMLErrorCode.RESPONSE_NOT_SIGNED);
			} else {
				throw new MoSAMLException(MoSAMLException.SAMLErrorCode.ASSERTION_NOT_SIGNED);
			}
		}
	}

	private static Credential getCredential(String publicKey, String privateKeyStr) throws CertificateException,
			InvalidKeySpecException, NoSuchAlgorithmException {
		publicKey = serializePublicCertificate(publicKey);
		InputStream is = new ByteArrayInputStream(publicKey.getBytes());
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf.generateCertificate(is);
		BasicX509Credential x509Credential = new BasicX509Credential();
		x509Credential.setPublicKey(cert.getPublicKey());
		PrivateKey privateKey = getPrivateKey(privateKeyStr);
		if (privateKey != null) {
			x509Credential.setPrivateKey(privateKey);
		}
		Credential credential = x509Credential;
		return credential;
	}

	private static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException,
			InvalidKeySpecException {
		if (StringUtils.isNotBlank(privateKey)) {
			privateKey = deserializePrivateCertificate(privateKey);
			byte[] bytes = Base64.decode(privateKey);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			return kf.generatePrivate(spec);
		}
		return null;
	}

	public static String serializePublicCertificate(String certificate) {
		String BEGIN_CERTIFICATE = "BEGIN CERTIFICATE";
		String END_CERTIFICATE = "END CERTIFICATE";
		if (StringUtils.isNotBlank(certificate)) {
			certificate = StringUtils.remove(certificate, "\r");
			certificate = StringUtils.remove(certificate, "\n");
			certificate = StringUtils.remove(certificate, "-");
			certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
			certificate = StringUtils.remove(certificate, END_CERTIFICATE);
			certificate = StringUtils.remove(certificate, " ");
			org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
			certificate = encoder.encodeToString(org.apache.commons.codec.binary.Base64.decodeBase64(certificate));
			StringBuffer cert = new StringBuffer("-----" + BEGIN_CERTIFICATE + "-----\r\n");
			cert.append(certificate);
			cert.append("-----" + END_CERTIFICATE + "-----");
			return cert.toString();
		}
		return certificate;
	}

	public static String serializePrivateCertificate(String certificate) {
		String BEGIN_CERTIFICATE = "BEGIN PRIVATE KEY";
		String END_CERTIFICATE = "END PRIVATE KEY";
		if (StringUtils.isNotBlank(certificate)) {
			certificate = StringUtils.remove(certificate, "\r");
			certificate = StringUtils.remove(certificate, "\n");
			certificate = StringUtils.remove(certificate, "-");
			certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
			certificate = StringUtils.remove(certificate, END_CERTIFICATE);
			certificate = StringUtils.remove(certificate, " ");
			org.apache.commons.codec.binary.Base64 encoder = new org.apache.commons.codec.binary.Base64(64);
			certificate = encoder.encodeToString(org.apache.commons.codec.binary.Base64.decodeBase64(certificate));
			StringBuffer cert = new StringBuffer("-----" + BEGIN_CERTIFICATE + "-----\r\n");
			cert.append(certificate);
			cert.append("-----" + END_CERTIFICATE + "-----");
			return cert.toString();
		}
		return certificate;
	}

	public static String deserializePublicCertificate(String certificate) {
		String BEGIN_CERTIFICATE = "BEGIN CERTIFICATE";
		String END_CERTIFICATE = "END CERTIFICATE";
		if (StringUtils.isNotBlank(certificate)) {
			certificate = StringUtils.remove(certificate, "\r");
			certificate = StringUtils.remove(certificate, "\n");
			certificate = StringUtils.remove(certificate, "-");
			certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
			certificate = StringUtils.remove(certificate, END_CERTIFICATE);
			certificate = StringUtils.remove(certificate, " ");
		}
		return certificate;
	}

	public static String deserializePrivateCertificate(String certificate) {
		String BEGIN_CERTIFICATE = "BEGIN PRIVATE KEY";
		String END_CERTIFICATE = "END PRIVATE KEY";
		if (StringUtils.isNotBlank(certificate)) {
			certificate = StringUtils.remove(certificate, "\r");
			certificate = StringUtils.remove(certificate, "\n");
			certificate = StringUtils.remove(certificate, "-");
			certificate = StringUtils.remove(certificate, BEGIN_CERTIFICATE);
			certificate = StringUtils.remove(certificate, END_CERTIFICATE);
			certificate = StringUtils.remove(certificate, " ");
		}
		return certificate;
	}

	public static Boolean isValidPublicCertificate(String certificate) {
		certificate = serializePublicCertificate(certificate);
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(certificate
							.getBytes()));
			return Boolean.TRUE;
		} catch (CertificateException e) {
			return Boolean.FALSE;
		}
	}

	public static Boolean isValidPrivateCertificate(String certificate) {
		certificate = serializePrivateCertificate(certificate);
		try {
			getPrivateKey(certificate);
			return Boolean.TRUE;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}

	public static String generateRandomString() {
		String uuid = UUID.randomUUID().toString();
		return "_" + StringUtils.remove(uuid, '-');
	}

	public static int getAssertionIDResetInterval(String resetAssertionIDList, Integer customResetInterval)
	{
		LOGGER.debug("getAssertionIDResetInterval called.");
		int resetInterval=24;
		if (StringUtils.equalsIgnoreCase(resetAssertionIDList, "daily")) {
			resetInterval = 24 ;
			LOGGER.debug("Reset Interval set to: "+ resetInterval);
		}  else  if (StringUtils.equalsIgnoreCase(resetAssertionIDList, "custom")){
			resetInterval=  customResetInterval;
			LOGGER.debug("Reset Interval set to: "+ resetInterval);
		}
		return resetInterval;
	}

	public static String signHttpRedirectRequest(String requestQueryString, String sigAlgo, String pubicKey, String
			privateKey) throws
			Exception {
		StringBuilder builder = new StringBuilder(requestQueryString);
		builder.append("&").append(SIGNATURE_ALGO_PARAM).append("=").append(URLEncoder.encode(sigAlgo, "UTF-8"));
		Signature signature = Signature.getInstance("SHA256withRSA");
		Credential credentials = getCredential(pubicKey, privateKey);
		signature.initSign(credentials.getPrivateKey());
		signature.update(builder.toString().getBytes());
		byte[] signatureByteArray = signature.sign();
		String signatureBase64encodedString = Base64.encodeBytes(signatureByteArray);
		//builder.append("&").append(SIGNATURE_PARAM).append("=").append(URLEncoder.encode(signatureBase64encodedString,
		//"UTF-8").trim());
		return signatureBase64encodedString;
	}

	public static SignableSAMLObject signHttpPostRequest(SignableSAMLObject request, String publicKey,
	                                                     String privateKey) throws Exception {
		org.opensaml.xml.signature.Signature signature = (org.opensaml.xml.signature.Signature) Configuration
				.getBuilderFactory().getBuilder(
						org.opensaml.xml.signature.Signature.DEFAULT_ELEMENT_NAME)
				.buildObject(org.opensaml.xml.signature.Signature.DEFAULT_ELEMENT_NAME);

		// Pass certificate type to get credentials
		Credential credential = getCredential(publicKey, privateKey);

		signature.setSigningCredential(credential);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

		KeyInfo keyInfo=(KeyInfo)Configuration.getBuilderFactory().getBuilder(KeyInfo.DEFAULT_ELEMENT_NAME)
				.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);

		X509Data data = (X509Data) Configuration.getBuilderFactory().getBuilder(X509Data.DEFAULT_ELEMENT_NAME).buildObject(X509Data.DEFAULT_ELEMENT_NAME);

		org.opensaml.xml.signature.X509Certificate xmlCert = (org.opensaml.xml.signature.X509Certificate) Configuration
				.getBuilderFactory().getBuilder(org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME)
				.buildObject(org.opensaml.xml.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
		xmlCert.setValue(deserializePublicCertificate(publicKey));
		data.getX509Certificates().add(xmlCert);
		keyInfo.getX509Datas().add(data);

		signature.setKeyInfo(keyInfo);
		String signatureAlgo = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
		signature.setSignatureAlgorithm(signatureAlgo);

		request.setSignature(signature);

		// Marshalling signableXmlObject
		MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
		Marshaller marshaller = marshallerFactory.getMarshaller(request);
		marshaller.marshall(request);

		// Signing signableXmlObject
		Signer.signObject(signature);

		return request;

/*
		org.opensaml.xmlsec.signature.Signature signature = OpenSAMLUtil.buildSignature();
		Credential credential = getCredential(pubicKey, privateKey);
		signature.setSigningCredential(getCredential(pubicKey, privateKey));
		signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
		signature.setCanonicalizationAlgorithm(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		signature.setSigningCredential(credential);
		signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

		X509KeyInfoGeneratorFactory kiFactory = new X509KeyInfoGeneratorFactory();
		kiFactory.setEmitEntityCertificate(true);

		try {
			KeyInfo keyInfo = kiFactory.newInstance().generate(credential);
			signature.setKeyInfo(keyInfo);
		} catch (org.opensaml.security.SecurityException ex) {
			throw ex;
		}

		String signatureAlgo = SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256;
		signature.setSignatureAlgorithm(signatureAlgo);
		request.setSignature(signature);
		request.releaseDOM();
		request.releaseChildrenDOM(true);
		return request;*/
	}

	public static Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, String publicKey, String
			privateKey) throws CertificateException, InvalidKeySpecException, NoSuchAlgorithmException,
			DecryptionException {
		StaticKeyInfoCredentialResolver keyInfoCredentialResolver = new StaticKeyInfoCredentialResolver(
				getCredential(publicKey, privateKey));
		Decrypter decrypter = new Decrypter(null, keyInfoCredentialResolver, new InlineEncryptedKeyResolver());
		Iterator<EncryptedKey> it = decrypter.getEncryptedKeyResolver().resolve(encryptedAssertion.getEncryptedData()).iterator();
		if(!it.hasNext()){
			decrypter = new Decrypter(null, keyInfoCredentialResolver, new EncryptedElementTypeEncryptedKeyResolver());
		}
		decrypter.setRootInNewDocument(true);
		return decrypter.decrypt(encryptedAssertion);
	}

	public static String htmlEncode(String s) {
		if (StringUtils.isNotBlank(s)) {
			StringBuffer encodedString = new StringBuffer("");
			char[] chars = s.toCharArray();
			for (char c : chars) {
				if (c == '<') {
					encodedString.append("&lt;");
				} else if (c == '>') {
					encodedString.append("&gt;");
				} else if (c == '\'') {
					encodedString.append("&apos;");
				} else if (c == '"') {
					encodedString.append("&quot;");
				} else if (c == '&') {
					encodedString.append("&amp;");
				} else {
					encodedString.append(c);
				}
			}
			return encodedString.toString();
		}
		return StringUtils.EMPTY;
	}

	public static HashMap<String, Object> parseXml(String fileContents) {
		
		HashMap<String, Object> configValues = new HashMap<String, Object>();
		String idpEntityId = null;
		String singleSignOnUrl = null;
		String singleLogoutUrl = null;
		String ssoBinding = null;
		String sloBinding = null;	
		Boolean isValidCertificate = true;
		certificates.clear();
			
		DocumentBuilderFactory dbFactory;
		DocumentBuilder dBuilder;
		Document doc;

		try{
				dbFactory = DocumentBuilderFactory.newInstance();
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(new InputSource(new StringReader(fileContents)));
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName(doc.getDocumentElement().getNodeName());
				Node nNode = nList.item(0);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					idpEntityId = eElement.getAttribute("entityID");
					NodeList nList1 = eElement.getElementsByTagName("md:KeyDescriptor");
					if(nList1.getLength()==0)
						nList1 = eElement.getElementsByTagName("KeyDescriptor");
					for (int temp1 = 0; temp1 < nList1.getLength(); temp1++) {
						Node nNode1 = nList1.item(temp1);
						Element eElement1 = (Element) nNode1;
						if(eElement1.getAttribute("use").equals("signing")){
							boolean isRepeated=false;
							for(int index=0;index < certificates.size();index++)
							{
								if(!MoSAMLUtils.isValidPublicCertificate(certificates.get(index))) {
									isValidCertificate = false;
								}
								else {
									//isValidCertificate = true;
									if(MoSAMLUtils.serializePublicCertificate(nNode1.getTextContent()).equals(certificates.get(index))){
										isRepeated=true;
										break;
									}
								}		
							}	
							if(!isRepeated) {
								certificates.add(MoSAMLUtils.serializePublicCertificate(nNode1.getTextContent()));
								if(!MoSAMLUtils.isValidPublicCertificate(certificates.get(certificates.size()-1)).booleanValue())
									isValidCertificate = false;
							}
						}
					}
					NodeList nList2 = doc.getElementsByTagName("md:SingleSignOnService");
					if(nList2.getLength()==0)
						nList2 = doc.getElementsByTagName("SingleSignOnService");
					if(nList2.getLength()!=0){
						Node nNode2 = nList2.item(0);
						Element eElement2 = (Element) nNode2;
						if(eElement2.getAttribute("Binding").contains("Redirect"))
							ssoBinding = "HttpRedirect";
						else
							ssoBinding = "HttpPost";
						singleSignOnUrl = eElement2.getAttribute("Location");
					}
					NodeList nList3 = doc.getElementsByTagName("md:SingleLogoutService");
					if(nList3.getLength()==0)
						nList3 = doc.getElementsByTagName("SingleLogoutService");
					if(nList3.getLength() != 0){
						Node nNode3 = nList3.item(0);
						Element eElement3 = (Element) nNode3;
						if(eElement3.getAttribute("Binding").contains("Redirect"))
							sloBinding = "HttpRedirect";
						else
							sloBinding = "HttpPost";
						singleLogoutUrl = eElement3.getAttribute("Location");
					}
				}


				if((idpEntityId == "") || (singleSignOnUrl == "") || (certificates.size() == 0) || (!isValidCertificate)){
					configValues.put("message", "error");
					return configValues;
				}
				else{
					configValues.put("idpEntityId", idpEntityId);
					configValues.put("ssoBinding", ssoBinding);
					configValues.put("singleSignOnUrl",singleSignOnUrl);
					configValues.put("sloBinding",sloBinding);
					configValues.put("singleLogoutUrl",singleLogoutUrl);
					configValues.put("X509Certificate",certificates.get(0));
					configValues.put("allX509Certificates",certificates);
					configValues.put("message", "success");
					return configValues;
				}
			} catch(Exception e){
			LOGGER.debug("There is an exception in parsing file" + e.getMessage());
			}
			return null;
		}

	public static int getMetadataRefreshInterval(String refreshInterval, Integer customRefreshInterval, String
			customRefreshIntervalUnit) {
		int interval;
		if (StringUtils.equalsIgnoreCase(refreshInterval, "hourly")) {
			interval = 60;
		} else if (StringUtils.equalsIgnoreCase(refreshInterval, "twicedaily")) {
			interval = 12 * 60;
		} else if (StringUtils.equalsIgnoreCase(refreshInterval, "daily")) {
			interval = 24 * 60;
		} else if (StringUtils.equalsIgnoreCase(refreshInterval, "weekly")) {
			interval = 7 * 24 * 60;
		} else if (StringUtils.equalsIgnoreCase(refreshInterval, "monthly")) {
			interval = 30 * 24 * 60;
		} else {
			if(StringUtils.equalsIgnoreCase(customRefreshIntervalUnit, "hours")) {
				interval = customRefreshInterval * 60;
			} else if(StringUtils.equalsIgnoreCase(customRefreshIntervalUnit, "days")) {
				interval = customRefreshInterval * 24 * 60;
			} else {
				interval = customRefreshInterval;
			}
		}
		return interval;
	}

	/**
	 * getCertificateInfo function retrieves details of x509 certificate and returns it
	 * in a map
	 * Parameter: X509 certificate string
	 * Return: Map<String,String> certificateInfo
	 * */
	public static Map<String, String> getCertificateInfo(String certificate) {
		LOGGER.debug("Retriving certificate information");
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(certificate
							.getBytes()));
			Map<String, String> certificateInfo = new LinkedHashMap<String,String>();
			certificateInfo.put("Version", Integer.toString(cert.getVersion())+"V");
			certificateInfo.put("Serial Number", cert.getSerialNumber().toString(16));
			certificateInfo.put("Signature Algorithm",cert.getSigAlgName());
			certificateInfo.put("Issuer", cert.getIssuerX500Principal().toString());
			certificateInfo.put("Subject",cert.getSubjectX500Principal().toString());
			certificateInfo.put("Valid From",cert.getNotBefore().toString());
			certificateInfo.put("Valid To",cert.getNotAfter().toString());
			certificateInfo.put("Public Key", cert.getPublicKey().getAlgorithm());
			certificateInfo.put("Thumbprint Algorithm","sha1");
			certificateInfo.put("Thumbprint",getThumbPrint(cert));
			return certificateInfo;
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.debug(e.getMessage());
			LOGGER.debug(e.getCause());
		}
		return null;
	}

	/**
	 *  getThumbPrint function retrieves hexadecimal thumbprint of the certificate
	 *  Parameter: X509 certificate
	 *  Return: Hexadecimal String thumbprint
	 */
	private static String getThumbPrint(X509Certificate cert) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] encodedCert = cert.getEncoded();
			md.update(encodedCert);
			return hexify(md.digest());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			LOGGER.debug(e.getMessage());
			LOGGER.debug(e.getCause());
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			LOGGER.debug(e.getMessage());
			LOGGER.debug(e.getCause());
		}
		return StringUtils.EMPTY;
	}

	private static String hexify(byte[] digest) {
		// TODO Auto-generated method stub
		char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		StringBuffer buf = new StringBuffer(digest.length * 2);

		for (int i = 0; i < digest.length; ++i) {
			buf.append(hexDigits[(digest[i] & 0xf0) >> 4]);
			buf.append(hexDigits[digest[i] & 0x0f]);
		}

		return buf.toString();
	}

	public static Map<String, List<String>> toMap(JSONObject jsonobj) throws JSONException {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		Iterator<String> keys = jsonobj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonobj.get(key);
			List<String> tempList = new ArrayList<>();
			if (value instanceof JSONArray) {
				tempList = toList((JSONArray) value);
			}
			map.put(key, tempList);
		}
		return map;
	}

	public static List<String> toList(JSONArray array) throws JSONException {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			} else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value.toString());
		}
		return list;
	}


	public static String sanitizeText(String text) {
		if(StringUtils.isBlank(text)){
			return text;
		}
		//Removing all the HTML Tags
		LOGGER.debug("Text before sanitization: "+text);
		text = Jsoup.parse(text).text();
		LOGGER.debug("Text after sanitization: "+text);
		return text;
	}

	public static ArrayList<String> parseIdpTestConfigObject(Map<String, List<String>> testConfig) {
		LOGGER.debug("parseIdpTestConfigObject");
		ArrayList<String> testConfigList = new ArrayList<String>();
		if (testConfig.size() != 0) {
			testConfigList.add("--Select--");
			for (String key : testConfig.keySet()) {
				List<String> tempList = testConfig.get(key);
				String entry = key + "    " + tempList;
				testConfigList.add(entry);
			}
			return testConfigList;
		} else {
			testConfigList.add("Attributes not found");
			return testConfigList;
		}
	}

	private static void disableExternalEntityParsing(DocumentBuilderFactory dbf){
		LOGGER.info("Disabling External Entity Parsing from DocumentBuilderFactory");
		String FEATURE = null;
		try {
			// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
			// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
			FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			dbf.setFeature(FEATURE, true);

			// If you can't completely disable DTDs, then at least do the following:
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			// JDK7+ - http://xml.org/sax/features/external-general-entities
			FEATURE = "http://xml.org/sax/features/external-general-entities";
			dbf.setFeature(FEATURE, false);

			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
			// JDK7+ - http://xml.org/sax/features/external-parameter-entities
			FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			dbf.setFeature(FEATURE, false);

			// Disable external DTDs as well
			FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			dbf.setFeature(FEATURE, false);

			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);

		} catch (ParserConfigurationException e) {
			// This should catch a failed setFeature feature
			LOGGER.debug("ParserConfigurationException was thrown. The feature '" +
					FEATURE + "' is probably not supported by your XML processor.");
		}
	}

}