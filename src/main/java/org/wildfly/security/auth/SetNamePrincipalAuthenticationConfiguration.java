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
import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.wildfly.security.auth.principal.NamePrincipal;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class SetNamePrincipalAuthenticationConfiguration extends AuthenticationConfiguration {

    private final NamePrincipal principal;

    SetNamePrincipalAuthenticationConfiguration(final AuthenticationConfiguration parent, final NamePrincipal principal) {
        super(parent.without(SetCallbackHandlerAuthenticationConfiguration.class));
        this.principal = principal;
    }

    void handleCallback(final Callback[] callbacks, final int index) throws UnsupportedCallbackException, IOException {
        Callback callback = callbacks[index];
        if (callback instanceof NameCallback) {
            ((NameCallback) callback).setName(principal.getName());
        } else {
            super.handleCallback(callbacks, index);
        }
    }

    Principal getPrincipal() {
        return principal;
    }

    AuthenticationConfiguration reparent(final AuthenticationConfiguration newParent) {
        return new SetNamePrincipalAuthenticationConfiguration(newParent, principal);
    }
}
