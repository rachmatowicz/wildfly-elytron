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

import org.wildfly.security.auth.util.NameRewriter;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class RewriteNameAuthenticationConfiguration extends AuthenticationConfiguration {

    private final NameRewriter rewriter;

    RewriteNameAuthenticationConfiguration(final AuthenticationConfiguration parent, final NameRewriter rewriter) {
        super(parent, true);
        this.rewriter = rewriter;
    }

    String doRewriteUser(final String original) {
        return rewriter.rewriteName(super.doRewriteUser(original));
    }

    AuthenticationConfiguration reparent(final AuthenticationConfiguration newParent) {
        return new RewriteNameAuthenticationConfiguration(newParent, rewriter);
    }
}
