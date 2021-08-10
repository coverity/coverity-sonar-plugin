/*
 * Coverity Sonar Plugin
 * Copyright (c) 2021 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.ws;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * SOAP handler for user authentication using ws-security.  This mechanism inserts the user's user name and password in
 * the SOAP header of each message.
 */
public class ClientAuthenticationHandlerWSS implements SOAPHandler<SOAPMessageContext> {
    public static final String WSS_AUTH_PREFIX = "wsse";
    public static final String WSS_AUTH_LNAME = "Security";
    public static final String WSS_AUTH_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private XWSSProcessor xwssProcessor = null;

    private String quote(String in) {
        return in.replace("\"", "&quot;");
    }

    public ClientAuthenticationHandlerWSS(String userName, String password) {
        String xwssConfigText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<xwss:SecurityConfiguration xmlns:xwss=\"http://java.sun.com/xml/ns/xwss/config\"> " +
                "<xwss:UsernameToken name=\"" + quote(userName) + "\" " +
                "password=\"" + quote(password) + "\" " +
                "useNonce=\"false\" digestPassword=\"false\"/>  " +
                "</xwss:SecurityConfiguration>";
        InputStream xwssConfig = new ByteArrayInputStream(xwssConfigText.getBytes(StandardCharsets.UTF_8));
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            XWSSProcessorFactory factory = XWSSProcessorFactory.newInstance();
            xwssProcessor = factory.createProcessorForSecurityConfiguration(xwssConfig, new SecurityEnvironmentHandler());
        } catch(XWSSecurityException se) {
            throw new RuntimeException(se);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
    }

    public boolean handleFault(SOAPMessageContext mc) {
        return true;
    }

    public void close(MessageContext mc) {
    }

    public Set<QName> getHeaders() {
        QName securityHeader = new QName(WSS_AUTH_URI, WSS_AUTH_LNAME, WSS_AUTH_PREFIX);
        HashSet<QName> headers = new HashSet<QName>();
        headers.add(securityHeader);
        return headers;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        boolean outbound = ((Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)).booleanValue();
        SOAPMessage msg = smc.getMessage();
        if(outbound) {
            try {
                ProcessingContext context = xwssProcessor.createProcessingContext(msg);
                context.setSOAPMessage(msg);
                SOAPMessage secureMsg = xwssProcessor.secureOutboundMessage(context);
                smc.setMessage(secureMsg);
            } catch(XWSSecurityException ex) {
                throw new RuntimeException(ex);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    private static class SecurityEnvironmentHandler implements CallbackHandler {

        public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

        }
    }


}
