package edu.tuberlin.dima.textmining.jedi.core.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.impl.XCASDeserializer;
import org.apache.uima.cas.impl.XCASSerializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

/**
 * XMI Cas serializer / de-serializer Helper.
 *
 */
public class UIMAXMLConverterHelper {

    private static final Log LOG = LogFactory.getLog(UIMAXMLConverterHelper.class);


    private final XCASSerializer ser;
    private final boolean formattedOutput;

    /**
     * Creates a new instance of the XML serializer.
     *
     * @param formattedOutput if true the output will be XML pretty printed, otherwise it will be just on one line
     * @throws IllegalStateException in case of errors
     */
    public UIMAXMLConverterHelper(boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
        JCas jCas = null;
        try {
            jCas = JCasFactory.createJCas();
            ser = new XCASSerializer(jCas.getTypeSystem());
        } catch (UIMAException e) {
            throw new IllegalStateException("Can't initialize the UIMA XCASSerializer", e);
        }
    }

    public String serialize(JCas jCas) throws IOException, SAXException {
        StringWriter writer = new StringWriter();
        XMLSerializer xmlSer = new XMLSerializer(writer, formattedOutput);
        ser.serialize(jCas.getCas(), xmlSer.getContentHandler());
        return writer.toString();
    }

    public boolean serialize(JCas jCas, OutputStream outputStream) throws IOException, SAXException {
        XMLSerializer xmlSer = new XMLSerializer(outputStream, formattedOutput);
        ser.serialize(jCas.getCas(), xmlSer.getContentHandler());

        return true;
    }

    public JCas deserialize(InputStream inputStream, JCas newElement) throws IOException, UIMAException, InterruptedException, SAXException {

        try {
            newElement.reset();
            // deserialize CAS
            XCASDeserializer.deserialize(inputStream, newElement.getCas());
            //XmiCasDeserializer.deserialize(inputStream, newElement.getCas());
            return newElement;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

    }

    public static String sanitizeString(String input) {
        // sanitize the content
        // get document text
        if(input != null) {
            char[] docCharArray = input.toCharArray();
            replaceInvalidXmlChars(docCharArray);
            return String.valueOf(docCharArray);
        }
        return input;
    }

    /**
     * This method is taken from
     *
     * uimaj-core
     *
     * org/apache/uima/util/CasToInlineXml
     * @param aChars input
     */
    private static void replaceInvalidXmlChars(char[] aChars) {
        for (int i = 0; i < aChars.length; i++) {
            if ((aChars[i] < 0x20 && aChars[i] != 0x09 && aChars[i] != 0x0A && aChars[i] != 0x0D)
                    || (aChars[i] > 0xD7FF && aChars[i] < 0xE000) || aChars[i] == 0xFFFE
                    || aChars[i] == 0xFFFF) {
                // System.out.println("Found invalid XML character: " + (int)aChars[i] + " at position " +
                // i); //temp
                aChars[i] = ' ';
            }
        }
    }

    public JCas deserialize(String input, JCas newElement) throws IOException, UIMAException, InterruptedException, SAXException {
        return deserialize(IOUtils.toInputStream(input, Charsets.UTF_8.name()), newElement);
    }

}
