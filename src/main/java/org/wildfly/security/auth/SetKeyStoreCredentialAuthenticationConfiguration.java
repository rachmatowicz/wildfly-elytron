/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.security.auth;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500PrivateCredential;

import org.wildfly.security.auth.callback.CredentialCallback;
import org.wildfly.security.keystore.PasswordEntry;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.spec.ClearPasswordSpec;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class SetKeyStoreCredentialAuthenticationConfiguration extends AuthenticationConfiguration {

    private final KeyStore keyStore;
    private final String alias;
    private final KeyStore.ProtectionParameter protectionParameter;

    SetKeyStoreCredentialAuthenticationConfiguration(final AuthenticationConfiguration parent, final KeyStore keyStore, final String alias, final KeyStore.ProtectionParameter protectionParameter) {
        super(parent.without(SetPasswordAuthenticationConfiguration.class).without(SetCallbackHandlerAuthenticationConfiguration.class));
        this.keyStore = keyStore;
        this.alias = alias;
        this.protectionParameter = protectionParameter;
    }

    AuthenticationConfiguration reparent(final AuthenticationConfiguration newParent) {
        return new SetKeyStoreCredentialAuthenticationConfiguration(newParent, keyStore, alias, protectionParameter);
    }

    void handleCallback(final Callback[] callbacks, final int index) throws IOException, UnsupportedCallbackException {
        final Callback callback = callbacks[index];
        if (callback instanceof CredentialCallback) {
            final CredentialCallback credentialCallback = (CredentialCallback) callback;
            final KeyStore.Entry entry;
            try {
                entry = keyStore.getEntry(alias, protectionParameter);
            } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
                throw new IOException("Unable to read credential", e);
            }
            if (entry instanceof PasswordEntry) {
                credentialCallback.setCredential(((PasswordEntry) entry).getPassword());
                return;
            } else if (entry instanceof KeyStore.PrivateKeyEntry) {
                final KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;
                final Certificate[] certificateChain = privateKeyEntry.getCertificateChain();
                if (certificateChain == null || certificateChain.length == 0) {
                    credentialCallback.setCredential(privateKeyEntry.getPrivateKey());
                    return;
                } else {
                    final Certificate certificate = privateKeyEntry.getCertificate();
                    if (certificate instanceof X509Certificate) {
                        credentialCallback.setCredential(new X500PrivateCredential((X509Certificate) certificate, privateKeyEntry.getPrivateKey()));
                        return;
                    }
                }
            } else if (entry instanceof KeyStore.TrustedCertificateEntry) {
                credentialCallback.setCredential(((KeyStore.TrustedCertificateEntry) entry).getTrustedCertificate());
                return;
            } else if (entry instanceof KeyStore.SecretKeyEntry) {
                credentialCallback.setCredential(((KeyStore.SecretKeyEntry) entry).getSecretKey());
                return;
            }
        } else if (callback instanceof PasswordCallback) {
            final KeyStore.Entry entry;
            try {
                entry = keyStore.getEntry(alias, protectionParameter);
            } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
                throw new IOException("Unable to read credential", e);
            }
            if (entry instanceof PasswordEntry) {
                final Password password = ((PasswordEntry) entry).getPassword();
                final PasswordFactory passwordFactory;
                try {
                    passwordFactory = PasswordFactory.getInstance(password.getAlgorithm());
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException("Unable to read credential", e);
                }
                final ClearPasswordSpec keySpec;
                try {
                    keySpec = passwordFactory.getKeySpec(password, ClearPasswordSpec.class);
                } catch (InvalidKeySpecException e) {
                    throw new IOException("Unable to read credential", e);
                }
                ((PasswordCallback) callback).setPassword(keySpec.getEncodedPassword());
                return;
            }
        }
        super.handleCallback(callbacks, index);
    }
}
