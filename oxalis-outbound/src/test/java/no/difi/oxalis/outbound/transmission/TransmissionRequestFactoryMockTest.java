/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
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

import brave.Span;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import no.difi.oxalis.api.lang.OxalisTransmissionException;
import no.difi.oxalis.api.lookup.LookupService;
import no.difi.oxalis.api.outbound.TransmissionRequest;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import no.difi.oxalis.test.lookup.MockLookupModule;
import no.difi.vefa.peppol.common.model.Header;
import org.mockito.Mockito;
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

        TransmissionRequest transmissionRequest;
        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequest = transmissionRequestFactory.newInstance(inputStream);
        }

        Assert.assertNotNull(transmissionRequest.getHeader());
        Assert.assertNotNull(transmissionRequest.getEndpoint());
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void endpointNotFound() throws Exception {
        Mockito.reset(lookupService);
        Mockito.when(lookupService.lookup(Mockito.any(Header.class), Mockito.any(Span.class)))
                .thenThrow(new OxalisTransmissionException("Exception from unit test."));

        try (InputStream inputStream = getClass().getResourceAsStream("/ehf-bii05-t10-valid-invoice.xml")) {
            transmissionRequestFactory.newInstance(inputStream);
        }
    }

    @Test(expectedExceptions = OxalisTransmissionException.class)
    public void unrecognizedContent() throws Exception {
        transmissionRequestFactory.newInstance(new ByteArrayInputStream("Hello World!".getBytes()));
    }
}