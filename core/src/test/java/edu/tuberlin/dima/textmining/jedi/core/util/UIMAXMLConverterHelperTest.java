package edu.tuberlin.dima.textmining.jedi.core.util;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class UIMAXMLConverterHelperTest {

    private JCas testCas;
    private UIMAXMLConverterHelper converterHelper;

    @Before
    public void setUp() throws Exception {
        try {
            testCas = JCasFactory.createJCas();
            converterHelper = new UIMAXMLConverterHelper(true);
        } catch (ResourceInitializationException e) {
            throw new IllegalArgumentException(e);
        }

    }

    String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<CAS version=\"2\">\n" +
            "    <uima.cas.Sofa _indexed=\"0\" _id=\"6\" sofaNum=\"1\" sofaID=\"_InitialView\" mimeType=\"text\" sofaString=\"Quickly and easily change content on your web site. ReadÂ On .&#10;Â &#10;ZazÂ® PayMeStore&#153;&#10;Increase your sales. Take orders for products and services on your web site. We make it fast, secure, and convenient. More .&#10;Â &#10;ZazÂ® NewsMailings&#153;&#10;Get in touch. Keep your customers up-to-date with all the latest news. Customers easily &quot;add me&quot; to your mailing list. Â  GÂ eÂ tÂ Â zÂ aÂ z Â .&#10;zazDIRECT &#153;&#10;Easily Share Info with Others *&#10;Rate the Listings *\"/>\n" +
            "    <uima.tcas.DocumentAnnotation _indexed=\"1\" _id=\"1\" _ref_sofa=\"6\" begin=\"0\" end=\"427\" language=\"en\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"18\" _ref_sofa=\"6\" begin=\"0\" end=\"7\" _ref_lemma=\"779\" _ref_pos=\"774\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"26\" _ref_sofa=\"6\" begin=\"8\" end=\"11\" _ref_lemma=\"789\" _ref_pos=\"784\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"34\" _ref_sofa=\"6\" begin=\"12\" end=\"18\" _ref_lemma=\"799\" _ref_pos=\"794\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"42\" _ref_sofa=\"6\" begin=\"19\" end=\"25\" _ref_lemma=\"809\" _ref_pos=\"804\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"50\" _ref_sofa=\"6\" begin=\"26\" end=\"33\" _ref_lemma=\"819\" _ref_pos=\"814\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"58\" _ref_sofa=\"6\" begin=\"34\" end=\"36\" _ref_lemma=\"829\" _ref_pos=\"824\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"66\" _ref_sofa=\"6\" begin=\"37\" end=\"41\" _ref_lemma=\"839\" _ref_pos=\"834\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"74\" _ref_sofa=\"6\" begin=\"42\" end=\"45\" _ref_lemma=\"849\" _ref_pos=\"844\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"82\" _ref_sofa=\"6\" begin=\"46\" end=\"50\" _ref_lemma=\"859\" _ref_pos=\"854\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"90\" _ref_sofa=\"6\" begin=\"50\" end=\"51\" _ref_lemma=\"869\" _ref_pos=\"864\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"98\" _ref_sofa=\"6\" begin=\"52\" end=\"56\" _ref_lemma=\"879\" _ref_pos=\"874\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"106\" _ref_sofa=\"6\" begin=\"57\" end=\"59\" _ref_lemma=\"889\" _ref_pos=\"884\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"114\" _ref_sofa=\"6\" begin=\"60\" end=\"61\" _ref_lemma=\"899\" _ref_pos=\"894\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"122\" _ref_sofa=\"6\" begin=\"64\" end=\"67\" _ref_lemma=\"909\" _ref_pos=\"904\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"130\" _ref_sofa=\"6\" begin=\"67\" end=\"68\" _ref_lemma=\"919\" _ref_pos=\"914\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"138\" _ref_sofa=\"6\" begin=\"69\" end=\"79\" _ref_lemma=\"929\" _ref_pos=\"924\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"146\" _ref_sofa=\"6\" begin=\"81\" end=\"89\" _ref_lemma=\"939\" _ref_pos=\"934\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"154\" _ref_sofa=\"6\" begin=\"90\" end=\"94\" _ref_lemma=\"949\" _ref_pos=\"944\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"162\" _ref_sofa=\"6\" begin=\"95\" end=\"100\" _ref_lemma=\"959\" _ref_pos=\"954\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"170\" _ref_sofa=\"6\" begin=\"100\" end=\"101\" _ref_lemma=\"969\" _ref_pos=\"964\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"178\" _ref_sofa=\"6\" begin=\"102\" end=\"106\" _ref_lemma=\"979\" _ref_pos=\"974\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"186\" _ref_sofa=\"6\" begin=\"107\" end=\"113\" _ref_lemma=\"989\" _ref_pos=\"984\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"194\" _ref_sofa=\"6\" begin=\"114\" end=\"117\" _ref_lemma=\"999\" _ref_pos=\"994\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"202\" _ref_sofa=\"6\" begin=\"118\" end=\"126\" _ref_lemma=\"1009\" _ref_pos=\"1004\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"210\" _ref_sofa=\"6\" begin=\"127\" end=\"130\" _ref_lemma=\"1019\" _ref_pos=\"1014\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"218\" _ref_sofa=\"6\" begin=\"131\" end=\"139\" _ref_lemma=\"1029\" _ref_pos=\"1024\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"226\" _ref_sofa=\"6\" begin=\"140\" end=\"142\" _ref_lemma=\"1039\" _ref_pos=\"1034\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"234\" _ref_sofa=\"6\" begin=\"143\" end=\"147\" _ref_lemma=\"1049\" _ref_pos=\"1044\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"242\" _ref_sofa=\"6\" begin=\"148\" end=\"151\" _ref_lemma=\"1059\" _ref_pos=\"1054\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"250\" _ref_sofa=\"6\" begin=\"152\" end=\"156\" _ref_lemma=\"1069\" _ref_pos=\"1064\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"258\" _ref_sofa=\"6\" begin=\"156\" end=\"157\" _ref_lemma=\"1079\" _ref_pos=\"1074\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"266\" _ref_sofa=\"6\" begin=\"158\" end=\"160\" _ref_lemma=\"1089\" _ref_pos=\"1084\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"274\" _ref_sofa=\"6\" begin=\"161\" end=\"165\" _ref_lemma=\"1099\" _ref_pos=\"1094\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"282\" _ref_sofa=\"6\" begin=\"166\" end=\"168\" _ref_lemma=\"1109\" _ref_pos=\"1104\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"290\" _ref_sofa=\"6\" begin=\"169\" end=\"173\" _ref_lemma=\"1119\" _ref_pos=\"1114\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"298\" _ref_sofa=\"6\" begin=\"173\" end=\"174\" _ref_lemma=\"1129\" _ref_pos=\"1124\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"306\" _ref_sofa=\"6\" begin=\"175\" end=\"181\" _ref_lemma=\"1139\" _ref_pos=\"1134\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"314\" _ref_sofa=\"6\" begin=\"181\" end=\"182\" _ref_lemma=\"1149\" _ref_pos=\"1144\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"322\" _ref_sofa=\"6\" begin=\"183\" end=\"186\" _ref_lemma=\"1159\" _ref_pos=\"1154\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"330\" _ref_sofa=\"6\" begin=\"187\" end=\"197\" _ref_lemma=\"1169\" _ref_pos=\"1164\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"338\" _ref_sofa=\"6\" begin=\"197\" end=\"198\" _ref_lemma=\"1179\" _ref_pos=\"1174\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"346\" _ref_sofa=\"6\" begin=\"199\" end=\"203\" _ref_lemma=\"1189\" _ref_pos=\"1184\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"354\" _ref_sofa=\"6\" begin=\"204\" end=\"205\" _ref_lemma=\"1199\" _ref_pos=\"1194\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"362\" _ref_sofa=\"6\" begin=\"208\" end=\"211\" _ref_lemma=\"1209\" _ref_pos=\"1204\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"370\" _ref_sofa=\"6\" begin=\"211\" end=\"212\" _ref_lemma=\"1219\" _ref_pos=\"1214\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"378\" _ref_sofa=\"6\" begin=\"213\" end=\"225\" _ref_lemma=\"1229\" _ref_pos=\"1224\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"386\" _ref_sofa=\"6\" begin=\"227\" end=\"230\" _ref_lemma=\"1239\" _ref_pos=\"1234\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"394\" _ref_sofa=\"6\" begin=\"231\" end=\"233\" _ref_lemma=\"1249\" _ref_pos=\"1244\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"402\" _ref_sofa=\"6\" begin=\"234\" end=\"239\" _ref_lemma=\"1259\" _ref_pos=\"1254\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"410\" _ref_sofa=\"6\" begin=\"239\" end=\"240\" _ref_lemma=\"1269\" _ref_pos=\"1264\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"418\" _ref_sofa=\"6\" begin=\"241\" end=\"245\" _ref_lemma=\"1279\" _ref_pos=\"1274\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"426\" _ref_sofa=\"6\" begin=\"246\" end=\"250\" _ref_lemma=\"1289\" _ref_pos=\"1284\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"434\" _ref_sofa=\"6\" begin=\"251\" end=\"260\" _ref_lemma=\"1299\" _ref_pos=\"1294\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"442\" _ref_sofa=\"6\" begin=\"261\" end=\"271\" _ref_lemma=\"1309\" _ref_pos=\"1304\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"450\" _ref_sofa=\"6\" begin=\"272\" end=\"276\" _ref_lemma=\"1319\" _ref_pos=\"1314\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"458\" _ref_sofa=\"6\" begin=\"277\" end=\"280\" _ref_lemma=\"1329\" _ref_pos=\"1324\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"466\" _ref_sofa=\"6\" begin=\"281\" end=\"284\" _ref_lemma=\"1339\" _ref_pos=\"1334\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"474\" _ref_sofa=\"6\" begin=\"285\" end=\"291\" _ref_lemma=\"1349\" _ref_pos=\"1344\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"482\" _ref_sofa=\"6\" begin=\"292\" end=\"296\" _ref_lemma=\"1359\" _ref_pos=\"1354\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"490\" _ref_sofa=\"6\" begin=\"296\" end=\"297\" _ref_lemma=\"1369\" _ref_pos=\"1364\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"498\" _ref_sofa=\"6\" begin=\"298\" end=\"307\" _ref_lemma=\"1379\" _ref_pos=\"1374\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"506\" _ref_sofa=\"6\" begin=\"308\" end=\"314\" _ref_lemma=\"1389\" _ref_pos=\"1384\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"514\" _ref_sofa=\"6\" begin=\"315\" end=\"316\" _ref_lemma=\"1399\" _ref_pos=\"1394\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"522\" _ref_sofa=\"6\" begin=\"316\" end=\"319\" _ref_lemma=\"1409\" _ref_pos=\"1404\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"530\" _ref_sofa=\"6\" begin=\"320\" end=\"322\" _ref_lemma=\"1419\" _ref_pos=\"1414\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"538\" _ref_sofa=\"6\" begin=\"322\" end=\"323\" _ref_lemma=\"1429\" _ref_pos=\"1424\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"546\" _ref_sofa=\"6\" begin=\"324\" end=\"326\" _ref_lemma=\"1439\" _ref_pos=\"1434\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"554\" _ref_sofa=\"6\" begin=\"327\" end=\"331\" _ref_lemma=\"1449\" _ref_pos=\"1444\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"562\" _ref_sofa=\"6\" begin=\"332\" end=\"339\" _ref_lemma=\"1459\" _ref_pos=\"1454\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"570\" _ref_sofa=\"6\" begin=\"340\" end=\"344\" _ref_lemma=\"1469\" _ref_pos=\"1464\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"578\" _ref_sofa=\"6\" begin=\"344\" end=\"345\" _ref_lemma=\"1479\" _ref_pos=\"1474\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"586\" _ref_sofa=\"6\" begin=\"348\" end=\"349\" _ref_lemma=\"1489\" _ref_pos=\"1484\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"594\" _ref_sofa=\"6\" begin=\"350\" end=\"351\" _ref_lemma=\"1499\" _ref_pos=\"1494\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"602\" _ref_sofa=\"6\" begin=\"352\" end=\"353\" _ref_lemma=\"1509\" _ref_pos=\"1504\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"610\" _ref_sofa=\"6\" begin=\"355\" end=\"356\" _ref_lemma=\"1519\" _ref_pos=\"1514\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"618\" _ref_sofa=\"6\" begin=\"357\" end=\"358\" _ref_lemma=\"1529\" _ref_pos=\"1524\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"626\" _ref_sofa=\"6\" begin=\"359\" end=\"360\" _ref_lemma=\"1539\" _ref_pos=\"1534\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"634\" _ref_sofa=\"6\" begin=\"362\" end=\"363\" _ref_lemma=\"1549\" _ref_pos=\"1544\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"642\" _ref_sofa=\"6\" begin=\"364\" end=\"373\" _ref_lemma=\"1559\" _ref_pos=\"1554\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"650\" _ref_sofa=\"6\" begin=\"376\" end=\"382\" _ref_lemma=\"1569\" _ref_pos=\"1564\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"658\" _ref_sofa=\"6\" begin=\"383\" end=\"388\" _ref_lemma=\"1579\" _ref_pos=\"1574\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"666\" _ref_sofa=\"6\" begin=\"389\" end=\"393\" _ref_lemma=\"1589\" _ref_pos=\"1584\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"674\" _ref_sofa=\"6\" begin=\"394\" end=\"398\" _ref_lemma=\"1599\" _ref_pos=\"1594\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"682\" _ref_sofa=\"6\" begin=\"399\" end=\"405\" _ref_lemma=\"1609\" _ref_pos=\"1604\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"690\" _ref_sofa=\"6\" begin=\"406\" end=\"407\" _ref_lemma=\"1619\" _ref_pos=\"1614\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"698\" _ref_sofa=\"6\" begin=\"408\" end=\"412\" _ref_lemma=\"1629\" _ref_pos=\"1624\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"706\" _ref_sofa=\"6\" begin=\"413\" end=\"416\" _ref_lemma=\"1639\" _ref_pos=\"1634\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"714\" _ref_sofa=\"6\" begin=\"417\" end=\"425\" _ref_lemma=\"1649\" _ref_pos=\"1644\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token _indexed=\"1\" _id=\"722\" _ref_sofa=\"6\" begin=\"426\" end=\"427\" _ref_lemma=\"1659\" _ref_pos=\"1654\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"730\" _ref_sofa=\"6\" begin=\"0\" end=\"51\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"734\" _ref_sofa=\"6\" begin=\"52\" end=\"61\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"738\" _ref_sofa=\"6\" begin=\"64\" end=\"101\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"742\" _ref_sofa=\"6\" begin=\"102\" end=\"157\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"746\" _ref_sofa=\"6\" begin=\"158\" end=\"198\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"750\" _ref_sofa=\"6\" begin=\"199\" end=\"205\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"754\" _ref_sofa=\"6\" begin=\"208\" end=\"240\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"758\" _ref_sofa=\"6\" begin=\"241\" end=\"297\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"762\" _ref_sofa=\"6\" begin=\"298\" end=\"345\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"766\" _ref_sofa=\"6\" begin=\"348\" end=\"363\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence _indexed=\"1\" _id=\"770\" _ref_sofa=\"6\" begin=\"364\" end=\"427\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV _indexed=\"1\" _id=\"774\" _ref_sofa=\"6\" begin=\"0\" end=\"7\" PosValue=\"RB\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV _indexed=\"1\" _id=\"794\" _ref_sofa=\"6\" begin=\"12\" end=\"18\" PosValue=\"RB\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV _indexed=\"1\" _id=\"1114\" _ref_sofa=\"6\" begin=\"169\" end=\"173\" PosValue=\"RB\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV _indexed=\"1\" _id=\"1384\" _ref_sofa=\"6\" begin=\"308\" end=\"314\" PosValue=\"RB\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"779\" _ref_sofa=\"6\" begin=\"0\" end=\"7\" value=\"quickly\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"789\" _ref_sofa=\"6\" begin=\"8\" end=\"11\" value=\"and\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"799\" _ref_sofa=\"6\" begin=\"12\" end=\"18\" value=\"easily\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"809\" _ref_sofa=\"6\" begin=\"19\" end=\"25\" value=\"change\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"819\" _ref_sofa=\"6\" begin=\"26\" end=\"33\" value=\"content\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"829\" _ref_sofa=\"6\" begin=\"34\" end=\"36\" value=\"on\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"839\" _ref_sofa=\"6\" begin=\"37\" end=\"41\" value=\"your\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"849\" _ref_sofa=\"6\" begin=\"42\" end=\"45\" value=\"web\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"859\" _ref_sofa=\"6\" begin=\"46\" end=\"50\" value=\"site\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"869\" _ref_sofa=\"6\" begin=\"50\" end=\"51\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"879\" _ref_sofa=\"6\" begin=\"52\" end=\"56\" value=\"Read\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"889\" _ref_sofa=\"6\" begin=\"57\" end=\"59\" value=\"On\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"899\" _ref_sofa=\"6\" begin=\"60\" end=\"61\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"909\" _ref_sofa=\"6\" begin=\"64\" end=\"67\" value=\"Zaz\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"919\" _ref_sofa=\"6\" begin=\"67\" end=\"68\" value=\"Â®\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"929\" _ref_sofa=\"6\" begin=\"69\" end=\"79\" value=\"PayMeStore\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"939\" _ref_sofa=\"6\" begin=\"81\" end=\"89\" value=\"increase\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"949\" _ref_sofa=\"6\" begin=\"90\" end=\"94\" value=\"your\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"959\" _ref_sofa=\"6\" begin=\"95\" end=\"100\" value=\"sale\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"969\" _ref_sofa=\"6\" begin=\"100\" end=\"101\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"979\" _ref_sofa=\"6\" begin=\"102\" end=\"106\" value=\"take\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"989\" _ref_sofa=\"6\" begin=\"107\" end=\"113\" value=\"order\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"999\" _ref_sofa=\"6\" begin=\"114\" end=\"117\" value=\"for\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1009\" _ref_sofa=\"6\" begin=\"118\" end=\"126\" value=\"product\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1019\" _ref_sofa=\"6\" begin=\"127\" end=\"130\" value=\"and\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1029\" _ref_sofa=\"6\" begin=\"131\" end=\"139\" value=\"service\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1039\" _ref_sofa=\"6\" begin=\"140\" end=\"142\" value=\"on\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1049\" _ref_sofa=\"6\" begin=\"143\" end=\"147\" value=\"your\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1059\" _ref_sofa=\"6\" begin=\"148\" end=\"151\" value=\"web\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1069\" _ref_sofa=\"6\" begin=\"152\" end=\"156\" value=\"site\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1079\" _ref_sofa=\"6\" begin=\"156\" end=\"157\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1089\" _ref_sofa=\"6\" begin=\"158\" end=\"160\" value=\"we\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1099\" _ref_sofa=\"6\" begin=\"161\" end=\"165\" value=\"make\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1109\" _ref_sofa=\"6\" begin=\"166\" end=\"168\" value=\"it\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1119\" _ref_sofa=\"6\" begin=\"169\" end=\"173\" value=\"fast\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1129\" _ref_sofa=\"6\" begin=\"173\" end=\"174\" value=\",\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1139\" _ref_sofa=\"6\" begin=\"175\" end=\"181\" value=\"secure\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1149\" _ref_sofa=\"6\" begin=\"181\" end=\"182\" value=\",\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1159\" _ref_sofa=\"6\" begin=\"183\" end=\"186\" value=\"and\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1169\" _ref_sofa=\"6\" begin=\"187\" end=\"197\" value=\"convenient\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1179\" _ref_sofa=\"6\" begin=\"197\" end=\"198\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1189\" _ref_sofa=\"6\" begin=\"199\" end=\"203\" value=\"more\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1199\" _ref_sofa=\"6\" begin=\"204\" end=\"205\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1209\" _ref_sofa=\"6\" begin=\"208\" end=\"211\" value=\"Zaz\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1219\" _ref_sofa=\"6\" begin=\"211\" end=\"212\" value=\"Â®\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1229\" _ref_sofa=\"6\" begin=\"213\" end=\"225\" value=\"NewsMailings\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1239\" _ref_sofa=\"6\" begin=\"227\" end=\"230\" value=\"get\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1249\" _ref_sofa=\"6\" begin=\"231\" end=\"233\" value=\"in\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1259\" _ref_sofa=\"6\" begin=\"234\" end=\"239\" value=\"touch\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1269\" _ref_sofa=\"6\" begin=\"239\" end=\"240\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1279\" _ref_sofa=\"6\" begin=\"241\" end=\"245\" value=\"keep\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1289\" _ref_sofa=\"6\" begin=\"246\" end=\"250\" value=\"your\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1299\" _ref_sofa=\"6\" begin=\"251\" end=\"260\" value=\"customer\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1309\" _ref_sofa=\"6\" begin=\"261\" end=\"271\" value=\"up-to-date\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1319\" _ref_sofa=\"6\" begin=\"272\" end=\"276\" value=\"with\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1329\" _ref_sofa=\"6\" begin=\"277\" end=\"280\" value=\"all\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1339\" _ref_sofa=\"6\" begin=\"281\" end=\"284\" value=\"the\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1349\" _ref_sofa=\"6\" begin=\"285\" end=\"291\" value=\"late\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1359\" _ref_sofa=\"6\" begin=\"292\" end=\"296\" value=\"news\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1369\" _ref_sofa=\"6\" begin=\"296\" end=\"297\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1379\" _ref_sofa=\"6\" begin=\"298\" end=\"307\" value=\"customer\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1389\" _ref_sofa=\"6\" begin=\"308\" end=\"314\" value=\"easily\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1399\" _ref_sofa=\"6\" begin=\"315\" end=\"316\" value=\"&quot;\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1409\" _ref_sofa=\"6\" begin=\"316\" end=\"319\" value=\"add\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1419\" _ref_sofa=\"6\" begin=\"320\" end=\"322\" value=\"me\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1429\" _ref_sofa=\"6\" begin=\"322\" end=\"323\" value=\"&quot;\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1439\" _ref_sofa=\"6\" begin=\"324\" end=\"326\" value=\"to\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1449\" _ref_sofa=\"6\" begin=\"327\" end=\"331\" value=\"your\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1459\" _ref_sofa=\"6\" begin=\"332\" end=\"339\" value=\"mailing\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1469\" _ref_sofa=\"6\" begin=\"340\" end=\"344\" value=\"list\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1479\" _ref_sofa=\"6\" begin=\"344\" end=\"345\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1489\" _ref_sofa=\"6\" begin=\"348\" end=\"349\" value=\"G\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1499\" _ref_sofa=\"6\" begin=\"350\" end=\"351\" value=\"e\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1509\" _ref_sofa=\"6\" begin=\"352\" end=\"353\" value=\"t\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1519\" _ref_sofa=\"6\" begin=\"355\" end=\"356\" value=\"z\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1529\" _ref_sofa=\"6\" begin=\"357\" end=\"358\" value=\"a\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1539\" _ref_sofa=\"6\" begin=\"359\" end=\"360\" value=\"z\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1549\" _ref_sofa=\"6\" begin=\"362\" end=\"363\" value=\".\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1559\" _ref_sofa=\"6\" begin=\"364\" end=\"373\" value=\"zazDIRECT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1569\" _ref_sofa=\"6\" begin=\"376\" end=\"382\" value=\"Easily\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1579\" _ref_sofa=\"6\" begin=\"383\" end=\"388\" value=\"Share\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1589\" _ref_sofa=\"6\" begin=\"389\" end=\"393\" value=\"Info\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1599\" _ref_sofa=\"6\" begin=\"394\" end=\"398\" value=\"with\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1609\" _ref_sofa=\"6\" begin=\"399\" end=\"405\" value=\"Others\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1619\" _ref_sofa=\"6\" begin=\"406\" end=\"407\" value=\"*\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1629\" _ref_sofa=\"6\" begin=\"408\" end=\"412\" value=\"Rate\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1639\" _ref_sofa=\"6\" begin=\"413\" end=\"416\" value=\"the\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1649\" _ref_sofa=\"6\" begin=\"417\" end=\"425\" value=\"Listings\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma _indexed=\"1\" _id=\"1659\" _ref_sofa=\"6\" begin=\"426\" end=\"427\" value=\"*\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ _indexed=\"1\" _id=\"784\" _ref_sofa=\"6\" begin=\"8\" end=\"11\" PosValue=\"CC\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ _indexed=\"1\" _id=\"1014\" _ref_sofa=\"6\" begin=\"127\" end=\"130\" PosValue=\"CC\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CONJ _indexed=\"1\" _id=\"1154\" _ref_sofa=\"6\" begin=\"183\" end=\"186\" PosValue=\"CC\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"804\" _ref_sofa=\"6\" begin=\"19\" end=\"25\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"934\" _ref_sofa=\"6\" begin=\"81\" end=\"89\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"974\" _ref_sofa=\"6\" begin=\"102\" end=\"106\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"1094\" _ref_sofa=\"6\" begin=\"161\" end=\"165\" PosValue=\"VVP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"1134\" _ref_sofa=\"6\" begin=\"175\" end=\"181\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"1234\" _ref_sofa=\"6\" begin=\"227\" end=\"230\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"1274\" _ref_sofa=\"6\" begin=\"241\" end=\"245\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V _indexed=\"1\" _id=\"1404\" _ref_sofa=\"6\" begin=\"316\" end=\"319\" PosValue=\"VV\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"814\" _ref_sofa=\"6\" begin=\"26\" end=\"33\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"844\" _ref_sofa=\"6\" begin=\"42\" end=\"45\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"854\" _ref_sofa=\"6\" begin=\"46\" end=\"50\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"904\" _ref_sofa=\"6\" begin=\"64\" end=\"67\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"954\" _ref_sofa=\"6\" begin=\"95\" end=\"100\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"984\" _ref_sofa=\"6\" begin=\"107\" end=\"113\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1004\" _ref_sofa=\"6\" begin=\"118\" end=\"126\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1024\" _ref_sofa=\"6\" begin=\"131\" end=\"139\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1054\" _ref_sofa=\"6\" begin=\"148\" end=\"151\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1064\" _ref_sofa=\"6\" begin=\"152\" end=\"156\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1204\" _ref_sofa=\"6\" begin=\"208\" end=\"211\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1254\" _ref_sofa=\"6\" begin=\"234\" end=\"239\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1294\" _ref_sofa=\"6\" begin=\"251\" end=\"260\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1354\" _ref_sofa=\"6\" begin=\"292\" end=\"296\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1374\" _ref_sofa=\"6\" begin=\"298\" end=\"307\" PosValue=\"NNS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1454\" _ref_sofa=\"6\" begin=\"332\" end=\"339\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1464\" _ref_sofa=\"6\" begin=\"340\" end=\"344\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1504\" _ref_sofa=\"6\" begin=\"352\" end=\"353\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1514\" _ref_sofa=\"6\" begin=\"355\" end=\"356\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1534\" _ref_sofa=\"6\" begin=\"359\" end=\"360\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN _indexed=\"1\" _id=\"1554\" _ref_sofa=\"6\" begin=\"364\" end=\"373\" PosValue=\"NN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"824\" _ref_sofa=\"6\" begin=\"34\" end=\"36\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"994\" _ref_sofa=\"6\" begin=\"114\" end=\"117\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"1034\" _ref_sofa=\"6\" begin=\"140\" end=\"142\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"1244\" _ref_sofa=\"6\" begin=\"231\" end=\"233\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"1314\" _ref_sofa=\"6\" begin=\"272\" end=\"276\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP _indexed=\"1\" _id=\"1594\" _ref_sofa=\"6\" begin=\"394\" end=\"398\" PosValue=\"IN\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"834\" _ref_sofa=\"6\" begin=\"37\" end=\"41\" PosValue=\"PP$\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"944\" _ref_sofa=\"6\" begin=\"90\" end=\"94\" PosValue=\"PP$\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1044\" _ref_sofa=\"6\" begin=\"143\" end=\"147\" PosValue=\"PP$\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1084\" _ref_sofa=\"6\" begin=\"158\" end=\"160\" PosValue=\"PP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1104\" _ref_sofa=\"6\" begin=\"166\" end=\"168\" PosValue=\"PP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1284\" _ref_sofa=\"6\" begin=\"246\" end=\"250\" PosValue=\"PP$\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1414\" _ref_sofa=\"6\" begin=\"320\" end=\"322\" PosValue=\"PP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PR _indexed=\"1\" _id=\"1444\" _ref_sofa=\"6\" begin=\"327\" end=\"331\" PosValue=\"PP$\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"864\" _ref_sofa=\"6\" begin=\"50\" end=\"51\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"894\" _ref_sofa=\"6\" begin=\"60\" end=\"61\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"914\" _ref_sofa=\"6\" begin=\"67\" end=\"68\" PosValue=\"SYM\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"964\" _ref_sofa=\"6\" begin=\"100\" end=\"101\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1074\" _ref_sofa=\"6\" begin=\"156\" end=\"157\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1124\" _ref_sofa=\"6\" begin=\"173\" end=\"174\" PosValue=\",\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1144\" _ref_sofa=\"6\" begin=\"181\" end=\"182\" PosValue=\",\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1174\" _ref_sofa=\"6\" begin=\"197\" end=\"198\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1194\" _ref_sofa=\"6\" begin=\"204\" end=\"205\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1214\" _ref_sofa=\"6\" begin=\"211\" end=\"212\" PosValue=\"SYM\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1264\" _ref_sofa=\"6\" begin=\"239\" end=\"240\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1364\" _ref_sofa=\"6\" begin=\"296\" end=\"297\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1474\" _ref_sofa=\"6\" begin=\"344\" end=\"345\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1544\" _ref_sofa=\"6\" begin=\"362\" end=\"363\" PosValue=\"SENT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1614\" _ref_sofa=\"6\" begin=\"406\" end=\"407\" PosValue=\"SYM\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC _indexed=\"1\" _id=\"1654\" _ref_sofa=\"6\" begin=\"426\" end=\"427\" PosValue=\"SYM\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"874\" _ref_sofa=\"6\" begin=\"52\" end=\"56\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"884\" _ref_sofa=\"6\" begin=\"57\" end=\"59\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"924\" _ref_sofa=\"6\" begin=\"69\" end=\"79\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1224\" _ref_sofa=\"6\" begin=\"213\" end=\"225\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1484\" _ref_sofa=\"6\" begin=\"348\" end=\"349\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1494\" _ref_sofa=\"6\" begin=\"350\" end=\"351\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1564\" _ref_sofa=\"6\" begin=\"376\" end=\"382\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1574\" _ref_sofa=\"6\" begin=\"383\" end=\"388\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1584\" _ref_sofa=\"6\" begin=\"389\" end=\"393\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1604\" _ref_sofa=\"6\" begin=\"399\" end=\"405\" PosValue=\"NPS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1624\" _ref_sofa=\"6\" begin=\"408\" end=\"412\" PosValue=\"NP\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NP _indexed=\"1\" _id=\"1644\" _ref_sofa=\"6\" begin=\"417\" end=\"425\" PosValue=\"NPS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ _indexed=\"1\" _id=\"1164\" _ref_sofa=\"6\" begin=\"187\" end=\"197\" PosValue=\"JJ\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ _indexed=\"1\" _id=\"1184\" _ref_sofa=\"6\" begin=\"199\" end=\"203\" PosValue=\"JJR\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ _indexed=\"1\" _id=\"1304\" _ref_sofa=\"6\" begin=\"261\" end=\"271\" PosValue=\"JJ\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ _indexed=\"1\" _id=\"1344\" _ref_sofa=\"6\" begin=\"285\" end=\"291\" PosValue=\"JJS\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART _indexed=\"1\" _id=\"1324\" _ref_sofa=\"6\" begin=\"277\" end=\"280\" PosValue=\"PDT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART _indexed=\"1\" _id=\"1334\" _ref_sofa=\"6\" begin=\"281\" end=\"284\" PosValue=\"DT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART _indexed=\"1\" _id=\"1524\" _ref_sofa=\"6\" begin=\"357\" end=\"358\" PosValue=\"DT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART _indexed=\"1\" _id=\"1634\" _ref_sofa=\"6\" begin=\"413\" end=\"416\" PosValue=\"DT\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O _indexed=\"1\" _id=\"1394\" _ref_sofa=\"6\" begin=\"315\" end=\"316\" PosValue=\"``\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O _indexed=\"1\" _id=\"1424\" _ref_sofa=\"6\" begin=\"322\" end=\"323\" PosValue=\"''\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.O _indexed=\"1\" _id=\"1434\" _ref_sofa=\"6\" begin=\"324\" end=\"326\" PosValue=\"TO\"/>\n" +
            "    <de.tudarmstadt.ukp.dkpro.core.api.ner.type.Organization _indexed=\"1\" _id=\"1664\" _ref_sofa=\"6\" begin=\"69\" end=\"89\" value=\"ORGANIZATION\"/>\n" +
            "</CAS>";

    @Test
    public void testConvert() throws Exception {

        testCas.reset();

        UIMAXMLConverterHelper uimaxmlConverterHelper = new UIMAXMLConverterHelper(false);
        uimaxmlConverterHelper.deserialize(IOUtils.toInputStream(XML, Charsets.UTF_8.name()), testCas);

        Collection<NamedEntity> namedEntities = JCasUtil.select(testCas, NamedEntity.class);

        for (NamedEntity namedEntity : namedEntities) {
            System.out.println(namedEntity.getType() + " " + namedEntity.getCoveredText());
        }
        System.out.println(Joiner.on("").join(namedEntities));


    }

}