/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.drone.browserstack.extension.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * This is an implementation of the {@link RemoteWebDriver} intended to be used with BrowserStack account.
 * BrowserStack is a cloud-based cross-browser testing tool. See browserstack.com
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackDriver extends RemoteWebDriver {

    public static final String READABLE_NAME = "browserstack";
    public static final String BROWSERSTACK_LOCAL_MANAGED = "browserstack.local.managed";
    public static final String BROWSERSTACK_LOCAL = "browserstack.local";

    private static final Logger log = Logger.getLogger(BrowserStackDriver.class.getName());

    public BrowserStackDriver(URL url, Capabilities capabilities) {
        super(url, capabilities);
    }

    @Override
    public void get(String url) {
        String host = null;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (host != null && ("localhost" .equals(host) || "127.0.0.1" .equals(host))) {
            if (getCapabilities().is(BROWSERSTACK_LOCAL) && !getCapabilities().is(BROWSERSTACK_LOCAL_MANAGED)) {
                log.info(
                    "To test against localhost and other locations behind your firewall, you need to run a BrowserStackLocal binary. "
                        + "You can ignore this if you have already started it, otherwise see browserstack.com/local-testing");
            }
        }
        super.get(url);
    }

}
