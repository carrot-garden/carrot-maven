/**
 * Copyright (C) 2010-2012 Andrei Pozolotin <Andrei.Pozolotin@gmail.com>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package org.apache.karaf.tooling.features;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.karaf.features.internal.model.Features;
import org.apache.karaf.features.internal.model.JaxbUtil;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @version $Rev: 1204969 $ $Date: 2011-11-22 07:11:34 -0600 (Tue, 22 Nov 2011) $
 */
public class GenerateDescriptorMojoTest {

    @Test
    public void testReadXml() throws JAXBException, SAXException, ParserConfigurationException, XMLStreamException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("input-features.xml");
        Features featuresRoot = JaxbUtil.unmarshal(in, false);
        assert featuresRoot.getRepository().size() == 1;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JaxbUtil.marshal(featuresRoot, baos);
        String s = new String(baos.toByteArray());
        assert s.indexOf("repository") > -1;
        assert s.indexOf("http://karaf.apache.org/xmlns/features/v1.0.0") > -1;
    }
}
