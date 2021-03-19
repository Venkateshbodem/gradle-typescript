/*
 * (c) Copyright 2021 Felipe Orozco, Robert Kruszewski. All rights reserved.
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

package com.gradlets.gradle.typescript.shim;

import com.gradlets.gradle.typescript.shim.cache.DescriptorLoader;
import com.gradlets.gradle.typescript.shim.clients.PackageJson;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.proxy.ProxyCallback;
import io.undertow.server.handlers.proxy.ProxyClient;
import io.undertow.server.handlers.proxy.ProxyConnection;
import java.util.concurrent.TimeUnit;

public final class NpmProxyClient implements ProxyClient {
    private final DescriptorLoader descriptorLoader;
    private final ProxyClient delegate;

    public NpmProxyClient(DescriptorLoader descriptorLoader, ProxyClient delegate) {
        this.descriptorLoader = descriptorLoader;
        this.delegate = delegate;
    }

    @Override
    public ProxyTarget findTarget(HttpServerExchange exchange) {
        return delegate.findTarget(exchange);
    }

    @Override
    public void getConnection(
            ProxyTarget target,
            HttpServerExchange exchange,
            ProxyCallback<ProxyConnection> callback,
            long timeout,
            TimeUnit timeUnit) {
        IvyPatterns.parseArtifactPath(exchange.getRelativePath()).ifPresent(moduleIdentifier -> {
            PackageJson packageJson = descriptorLoader
                    .getIvyDescriptor(moduleIdentifier.packageName(), moduleIdentifier.packageVersion())
                    .packageJson()
                    .get();
            exchange.setRequestURI(packageJson.dist().tarball(), true);
        });
        delegate.getConnection(target, exchange, callback, timeout, timeUnit);
    }
}