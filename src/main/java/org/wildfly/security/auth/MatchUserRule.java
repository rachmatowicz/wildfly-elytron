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

import java.net.URI;

/**
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
class MatchUserRule extends MatchRule {

    private final String userSpec;

    MatchUserRule(final MatchRule parent, final String userSpec) {
        super(parent.without(MatchNoUserRule.class));
        this.userSpec = userSpec;
    }

    public boolean matches(final URI uri) {
        String userInfo = getUserInfo(uri);
        return userInfo != null && userInfo.equals(userSpec) && super.matches(uri);
    }

    MatchRule reparent(final MatchRule newParent) {
        return new MatchUserRule(newParent, userSpec);
    }

    public String getMatchUser() {
        return userSpec;
    }

    public boolean isUserMatched() {
        return true;
    }

    boolean halfEqual(final MatchRule other) {
        return userSpec.equals(other.getMatchUser());
    }

    public int hashCode() {
        return 3323 * userSpec.hashCode() + parentHashCode();
    }

    StringBuilder asString(final StringBuilder b) {
        return parentAsString(b).append("user=").append(userSpec).append(',');
    }
}
