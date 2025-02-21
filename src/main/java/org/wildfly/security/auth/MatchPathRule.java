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
class MatchPathRule extends MatchRule {

    private final String pathSpec;

    MatchPathRule(final MatchRule parent, final String pathSpec) {
        super(parent);
        if (pathSpec.endsWith("/")) {
            this.pathSpec = pathSpec;
        } else {
            this.pathSpec = pathSpec + "/";
        }
    }

    public boolean matches(final URI uri) {
        return uri.isAbsolute() && prefixes(uri.getPath()) && super.matches(uri);
    }

    MatchRule reparent(final MatchRule newParent) {
        return new MatchPathRule(newParent, pathSpec);
    }

    boolean halfEqual(final MatchRule other) {
        return pathSpec.equals(other.getMatchPath()) && parentHalfEqual(other);
    }

    public String getMatchPath() {
        return pathSpec;
    }

    public boolean isPathMatched() {
        return true;
    }

    public int hashCode() {
        // our prime is 3923
        return 3923 * pathSpec.hashCode() + parentHashCode();
    }

    private boolean prefixes(String pathSpec) {
        if (pathSpec == null) pathSpec = "/";
        if (! pathSpec.startsWith("/")) pathSpec = "/" + pathSpec;
        if (! pathSpec.endsWith("/")) pathSpec = pathSpec + "/";
        return pathSpec.startsWith(this.pathSpec);
    }

    StringBuilder asString(final StringBuilder b) {
        return parentAsString(b).append("path=~").append(pathSpec).append(',');
    }
}
