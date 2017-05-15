/**
 * Copyright 2015 - Tássio Guerreiro Antunes Virgínio
 *
 * Este arquivo é parte do programa Reserva de Recursos
 *
 * O Reserva de Recursos é um software livre; você pode redistribui-lo e/ou modifica-lo
 * dentro dos termos da Licença Pública Geral GNU como publicada pela
 * Fundação do Software Livre (FSF); na versão 2 da Licença.
 *
 * Este programa é distribuido na esperança que possa ser util, mas SEM
 * NENHUMA GARANTIA; sem uma garantia implicita de ADEQUAÇÂO a qualquer
 * MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU
 * para maiores detalhes.
 *
 * Você deve ter recebido uma cópia da Licença Pública Geral GNU, sob o
 * título "licensa_uso.htm", junto com este programa, se não, escreva para a
 * Fundação do Software Livre(FSF) Inc., 51 Franklin St, Fifth Floor,
 */
package br.reservarecursos.xml;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by tassio on 03/12/15.
 */
public class PFSenseImport {

    private static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            textVal = el.getFirstChild().getNodeValue();
        }
        return textVal;
    }

    private static User getUser(Element empEl) {
        String name = getTextValue(empEl, "name");
        String password = getTextValue(empEl, "md5-hash");
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        return user;
    }

    public static void main2(String[] args) {
        String senhaMD5 = "";
        try {
            String s = "teste";
            MessageDigest m2 = MessageDigest.getInstance("MD5");
            m2.update(s.getBytes(), 0, s.length());
            senhaMD5 = (new BigInteger(1, m2.digest()).toString(16)).toString();
            System.out.println("MD5: " + senhaMD5);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {
        String uri = "/home/tassio/Downloads/system-config-tron.ifto.local-20151203140535.xml";
        try {
            DOMParser parser = new DOMParser();
            parser.parse(uri);
            org.w3c.dom.Document doc = parser.getDocument();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document dom = db.parse(uri);
            org.w3c.dom.Element docEle = dom.getDocumentElement();
            org.w3c.dom.NodeList nl = docEle.getElementsByTagName("user");
            if (nl != null && nl.getLength() > 0) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Element el = (org.w3c.dom.Element) nl.item(i);
                    User user = getUser(el);
                    System.out.println("INSERT INTO Usuario(nome,login,senha) values ('"
                            + user.getName() + "','"
                            + user.getName() + "','"
                            + user.getPassword() + "');");
//                    System.out.println(user);
//                        myEmpls.add(e);
                }
            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
