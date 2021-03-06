/*
 * Copyright 2010-2018 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.outbound.transmission;

import com.google.inject.Injector;
import com.google.inject.util.Modules;
import no.difi.oxalis.api.lang.OxalisContentException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionMessage;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.oxalis.test.lookup.MockLookupModule;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TransmissionRequestFactoryMockTest {

    private Injector injector = com.google.inject.Guice.createInjector(
            Modules.override(new GuiceModuleLoader()).with(new MockLookupModule()));

    @Inject
    private TransmissionRequestFactory transmissionRequestFactory;

    @Inject
    private LookupService lookupService;

    @BeforeClass
    public void beforeClass() {
        transmissionRequestFactory = injector.getInstance(TransmissionRequestFactory.class);
        lookupService = injector.getInstance(LookupService.class);
    }

    @Test
    public void simple() throws Exception {
        MockLookupModule.resetService();

        TransmissionMessage transmissionMessage;
        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionMessage = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionMessage.getHeader());
    }

    @Test(expectedExceptions = OxalisContentException.class)
    public void unrecognizedContent() throws Exception {
        transmissionRequestFactory.newInstance(new ByteArrayInputStream("Hello World!".getBytes()));
    }
}
