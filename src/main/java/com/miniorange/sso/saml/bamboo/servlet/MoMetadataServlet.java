package com.miniorange.sso.saml.bamboo.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.ContactPerson;
import org.opensaml.saml2.metadata.ContactPersonTypeEnumeration;
import org.opensaml.saml2.metadata.EmailAddress;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.GivenName;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.Organization;
import org.opensaml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml2.metadata.OrganizationName;
import org.opensaml.saml2.metadata.OrganizationURL;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml2.metadata.impl.AssertionConsumerServiceBuilder;
import org.opensaml.saml2.metadata.impl.ContactPersonBuilder;
import org.opensaml.saml2.metadata.impl.EmailAddressBuilder;
import org.opensaml.saml2.metadata.impl.EntityDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.GivenNameBuilder;
import org.opensaml.saml2.metadata.impl.KeyDescriptorBuilder;
import org.opensaml.saml2.metadata.impl.NameIDFormatBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationDisplayNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationNameBuilder;
import org.opensaml.saml2.metadata.impl.OrganizationURLBuilder;
import org.opensaml.saml2.metadata.impl.SPSSODescriptorBuilder;
import org.opensaml.saml2.metadata.impl.SingleLogoutServiceBuilder;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.X509Certificate;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.signature.impl.KeyInfoBuilder;
import org.opensaml.xml.signature.impl.X509CertificateBuilder;
import org.opensaml.xml.signature.impl.X509DataBuilder;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.utils.MoSAMLUtils;

public class MoMetadataServlet extends HttpServlet {

	private MoSAMLSettings settings;
	private static Log LOGGER = LogFactory.getLog(MoMetadataServlet.class);
	
	public MoMetadataServlet(MoSAMLSettings settings) {
		this.settings=settings;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LOGGER.debug("Download metadata servlet doGet() called");
		try {
			String certificate = settings.getPublicSPCertificate();
			certificate = MoSAMLUtils.deserializePublicCertificate(certificate);
			String metadata = generateMetadata(settings.getSpEntityId(), settings.getSigning(), settings.getEncryption(), certificate, settings.getLoginServletUrl(),settings.getLogoutServletUrl());
			response.setContentType(MediaType.TEXT_XML);
			response.getOutputStream().write(metadata.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			PrintWriter writer = response.getWriter();
			writer.print("An error occurred while generating the metadata.");
			writer.close();
		}
		
	}
	
	private String generateMetadata(String entityId, Boolean signingCert, Boolean encryptionCert, String certificate, String acsurl, String singleLogOutLocation) {
		LOGGER.debug("generate metadata callled");
		MoSAMLUtils.doBootstrap();
		EntityDescriptorBuilder builder = new EntityDescriptorBuilder();
		SPSSODescriptorBuilder spssoDescriptorBuilder = new SPSSODescriptorBuilder();
		KeyDescriptorBuilder keyDescriptorBuilder = new KeyDescriptorBuilder();
		KeyInfoBuilder keyInfoBuilder = new KeyInfoBuilder();
		X509DataBuilder x509DataBuilder = new X509DataBuilder();
		X509CertificateBuilder x509CertificateBuilder = new X509CertificateBuilder();
		NameIDFormatBuilder nameIdFormatBuilder =  new NameIDFormatBuilder();
		AssertionConsumerServiceBuilder assertionConsumerServiceBuilder = new AssertionConsumerServiceBuilder();
		SingleLogoutServiceBuilder singleLogOutServiceBuilder = new SingleLogoutServiceBuilder();
		OrganizationBuilder organizationBuilder = new OrganizationBuilder();
		OrganizationNameBuilder organizationNameBuilder = new OrganizationNameBuilder();
		OrganizationDisplayNameBuilder organizationDisplayNameBuilder = new OrganizationDisplayNameBuilder();
		OrganizationURLBuilder organizationUrlBuilder = new OrganizationURLBuilder();
		ContactPersonBuilder contactPersonBuilder = new ContactPersonBuilder();
		GivenNameBuilder givenNameBuilder =new GivenNameBuilder();
		EmailAddressBuilder emailAddressBuilder = new EmailAddressBuilder();

		EntityDescriptor entityDescriptor = builder.buildObject();
		SPSSODescriptor spssoDescriptor = spssoDescriptorBuilder.buildObject();
		AssertionConsumerService assertionConsumerService = assertionConsumerServiceBuilder.buildObject();
		Organization organization = organizationBuilder.buildObject();
		ContactPerson contactPersonTechnical = contactPersonBuilder.buildObject();
		ContactPerson contactPersonSupport = contactPersonBuilder.buildObject();

		entityDescriptor.setEntityID(entityId);
		
		spssoDescriptor.setWantAssertionsSigned(true);
		spssoDescriptor.addSupportedProtocol("urn:oasis:names:tc:SAML:2.0:protocol");
		
		//signing
		if(BooleanUtils.toBoolean(signingCert)){
			spssoDescriptor.setAuthnRequestsSigned(true);
			KeyDescriptor signingKeyDescriptor = keyDescriptorBuilder.buildObject();
			signingKeyDescriptor.setUse(UsageType.SIGNING);
			KeyInfo signingKeyInfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
			X509Data signingX509Data = x509DataBuilder.buildObject(X509Data.DEFAULT_ELEMENT_NAME);
			X509Certificate signingX509Certificate = x509CertificateBuilder
					.buildObject(X509Certificate.DEFAULT_ELEMENT_NAME);
			signingX509Certificate.setValue(certificate);
			signingX509Data.getX509Certificates().add(signingX509Certificate);
			signingKeyInfo.getX509Datas().add(signingX509Data);
			signingKeyDescriptor.setKeyInfo(signingKeyInfo);
			spssoDescriptor.getKeyDescriptors().add(signingKeyDescriptor);
		}

		//encryption
		if(BooleanUtils.toBoolean(encryptionCert)) {
			KeyDescriptor encKeyDescriptor = keyDescriptorBuilder.buildObject();
			encKeyDescriptor.setUse(UsageType.ENCRYPTION);
			KeyInfo encKeyInfo = keyInfoBuilder.buildObject(KeyInfo.DEFAULT_ELEMENT_NAME);
			X509Data encX509Data = x509DataBuilder.buildObject(X509Data.DEFAULT_ELEMENT_NAME);
			X509Certificate encX509Certificate = x509CertificateBuilder
					.buildObject(X509Certificate.DEFAULT_ELEMENT_NAME);
			encX509Certificate.setValue(certificate);
			encX509Data.getX509Certificates().add(encX509Certificate);
			encKeyInfo.getX509Datas().add(encX509Data);
			encKeyDescriptor.setKeyInfo(encKeyInfo);
			spssoDescriptor.getKeyDescriptors().add(encKeyDescriptor);
		}

		SingleLogoutService singleLogoutServiceRedir = singleLogOutServiceBuilder.buildObject();
		singleLogoutServiceRedir
		.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		singleLogoutServiceRedir.setLocation(singleLogOutLocation);
		spssoDescriptor.getSingleLogoutServices().add(singleLogoutServiceRedir);

		SingleLogoutService singleLogoutServicePost = singleLogOutServiceBuilder.buildObject();
		singleLogoutServicePost.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
		singleLogoutServicePost.setLocation(singleLogOutLocation);
		spssoDescriptor.getSingleLogoutServices().add(singleLogoutServicePost);

		List<String> nameIds = new ArrayList<>();
		nameIds.add("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		nameIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
		nameIds.add("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");

		for (String nameId : nameIds) {
			NameIDFormat nameIDFormat = nameIdFormatBuilder.buildObject();
			nameIDFormat.setFormat(nameId);
			spssoDescriptor.getNameIDFormats().add(nameIDFormat);
		}
		
		assertionConsumerService.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		assertionConsumerService.setLocation(acsurl);
		assertionConsumerService.setIndex(1);
		spssoDescriptor.getAssertionConsumerServices().add(assertionConsumerService);
		
		entityDescriptor.getRoleDescriptors().add(spssoDescriptor);

		OrganizationName organizationName = organizationNameBuilder.buildObject();
		organizationName.setName(new LocalizedString(settings.getOrganizationName(),Locale.getDefault().getLanguage()));
		organization.getOrganizationNames().add(organizationName);
		OrganizationDisplayName organizationDisplayName = organizationDisplayNameBuilder.buildObject();
		organizationDisplayName.setName(new LocalizedString(settings.getOrganizationDisplayName(),Locale.getDefault().getLanguage()));
		organization.getDisplayNames().add(organizationDisplayName);
		OrganizationURL organizationURL = organizationUrlBuilder.buildObject();
		organizationURL.setURL(new LocalizedString(settings.getOrganizationUrl(), Locale.getDefault().getLanguage()));
		organization.getURLs().add(organizationURL);
		entityDescriptor.setOrganization(organization);

		contactPersonTechnical.setType(ContactPersonTypeEnumeration.TECHNICAL);
		GivenName givenNameTechnical = givenNameBuilder.buildObject();
		givenNameTechnical.setName(settings.getTechnicalContactName());
		contactPersonTechnical.setGivenName(givenNameTechnical);
		EmailAddress emailAddressTechnical = emailAddressBuilder.buildObject();
		emailAddressTechnical.setAddress(settings.getTechnicalContactEmail());
		contactPersonTechnical.getEmailAddresses().add(emailAddressTechnical);

		contactPersonSupport.setType(ContactPersonTypeEnumeration.SUPPORT);
		GivenName givenNameSupport = givenNameBuilder.buildObject();
		givenNameSupport.setName(settings.getSupportContactName());
		contactPersonSupport.setGivenName(givenNameSupport);
		EmailAddress emailAddressSupport = emailAddressBuilder.buildObject();
		emailAddressSupport.setAddress(settings.getSupportContactEmail());
		contactPersonSupport.getEmailAddresses().add(emailAddressSupport);

		entityDescriptor.getContactPersons().add(contactPersonTechnical);
		entityDescriptor.getContactPersons().add(contactPersonSupport);

		try {
			MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
			Marshaller marshaller = marshallerFactory.getMarshaller(entityDescriptor);
			Element element = marshaller.marshall(entityDescriptor);
			return XMLHelper.nodeToString(element);
		} catch(Exception e) {
			LOGGER.debug("Marshalling Exception:" + e);
		}
		return null;
	}

	public void setSettings(MoSAMLSettings settings) {
		this.settings = settings;
	}
}
